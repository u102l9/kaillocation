# 架构指南

> Kail Location 的模块结构、部署模式、注入系统、数据层与原生代码。

## 1. 项目概览

Kail Location 是一个面向开发与测试人员的位置/传感器调试工具，提供路线、位置、导航、步频、虚拟定位与 NFC 模拟。应用基于 **Kotlin + Jetpack Compose + MVVM**，并通过 Root、Xposed、沙盒、开发者模式四种部署方式将模拟数据注入到目标进程。

```
kail_location/
├── app/                      主应用（com.kail.location）
├── KailLocationXposed/       独立的 Xposed 模块（com.kail.locationxposed）
├── NewBlackbox/              沙盒框架
│   ├── Bcore/                沙盒核心（被 :app 依赖）
│   ├── black-reflection/     反射注解处理
│   └── compiler/             编译器模块
├── docs/                     文档
└── assets/                   资源
```

## 2. 模块结构

### 2.1 `:app` — 主应用

包名 `com.kail.location`，承载 UI、业务逻辑、四种部署模式的服务与原生注入器。源码位于 `app/src/main/java/com/kail/location/`：

| 包 | 职责 |
| --- | --- |
| `utils/` | `GoApplication`（入口）、`KailLog`、`MapUtils`、`ShellUtils` 等 |
| `views/` | Compose UI，按功能分子包（见 §6） |
| `viewmodels/` | 各功能对应的 ViewModel |
| `service/` | 四种部署模式的前台服务（见 §3） |
| `inject/` | 注入到目标进程的代码（见 §4） |
| `repositories/` | 数据仓库，`RootMockRepository` 统一调度模拟 |
| `data/local/` | Room 数据库（`AppDatabase`、`HistoryDao`、`HistoryEntity`） |
| `sandbox/` | 沙盒管理（`SandboxManager`、`SandboxSettingsManager`） |
| `root/` | `NativeSensorHook`，传感器原生 hook 的 Kotlin 桥接 |
| `geo/` | 地理计算（`GeoMath`、`GeoPredict`） |
| `nmea/` | NMEA 语句生成 |
| `network/` | Wigle / OpenCellId / RuoYi 网络客户端 |
| `auth/` | `AuthManager`、`UsageManager` |
| `models/` | 数据模型 |

### 2.2 `KailLocationXposed` — Xposed 模块

独立 APK，包名 `com.kail.locationxposed`，声明为 `xposedmodule`。在 Xposed/LSPosed 框架下加载，hook 目标应用的定位相关系统 API。核心源码在 `xposed/`：

- `core/FakeLocationXposed` — 模块入口
- `core/KailCommandHandler` — 接收主 app 通过 `LocationManager.sendExtraCommand` 发来的命令
- `core/FakeLocState` — 全局状态
- `hooks/` — 各类 hook（`LocationManagerHook`、`LocationServiceHook`、`BasicLocationHook`、`gnss/GnssHook`、`gnss/LocationNMEAHook` 等）
- `sensor/` — 传感器 hook（`NativeSensorHook`、`SensorHookLite`）
- `base/BaseLocationHook` — hook 基类

### 2.3 `NewBlackbox` — 沙盒框架

源自 BlackBox 的虚拟容器框架，提供应用级隔离的运行环境。主 app 仅依赖 `:NewBlackbox:Bcore`（见 `settings.gradle.kts` 注释）。沙盒模式下，目标应用运行在容器内，`SandboxLocationHook` 在容器内对定位 API 做拦截。

## 3. 部署模式

`service/` 下按模式分四个子包，每个模式一个前台服务，由 `RootMockRepository` 和各 ViewModel 统一调度：

| 模式 | 服务类 | 原理 | 前置条件 |
| --- | --- | --- | --- |
| Developer | `service/Developer/ServiceGoDeveloper` | 使用系统 Mock Location API（`MockLocationProvider`）写入测试位置 | 开发者选项 + 选择模拟位置应用 |
| Root | `service/Root/ServiceGoRoot` + `RootDeployer` | ptrace 注入 `libfakeloc.so` 到 `system_server`，加载 `inject.dex` 安装系统级 hook | Root 权限 |
| Xposed | `service/Xposed/ServiceGoXposed` | 通过 `LocationManager.sendExtraCommand` 向 Xposed 模块下发命令，由模块 hook 目标进程 | 已安装并启用 Xposed/LSPosed 模块 |
| Sandbox | `service/Sandbox/ServiceGoSandbox` + `SandboxLocationHook` | 目标应用运行在 NewBlackbox 容器内，在容器进程内 hook 定位 API | 授权沙盒权限 |

四种服务共享统一的 `Intent` extras 协议（`EXTRA_CONTROL_ACTION`、`EXTRA_ROUTE_POINTS`、`EXTRA_STEP_ENABLED` 等，见 `ServiceConstants`），支持启动/暂停/恢复/停止/拖动进度/调速等控制。

