// elf_hooker.h
//
// Reconstructed PLT/GOT hook engine shared by libStepSensor and libantidetect.
// Recovered from the ElfReader / ElfHooker classes in do/complete/libStepSensor.c
// (arm64) and do/complete/libantidetect64.c / libantidetect.c (arm64 + arm).
//
// ElfHooker locates a loaded module in /proc/self/maps; ElfReader parses that
// module's in-memory dynamic segment (symbol table, relocations, ELF / GNU
// hash tables) and rewrites the GOT entry for a named imported symbol so calls
// route through a replacement function.  The original target is returned so the
// hook can chain to it.
//
// This header is ABI-neutral: it selects the natural-width ELF structures and
// relocation flavour at compile time, so the same source compiles for both
// arm64-v8a (Elf64 / RELA / R_AARCH64_*) and armeabi-v7a (Elf32 / REL /
// R_ARM_*).  The algorithm is preserved exactly from the decompilation.

#ifndef ELF_HOOKER_H
#define ELF_HOOKER_H

#include <cstdint>
#include <cstdio>
#include <cstdlib>
#include <cstring>

#include <elf.h>
#include <unistd.h>
#include <sys/mman.h>

#include <android/log.h>

#include "kail_log.h"

namespace elfhook {

static const char *kTag = "Hook.native";

// ---------------------------------------------------------------------------
// ABI selection
// ---------------------------------------------------------------------------
#if defined(__LP64__)

using ElfEhdr = Elf64_Ehdr;
using ElfPhdr = Elf64_Phdr;
using ElfDyn  = Elf64_Dyn;
using ElfSym  = Elf64_Sym;
using ElfRelo = Elf64_Rela;          // arm64 uses RELA (with addend)
using BloomWord = uint64_t;

#define ELFHOOK_R_TYPE(i) ELF64_R_TYPE(i)
#define ELFHOOK_R_SYM(i)  ELF64_R_SYM(i)

static const int      kElfClass     = ELFCLASS64;
static const uint16_t kElfMachine   = EM_AARCH64;
static const uint32_t kRJumpSlot    = 1026;   // R_AARCH64_JUMP_SLOT
static const uint32_t kRGlobDat     = 1025;   // R_AARCH64_GLOB_DAT
static const uint32_t kRAbs         = 257;    // R_AARCH64_ABS64

#else  // 32-bit ARM

using ElfEhdr = Elf32_Ehdr;
using ElfPhdr = Elf32_Phdr;
using ElfDyn  = Elf32_Dyn;
using ElfSym  = Elf32_Sym;
using ElfRelo = Elf32_Rel;           // arm uses REL (no addend)
using BloomWord = uint32_t;

#define ELFHOOK_R_TYPE(i) ELF32_R_TYPE(i)
#define ELFHOOK_R_SYM(i)  ELF32_R_SYM(i)

static const int      kElfClass     = ELFCLASS32;
static const uint16_t kElfMachine   = EM_ARM;
static const uint32_t kRJumpSlot    = 22;     // R_ARM_JUMP_SLOT
static const uint32_t kRGlobDat     = 21;     // R_ARM_GLOB_DAT
static const uint32_t kRAbs         = 2;      // R_ARM_ABS32

#endif

static const unsigned kBloomBits = sizeof(BloomWord) * 8;
static const uintptr_t kPageMask = ~(uintptr_t)0xfff;

// ---------------------------------------------------------------------------
// ElfReader: parse one loaded module and patch its relocations.
// ---------------------------------------------------------------------------
class ElfReader {
 public:
  ElfReader(const char *name, void *base)
      : name_(name), base_(reinterpret_cast<uintptr_t>(base)) {}

  // parse(): validate the header and walk the dynamic segment.  0 on success.
  int parse() {
    ehdr_ = reinterpret_cast<ElfEhdr *>(base_);
    if (verifyHeader() != 0)
      return -1;

    phnum_ = ehdr_->e_phnum;
    phdr_  = reinterpret_cast<ElfPhdr *>(base_ + ehdr_->e_phoff);

    // Compute the load bias from the first PT_LOAD segment.
    bias_ = 0;
    bool found = false;
    for (uint16_t i = 0; i < phnum_; ++i) {
      if (phdr_[i].p_type == PT_LOAD) {
        bias_ = base_ + phdr_[i].p_offset - phdr_[i].p_vaddr;
        found = true;
        break;
      }
    }
    if (!found) {
      KLOGE(kTag, "failed to get segment base address");
      return -1;
    }

    if (parseDynamic() != 0) {
      KLOGE(kTag, "failed to parse dynamic segment");
      return -1;
    }
    return 0;
  }

