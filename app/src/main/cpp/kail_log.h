// kail_log.h
//
// 全项目统一的 native 日志宏。目标：日志「详细但不杂乱」，方便定位问题。
//
// 与 Java/Kotlin 侧的 KailLog 对齐：每条日志都带「文件:行号#函数」定位信息，
// 并区分级别（V/D/I/W/E）。高频(V/D)日志受 kail_log_verbose 运行时开关控制，
// 默认关闭以避免刷屏；W/E 始终输出。
//
// 用法：
//   #include "kail_log.h"
//   static const char *kTag = "StepSensor.native";
//   KLOGI(kTag, "doHook installed %d hooks", n);
//   KLOGV(kTag, "event type=%d", type);        // 仅在 verbose 开启时输出
//   KLOGE(kTag, "parse failed: %s", err);
//
// 运行时开关（可由 JNI/属性控制）：
//   kail::setVerboseLogging(true);

#ifndef KAIL_LOG_H
#define KAIL_LOG_H

#include <android/log.h>

namespace kail {

// 高频/调试(V/D)日志开关。默认关闭，避免热点路径刷屏。
inline bool &verboseFlag() {
  static bool gVerbose = false;
  return gVerbose;
}

inline void setVerboseLogging(bool enabled) { verboseFlag() = enabled; }
inline bool verboseLogging() { return verboseFlag(); }

}  // namespace kail

// 仅取文件名（去掉目录前缀），让日志保持简洁。
#define KAIL_FILE_                                                             \
  (__builtin_strrchr(__FILE__, '/') ? __builtin_strrchr(__FILE__, '/') + 1    \
                                    : __FILE__)

// 通用打印：自动附加 [file:line#func] 定位信息。
// 采用变参宏 + __android_log_print 的原生 printf 语义，零依赖、可移植。
#define KAIL_LOG_(prio, tag, fmt, ...)                                        \
  __android_log_print(prio, tag, "[%s:%d#%s] " fmt, KAIL_FILE_, __LINE__,     \
                      __func__, ##__VA_ARGS__)

// 高频/逐事件级别：受 verbose 开关控制，默认关闭，避免热点路径刷屏。
#define KLOGV(tag, fmt, ...)                                                   \
  do {                                                                        \
    if (kail::verboseLogging())                                               \
      KAIL_LOG_(ANDROID_LOG_VERBOSE, tag, fmt, ##__VA_ARGS__);                \
  } while (0)

// 调试级别：以 DEBUG 优先级输出（由 logcat 的级别过滤控制，不额外门控），
// 适合低频的状态/分支日志。高频日志请用 KLOGV。
#define KLOGD(tag, fmt, ...) KAIL_LOG_(ANDROID_LOG_DEBUG, tag, fmt, ##__VA_ARGS__)

// 关键状态：始终输出。
#define KLOGI(tag, fmt, ...) KAIL_LOG_(ANDROID_LOG_INFO, tag, fmt, ##__VA_ARGS__)
#define KLOGW(tag, fmt, ...) KAIL_LOG_(ANDROID_LOG_WARN, tag, fmt, ##__VA_ARGS__)
#define KLOGE(tag, fmt, ...) KAIL_LOG_(ANDROID_LOG_ERROR, tag, fmt, ##__VA_ARGS__)

#endif  // KAIL_LOG_H