### 3.1 Root 模式注入流程

`RootDeployer`（`service/Root/RootDeployer.kt`）负责部署与引导：

1. **部署产物**到 `/data/kail-loc/`：
   - `deployInjectorBin` — `kail_inject`（ptrace 注入器可执行文件，打包为 `libkail_inject.so`）
   - `deployFakelocLibs` — `libfakeloc.so`（注入目标进程的动态库）
   - `deployDexPayload` — `inject.dex`（slim DEX，见 §4.2）
   - `deployNativeHookLib` — `libkail_native_hook.so`（传感器 hook）
2. **bootstrapInjection** — 通过 `su` 执行 `kail_inject -P system_server -l libfakeloc.so -n com.kail.location`
3. 注入器 ptrace attach 到 `system_server`，远程 `dlopen` `libfakeloc.so` 并调用 `doRun()`
4. `doRun()` 用 `InMemoryDexClassLoader` 加载 `inject.dex`，调用 `InjectDex.hookApplication` 安装系统服务 hook
5. **通信**：`ServiceGoRoot` 通过 `RootLocationControl`（`inject/utils/RootLocationControl.java`，本地 socket）与运行在 `system_server` 内的 `MockLocationManagerService` 通信，下发坐标/速度/步频等

### 3.2 Xposed 模式通信

主 app `ServiceGoXposed` 与 Xposed 模块之间通过 `LocationManager.sendExtraCommand("kail", <key>, extras)` 通信：

- `exchangeKey` — 换取会话密钥
- `sendXposedCommand(commandId, extras)` — 下发 `start` / `update_location` / `set_config` / `set_step_enabled` / `set_step_cadence` 等命令

Xposed 侧 `KailCommandHandler` 接收并分发到 `FakeLocState`。

## 4. 注入系统

`inject/` 包的代码不运行在主 app 进程，而是被打包进 `assets/inject.dex`，在 Root 模式下注入到 `system_server`（或目标进程）内执行。

### 4.1 结构

```
inject/
├── fakelocation/
│   ├── InjectDex.java          DEX 入口：hookApplication / init / initZygote
│   ├── hook/
│   │   ├── app/                AppProcessHook, ClientMockHook, RootHideHook, RuntimeAntiDetectionHook
│   │   ├── phone/              PhoneInterfaceManagerHook
│   │   └── system/             PackageManagerServiceHook, TelephonyRegistryHook, WifiServiceHook, WifiServiceCompat
│   ├── service/                MockLocationManagerService, MockWifiManagerService（运行在被注入进程）
│   ├── listener/
│   ├── model/
│   └── aidl/
└── utils/
    ├── RootLocationControl.java        主 app ↔ 注入端的 socket 控制通道
    ├── MockLocationHookManager.java
    ├── MockLocationServiceManager.java
    ├── MockWifiConfigManager.java
    ├── NativeStepHook.java             步频传感器原生 hook 桥接
    ├── AntiDetectionServiceManager.java
    ├── CallingProcessUtils.java
    ├── AesCipherUtils.java
    ├── PackageSignatureVerifier.java
    └── ... 
```

`InjectDex`（`inject/fakelocation/InjectDex.java`）是 DEX 的引导入口，暴露 `hookApplication`、`init`、`initZygote`，由 `libfakeloc.so` 的 `doRun()` 反射调用。它依次实例化 `hook/` 下的各 Hook 并安装到系统服务。

### 4.2 Slim DEX 构建

`app/build.gradle.kts` 在 `androidComponents.onVariants` 中注册 `build<Variant>InjectSlimDex` 任务：

- 依赖 `compile<Variant>JavaWithJavac` / `compile<Variant>Kotlin`
- 从编译产物中筛选类名前缀为 `com/kail/location/inject/` 和 `com/kail/location/lib/lhooker/` 的 `.class`
- 打包为 jar 后用 D8 编译为裸 `.dex`（`--min-api 27`）
- 输出到 `src/main/assets/inject.dex`，并同步到 merged assets

这样注入端用 `InMemoryDexClassLoader(ByteBuffer)` 加载时无需在 `system_server` 内写 OAT，避免 SELinux 拦截与 ART 编译耗时（见 `build.gradle.kts` 内注释）。

## 5. 原生代码

`app/src/main/cpp/` 通过 CMake 构建（`externalNativeBuild.cmake.path`），ABI 为 `arm64-v8a` 与 `armeabi-v7a`。

### 5.1 `root/` — Root 注入器与注入库