  // hook(): redirect the GOT slot for `symbol` to `replacement`, returning the
  // previous target through `original`.  0 on success.
  int hook(const char *symbol, void *replacement, void **original) {
    ElfSym *sym = nullptr;
    unsigned symIndex = 0;
    if (findSymbolByName(symbol, &sym, &symIndex) != 0) {
      KLOGD(kTag, "hook %s failure in %s", symbol, name_);
      return -1;
    }

    bool patched = false;

    // PLT relocations (lazy-bound calls): JUMP_SLOT.
    for (size_t i = 0; i < pltReloCount_; ++i) {
      const ElfRelo &r = pltRelo_[i];
      if (ELFHOOK_R_TYPE(r.r_info) == kRJumpSlot &&
          ELFHOOK_R_SYM(r.r_info) == symIndex) {
        if (hookInternally(reinterpret_cast<void **>(bias_ + r.r_offset),
                           replacement, original) == 0)
          patched = true;
        break;
      }
    }

    // Regular relocations (data references): GLOB_DAT / ABS.
    if (!patched) {
      for (size_t i = 0; i < reloCount_; ++i) {
        const ElfRelo &r = relo_[i];
        uint32_t type = ELFHOOK_R_TYPE(r.r_info);
        if ((type == kRGlobDat || type == kRAbs) &&
            ELFHOOK_R_SYM(r.r_info) == symIndex) {
          if (hookInternally(reinterpret_cast<void **>(bias_ + r.r_offset),
                             replacement, original) == 0)
            patched = true;
          break;
        }
      }
    }

    if (patched) {
      KLOGD(kTag, "hook %s successfully in %s", symbol, name_);
      return 0;
    }
    KLOGD(kTag, "hook %s failure in %s", symbol, name_);
    return -1;
  }

 private:
  int verifyHeader() {
    const ElfEhdr *e = reinterpret_cast<ElfEhdr *>(base_);
    if (memcmp(e->e_ident, ELFMAG, SELFMAG) != 0) {
      KLOGE(kTag, "wrong elf format for magic");
      return -1;
    }
    if (e->e_ident[EI_CLASS] != kElfClass) {
      KLOGE(kTag, "wrong elf bit format");
      return -1;
    }
    if (e->e_ident[EI_DATA] != ELFDATA2LSB) {
      KLOGE(kTag, "wrong elf format for not little-endian");
      return -1;
    }
    if (e->e_type != ET_DYN) {
      KLOGE(kTag, "wrong elf format for e_type");
      return -1;
    }
    if (e->e_machine != kElfMachine) {
      KLOGE(kTag, "wrong elf format for e_machine");
      return -1;
    }
    return 0;
  }

  int parseDynamic() {
    ElfDyn *dyn = nullptr;
    for (uint16_t i = 0; i < phnum_; ++i) {
      if (phdr_[i].p_type == PT_DYNAMIC) {
        dyn = reinterpret_cast<ElfDyn *>(bias_ + phdr_[i].p_vaddr);
        break;
      }
    }
    if (!dyn)
      return -1;

    for (; dyn->d_tag != DT_NULL; ++dyn) {
      switch (dyn->d_tag) {
        case DT_PLTRELSZ:
          pltReloCount_ = dyn->d_un.d_val / sizeof(ElfRelo);
          break;
        case DT_STRTAB:
          strtab_ = reinterpret_cast<const char *>(bias_ + dyn->d_un.d_ptr);
          break;
        case DT_SYMTAB:
          symtab_ = reinterpret_cast<ElfSym *>(bias_ + dyn->d_un.d_ptr);
          break;
        case DT_JMPREL:
          pltRelo_ = reinterpret_cast<ElfRelo *>(bias_ + dyn->d_un.d_ptr);
          break;
#if defined(__LP64__)
        case DT_RELA:
          relo_ = reinterpret_cast<ElfRelo *>(bias_ + dyn->d_un.d_ptr);
          break;
        case DT_RELASZ:
          reloCount_ = dyn->d_un.d_val / sizeof(ElfRelo);
          break;
        case DT_REL:
        case DT_RELSZ:
          KLOGE(kTag, "unsupported DT_REL in %s", name_);
          return -1;
#else
        case DT_REL:
          relo_ = reinterpret_cast<ElfRelo *>(bias_ + dyn->d_un.d_ptr);
          break;
        case DT_RELSZ:
          reloCount_ = dyn->d_un.d_val / sizeof(ElfRelo);
          break;
        case DT_RELA:
          KLOGE(kTag, "unsupported DT_RELA in %s", name_);
          return -1;
        case DT_RELASZ:
          KLOGE(kTag, "unsupported DT_RELASZ in %s", name_);
          return -1;
#endif
        case DT_PLTREL:
#if defined(__LP64__)
          if (dyn->d_un.d_val != DT_RELA) {
            KLOGE(kTag, "unsupported DT_PLTREL in %s, expected DT_RELA", name_);
            return -1;
          }
#else
          if (dyn->d_un.d_val != DT_REL) {
            KLOGE(kTag, "unsupported DT_PLTREL in %s, expected DT_REL", name_);
            return -1;
          }
#endif
          break;
        case DT_HASH: {
          const uint32_t *h = reinterpret_cast<uint32_t *>(bias_ + dyn->d_un.d_ptr);
          elfNbucket_ = h[0];
          elfNchain_  = h[1];
          elfBucket_  = h + 2;
          elfChain_   = elfBucket_ + elfNbucket_;
          break;
        }
        case DT_GNU_HASH: {
          const uint32_t *h = reinterpret_cast<uint32_t *>(bias_ + dyn->d_un.d_ptr);
          gnuNbucket_   = h[0];
          gnuSymoffset_ = h[1];
          gnuMaskwords_ = h[2];
          gnuShift2_    = h[3];
          gnuBloom_     = reinterpret_cast<const BloomWord *>(h + 4);
          gnuBucket_    = reinterpret_cast<const uint32_t *>(gnuBloom_ + gnuMaskwords_);
          gnuChain_     = gnuBucket_ + gnuNbucket_ - gnuSymoffset_;
          if (gnuMaskwords_ & (gnuMaskwords_ - 1)) {
            KLOGE(kTag, "invalid maskwords for gnu_hash in %s", name_);
            return -1;
          }
          gnuMaskwords_ -= 1;
          useGnuHash_ = true;
          break;
        }
        default:
          break;
      }
    }

    if (!strtab_ || !symtab_) {
      KLOGE(kTag, "no DT_STRTAB or DT_SYMTAB found in %s", name_);
      return -1;
    }
    return 0;
  }