| 文件 | 作用 |
| --- | --- |
| `inject.cpp` / `inject64.cpp` | ptrace 注入器（32/64 位），编译为 `kail_inject` 可执行文件。远程 `dlopen` 目标库并调用 `doRun` |
| `libfakeloc_init.cpp` / `libfakeloc_initzygote.cpp` | `libfakeloc.so` 入口，`doRun()` 加载 `inject.dex` 并调用 `InjectDex` |
| `libfakeloc_apphook.cpp` | App 进程级 hook |
| `libantidetect.cpp` | 反检测 |
| `liblhooker.cpp` | LHooker inline hook 封装（配合 `lib/lhooker/`） |
| `libStepSensor.cpp` | 步频传感器原生 hook |
| `elf_hooker.h` / `fakeloc_common.h` | ELF 符号解析与公共定义 |

`app/build.gradle.kts` 在 `mergeNativeLibs` / `stripDebugSymbols` 等任务里把 CMake 产出的 `kail_inject` 重命名为 `libkail_inject.so` 放入 `lib/<abi>/`，使其随 APK 分发。

### 5.2 `native_hook/` — 传感器 hook

| 文件 | 作用 |
| --- | --- |
| `hook.cpp` | 原生 hook 入口（ShadowHook/Dobby） |
| `sensor_simulator.cpp` / `sensor_simulator.h` | 步频/传感器数据生成 |
| `elf_sym_resolver.h` | 运行时 ELF 符号地址解析 |

`root/NativeSensorHook.kt` 是对应的 Kotlin 桥接，`inject/utils/NativeStepHook.java` 在注入端调用。

### 5.3 关键原生依赖

- `com.bytedance.android:shadowhook` — ByteDance ShadowHook（inline hook）
- `io.github.vvb2060.ndk:dobby` — Dobby hook 框架

## 6. 数据层与功能模块

### 6.1 数据层

- **Room**：`data/local/AppDatabase` + `HistoryDao` + `HistoryEntity`，记录历史位置/搜索
- **repositories**：
  - `HistoryRepository` — 历史记录读写
  - `RootMockRepository` — 统一调度模拟启停，按模式分发到四个 `ServiceGo*`
  - `DataBaseHistoryLocation` / `DataBaseHistorySearch` — 查询封装

### 6.2 功能模块（`views/`）

每个子包对应一个 Compose 功能页面 + 一个 ViewModel：

| 子包 | 功能 |
| --- | --- |
| `locationsimulation` | 位置模拟 |
| `routesimulation` | 路线模拟（回放/调速/拖动） |
| `navigationsimulation` | 导航模拟 |
| `cellsimulation` | 基站模拟 |
| `wifisimulation` | Wi-Fi 模拟 |
| `nfcsimulation` | NFC 模拟 |
| `independentsimulation` | 独立模拟 |
| `joystick` | 摇杆浮窗 |
| `locationpicker` | 地图选点 |
| `history` | 历史记录 |
| `sandbox` | 沙盒管理 |
| `xposedsettings` | Xposed 模块设置 |
| `roothide` | Root 隐藏 |
| `settings` | 通用设置 |
| `sponsor` | 赞助 |
| `auth` | 授权 |
| `theme` / `base` / `common` | 主题与基础设施 |

## 7. 应用启动流程

`GoApplication`（`utils/GoApplication.kt`）：

1. `attachBaseContext` — 调用 `BlackBoxCore.doAttachBaseContext` 初始化沙盒（`ClientConfiguration` 禁用 daemon/vpn）
2. `onCreate` — 主进程中：
   - `BlackBoxCore.doCreate()` + `SandboxManager.init` + `SandboxSettingsManager.init`
   - 加载默认 `preferences_main`
   - `FirebaseApp` 初始化
   - `AuthManager.init` + `UsageManager.init`
   - 注册 `ActivityLifecycleCallbacks`
   - 后台线程同步注入日志标记（`RootDeployer.syncInjectLogMarkers`）
   - 设置全局 `UncaughtExceptionHandler` 写崩溃文件
   - `InjectionCrashSentinel.checkAndReport` — 检测上次注入是否导致系统重启
   - 百度地图 SDK 初始化（使用用户配置的 AK）

## 8. 技术栈速览

| 层 | 技术 |
| --- | --- |
| 语言 | Kotlin / Java（注入端）/ C++（原生） |
| UI | Jetpack Compose + Material3 |
| 架构 | MVVM |
| 持久化 | Room |
| 地图 | 百度地图 SDK（搜索/导航依赖 AK） |
| 注入 | ptrace + DexClassLoader / Xposed / ShadowHook + Dobby |
| 沙盒 | NewBlackbox (Bcore) |
| 监控 | Firebase Analytics + Crashlytics |
| 构建 | Gradle KTS + CMake + NDK |

## 相关文档

- [开发指南](development-zh.md)
- [百度地图 API Key 配置](baiduApiKey.md)
- [README](../README.md)