  static uint32_t elfHash(const char *name) {
    uint32_t h = 0, g;
    for (const unsigned char *p = (const unsigned char *)name; *p; ++p) {
      h = (h << 4) + *p;
      g = h & 0xf0000000;
      h ^= g >> 24;
      h &= ~g;
    }
    return h;
  }

  static uint32_t gnuHash(const char *name) {
    uint32_t h = 5381;
    for (const unsigned char *p = (const unsigned char *)name; *p; ++p)
      h += (h << 5) + *p;
    return h;
  }

  int findSymbolByName(const char *name, ElfSym **out, unsigned *outIndex) {
    if (!useGnuHash_)
      return elfLookup(name, out, outIndex);
    if (gnuLookup(name, out, outIndex) == 0)
      return 0;

    // Fallback: linear scan up to the gnu symbol offset.
    for (uint32_t i = 0; i < gnuSymoffset_; ++i) {
      if (strcmp(&strtab_[symtab_[i].st_name], name) == 0) {
        *outIndex = i;
        *out = &symtab_[i];
        return 0;
      }
    }
    KLOGE(kTag, "not found %s in %s before gnu symbol index %u", name, name_, gnuSymoffset_);
    return -1;
  }

  int gnuLookup(const char *name, ElfSym **out, unsigned *outIndex) {
    uint32_t hash = gnuHash(name);
    BloomWord word = gnuBloom_[(hash / kBloomBits) & gnuMaskwords_];
    BloomWord mask = ((BloomWord)1 << (hash % kBloomBits)) |
                     ((BloomWord)1 << ((hash >> gnuShift2_) % kBloomBits));

    *out = nullptr;
    *outIndex = 0;
    if ((word & mask) != mask) {
      KLOGE(kTag, "gnuLookup: not found symbol %s in %s", name, name_);
      return -1;
    }

    uint32_t n = gnuBucket_[hash % gnuNbucket_];
    if (n == 0) {
      KLOGE(kTag, "gnuLookup: not found symbol %s in %s", name, name_);
      return -1;
    }

    do {
      ElfSym *sym = &symtab_[n];
      if (((gnuChain_[n] ^ hash) >> 1) == 0 &&
          strcmp(&strtab_[sym->st_name], name) == 0) {
        *outIndex = n;
        *out = sym;
        return 0;
      }
    } while ((gnuChain_[n++] & 1) == 0);

    KLOGE(kTag, "gnuLookup: not found symbol %s in %s", name, name_);
    return -1;
  }

  int elfLookup(const char *name, ElfSym **out, unsigned *outIndex) {
    if (!elfBucket_ || !elfChain_)
      return -1;
    uint32_t hash = elfHash(name);
    for (uint32_t n = elfBucket_[hash % elfNbucket_]; n != 0; n = elfChain_[n]) {
      if (strcmp(&strtab_[symtab_[n].st_name], name) == 0) {
        *outIndex = n;
        *out = &symtab_[n];
        return 0;
      }
    }
    return -1;
  }

  int hookInternally(void **gotEntry, void *replacement, void **original) {
    if (*gotEntry == replacement) {
      KLOGD(kTag, "already been hooked");
      return 0;
    }

    // Find the segment that owns this GOT entry so we can derive its protection.
    ElfPhdr *seg = nullptr;
    uintptr_t addr = reinterpret_cast<uintptr_t>(gotEntry);
    for (uint16_t i = 0; i < phnum_; ++i) {
      uintptr_t start = (bias_ + phdr_[i].p_vaddr) & kPageMask;
      uintptr_t end   = (bias_ + phdr_[i].p_vaddr + phdr_[i].p_memsz + 0xfff) & kPageMask;
      if (start <= addr && addr <= end) {
        seg = &phdr_[i];
        break;
      }
    }
    if (!seg) {
      KLOGE(kTag, "failed to find segment for address %p in %s", gotEntry, name_);
      return -1;
    }

    // Matches the decompilation: PROT_WRITE plus PROT_READ when the segment is
    // readable.  ((p_flags >> 2) & 1) extracts PF_R into the PROT_READ bit.
    int prot = PROT_WRITE | ((seg->p_flags >> 2) & 1);
    void *page = reinterpret_cast<void *>(addr & kPageMask);
    if (mprotect(page, 0x1000, prot) != 0) {
      KLOGE(kTag, "failed to mprotect %p in %s", page, name_);
      return -1;
    }

    *original = *gotEntry;
    *gotEntry = replacement;
    __builtin___clear_cache((char *)page, (char *)page + 0x1000);
    return 0;
  }

  const char *name_;
  uintptr_t   base_  = 0;
  uintptr_t   bias_  = 0;
  ElfEhdr    *ehdr_  = nullptr;
  ElfPhdr    *phdr_  = nullptr;
  uint16_t    phnum_ = 0;

  const char *strtab_  = nullptr;
  ElfSym     *symtab_  = nullptr;
  ElfRelo    *relo_    = nullptr;
  size_t      reloCount_   = 0;
  ElfRelo    *pltRelo_ = nullptr;
  size_t      pltReloCount_ = 0;

  // ELF (SysV) hash table.
  uint32_t        elfNbucket_ = 0;
  uint32_t        elfNchain_  = 0;
  const uint32_t *elfBucket_  = nullptr;
  const uint32_t *elfChain_   = nullptr;

  // GNU hash table.
  bool             useGnuHash_   = false;
  uint32_t         gnuNbucket_   = 0;
  uint32_t         gnuSymoffset_ = 0;
  uint32_t         gnuMaskwords_ = 0;
  uint32_t         gnuShift2_    = 0;
  const BloomWord *gnuBloom_     = nullptr;
  const uint32_t  *gnuBucket_    = nullptr;
  const uint32_t  *gnuChain_     = nullptr;
};

// ---------------------------------------------------------------------------
// ElfHooker: locate a module's path / base address in /proc/self/maps.
// ---------------------------------------------------------------------------
class ElfHooker {
 public:
  // get_module_base: first mapping base whose path contains `keyword`.
  static void *getModuleBase(const char *keyword) {
    FILE *fp = fopen("/proc/self/maps", "r");
    if (!fp)
      return nullptr;
    void *base = nullptr;
    char line[1024];
    while (fgets(line, sizeof(line), fp)) {
      if (strstr(line, keyword)) {
        base = (void *)strtoul(line, nullptr, 16);
        break;
      }
    }
    fclose(fp);
    return base;
  }

  // get_module_path: full path of the first executable mapping that contains
  // `keyword`.  Returns a pointer into a static buffer.
  static const char *getModulePath(const char *keyword) {
    static char path[512];
    FILE *fp = fopen("/proc/self/maps", "r");
    if (!fp)
      return nullptr;
    const char *result = nullptr;
    char line[1024];
    while (fgets(line, sizeof(line), fp)) {
      if (strstr(line, keyword) && (strstr(line, "r-xp") || strstr(line, "r--p"))) {
        char *slash = strchr(line, '/');
        if (slash) {
          size_t len = strcspn(slash, "\n");
          if (len < sizeof(path)) {
            memcpy(path, slash, len);
            path[len] = 0;
            result = path;
          }
        }
        break;
      }
    }
    fclose(fp);
    return result;
  }
};

}  // namespace elfhook

#endif  // ELF_HOOKER_H
