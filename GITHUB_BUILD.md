# GitHub Actions 自动打包说明

## 零、第一次跑失败的根因（已修复）

失败日志：`sdkmanager` 退出码 1，job 13 秒就结束了，Gradle 根本没启动。

```
[command]/usr/local/lib/android/sdk/cmdline-tools/16.0/bin/sdkmanager tools
Warning: Failed to find package 'tools'
Error: The process '.../sdkmanager' failed with exit code 1
```

原因：`android-actions/setup-android@v3` 的默认 `packages` 是 `tools platform-tools`，
它一定去执行 `sdkmanager tools`。但 **新版 Android cmdline-tools（16.0）已经移除 `tools` 包**，
于是 sdkmanager 找不到包 → 非零退出 → 整个 job 挂掉。

修复：**弃用 `setup-android`**，改为脚本 `.github/scripts/setup-android-sdk.sh`
自行定位 `sdkmanager` 并精确安装 `platform-tools` / `platforms;android-36` /
`build-tools;36.0.0` / `cmake;3.22.1` / `ndk;29.0.13846066`，
同时预先写入 license 摘要文件避免交互式提问卡住。

> 顺带说明：SDK 安装逻辑放在独立 `.sh` 里、workflow 只保留极简 YAML，
> 是为了避免大段 shell 嵌进 YAML 块标量带来的缩进 / 引号风险（GitHub 的
> workflow 校验器对这类写法报错时，行号往往指向块标量附近，很难排查）。

## 一、本次新增 / 修改的文件

| 文件 | 类型 | 说明 |
| --- | --- | --- |
| `.github/workflows/build-apk.yml` | 新增 | 主 App 构建（push / PR / 手动 / tag 触发），已修 SDK 安装 |
| `.github/workflows/build-xposed.yml` | 新增 | Xposed 模块构建，手动触发 |
| `.github/scripts/setup-android-sdk.sh` | 新增 | SDK / NDK / CMake 安装脚本，workflow 只负责调用 |
| `app/kail-release.jks` | 新增 | 内置自签发布密钥 |
| `KailLocationXposed/app/kail-release.jks` | 新增 | 同一套密钥，供 Xposed 模块签名 |
| `app/build.gradle.kts` | 修改 | 新增 `signingConfigs`，release / debug 都用它签名 |
| `KailLocationXposed/app/build.gradle.kts` | 修改 | 同上 |
| `.gitignore` | 修改 | 给两个 jks 加例外，否则 `*.jks` 规则会让 CI checkout 拿不到密钥 |
| `gradle.properties` | 修改 | 追加签名口令；`org.gradle.jvmargs` 提到 4096m |
| `KailLocationXposed/gradle.properties` | 修改 | 追加同样的签名口令 |
| `settings.gradle.kts` | 修改 | 仓库顺序改为 google / mavenCentral 优先，阿里云镜像降为兜底（Runner 访问官方源更快） |
| `NewBlackbox/Bcore/build.gradle` | 修改 | `ndkVersion` 从 `externalNativeBuild` 块内移到 `android{}` 顶层（官方 DSL 位置） |
| `GITHUB_BUILD.md` | 新增 | 本说明 |

除上表外没有改动任何源码。

## 二、版本矩阵（全部沿用仓库原值）

| 组件 | 版本 | 来源 |
| --- | --- | --- |
| Gradle | 8.13 | `gradle/wrapper/gradle-wrapper.properties` |
| AGP | 8.13.2 | `gradle/libs.versions.toml` |
| Kotlin | 2.2.0 | 同上 |
| KSP | 2.3.0 | 根 `build.gradle.kts` / Xposed 的 toml |
| JDK | 21 | `NewBlackbox/Bcore` 的 `sourceCompatibility = JavaVersion.VERSION_21` |
| compileSdk / targetSdk | 36 | 根 `build.gradle.kts` 的 `extra` |
| minSdk | 27 | 同上 |
| NDK | 29.0.13846066 | `NewBlackbox/Bcore/build.gradle` |
| CMake | 3.22.1 | `app` 的 prefab（dobby）+ CMake 构建所需 |
| Build Tools | 36.0.0 | 与 compileSdk 36 配套 |

## 三、怎么用

1. 把本压缩包按目录结构覆盖到仓库根目录，提交并 Push。
2. 打开仓库 **Actions** 页 →「构建 Kail Location APK」：
   - push / PR：Debug + Release 一起编
   - 手动：Actions → Run workflow，可选 `debug` / `release` / `both`
   - 打 tag（`git tag v1.7.2 && git push --tags`）：额外自动创建 Release 并附 APK
3. 结束后在任务页底部 **Artifacts** 下载 `kail-location-apk-<编号>`，APK 已签名可直接装。

## 四、签名：已内置，开箱即用

`app/kail-release.jks`（与 Xposed 模块那份是同一套）已随仓库提交，
`app/build.gradle.kts` 已配置 `signingConfigs` 并在 release / debug 上生效，
本地与 CI 都无需额外配置，产物直接可安装。

| 项 | 值 |
| --- | --- |
| keystore | `app/kail-release.jks` |
| keyAlias | `kail` |
| storePassword / keyPassword | `kail2026` |
| 有效期 | 30 年（10950 天） |
| SHA-256 | `76:7F:94:24:11:9A:36:74:B2:2A:07:AE:D9:F2:4B:08:B4:43:CA:4F:C9:A9:20:04:F1:E6:44:78:EE:66:C0:BF` |

换自己的密钥：

```bash
keytool -genkeypair -v -keystore app/kail-release.jks -keyalg RSA -keysize 2048 \
  -validity 10950 -alias kail -storepass 你的口令 -keypass 你的口令 \
  -dname "CN=..., OU=..., O=..., C=CN"
```

再同步改 `gradle.properties` 里的 `KAIL_STORE_PASSWORD / KAIL_KEY_ALIAS / KAIL_KEY_PASSWORD`
（临时覆盖：`./gradlew :app:assembleRelease -PKAIL_STORE_PASSWORD=xxx`）。

> ⚠️ 私钥入库意味着任何拿到仓库的人都能以你的名义签名，仅适合自用 / 测试分发。

## 五、本地构建

```bash
./gradlew :app:assembleDebug      # 调试包（已签名）
./gradlew :app:assembleRelease    # 发布包（已签名）
cd KailLocationXposed && ./gradlew :app:assembleRelease   # Xposed 模块
```

本地需要：`platforms;android-36`、`build-tools;36.0.0`、`ndk;29.0.13846066`、`cmake;3.22.1`，
以及 `local.properties` 里的 `sdk.dir`。

## 六、如果还失败，按这个顺序排查

这些都是静态分析结论（沙盒无 Android SDK、拉不到依赖，未做真机编译验证）：

1. **Kotlin 2.2.0 + KSP 2.3.0 不匹配**：KSP 官方要求 KGP 2.2.10 起。若报
   `KSP requires Kotlin ... / bad plugin version`，二选一：
   - 把 `gradle/libs.versions.toml` 的 `kotlin` 改成 `2.2.10`
   - 或把根 `build.gradle.kts` 的 KSP 改成 `2.2.10-2.0.2`
2. **Compose 版本不匹配**：仓库用 Kotlin 2.2.0 + Compose BOM 2024.09.00，是配套组合，
   单独升 Kotlin 会报 compose compiler 与 runtime 版本冲突，要升就一起升。
3. **NDK 版本不可用**：脚本会先试 `ndk;29.0.13846066`，装不上则自动选 29.x 最新；
   若都失败会在日志里打 warning，此时需要改 Bcore 的 `ndkVersion`。
4. **`Bcore` 老 DSL**：该模块用 Groovy + `lintOptions` / `aidlPackagedList`，AGP 8.13 仍支持，
   **AGP 9 才移除，不要升级到 AGP 9**。
5. **Firebase**：`app/google-services.json` 已入库，google-services 4.4.4 + Crashlytics 3.0.7 可正常工作；
   不想让 CI 碰 Firebase 就删掉 `app/build.gradle.kts` 里这两个插件与 `firebase-*` 依赖。
6. **百度地图**：`app/libs` 内已有 jar 与预编译 so，`AndroidManifest` 里的 AK 需自行确认有效，
   否则能编出包但地图不可用（不影响编译）。
7. **inject.dex**：`app/build.gradle.kts` 会用 D8 生成精简 dex；CI 上若找不到 `r8.jar`
   会打 warning 并跳过，此时沿用仓库里已有的 `app/src/main/assets/inject.dex`，构建不会失败。
8. **耗时**：首次要拉 Gradle + AGP + 全部依赖 + NDK + CMake，约 10–25 分钟；
   已用 `gradle/actions/setup-gradle` 缓存，第二次快很多。私有仓库每月 2000 分钟额度，别反复全量重编。
9. **`setup-java@v5` 若报参数错误**：改回 `actions/setup-java@v4` 即可（v4 只是弃用、仍能跑）。

## 七、Runner 环境的三条提示（都不影响打包）

CI 日志里常见的三条提示，逐条说明：

| 提示 | 是否影响构建 | 处理 |
| --- | --- | --- |
| `Node.js 20 is deprecated ... forced to run on Node.js 24` | **不影响** | Runner 已自动改用 Node 24 执行这些 action，功能正常，只是提醒 |
| `setup-java v4 is deprecated ... migrate to v5` | **目前不影响** | 已升级到 `setup-java@v5` |
| `ubuntu-latest will migrate to Ubuntu 26.04` | **现在不影响，11 月后有风险** | 已固定为 `ubuntu-24.04` |

细节：

- **Node 20 → Node 24**：GitHub 已把 Node 20 的移除日期定在 2026-09-23，Runner 从 2026-06-16 起默认用 Node 24。
  旧版 action 会被"强制在 Node 24 上运行"，属于向前兼容处理，不会中断构建。
  真正需要担心的是某个 action 在 Node 24 上存在真实不兼容——目前这四个（checkout / setup-java /
  upload-artifact / setup-gradle）都是常见 action，未见异常。
- **setup-java v4 弃用**：v4 已不再接收更新，所以升级到 v5 是官方建议方向，已改。
- **ubuntu-latest → Ubuntu 26.04**：官方计划 2026-10-19 起分批迁移、11-19 完成。
  Android 构建牵涉 NDK / CMake / 系统库，跨大版本 Ubuntu 有可能踩到兼容问题，
  所以这里直接写死 `ubuntu-24.04`，环境可预期，也顺带消掉这条提示。

> 说明：`actions/checkout`、`actions/upload-artifact`、`gradle/actions` 仍保持 `@v4`。
> 它们只是 Node 运行时提示，功能正常；我没有改成更高主版本号，是因为未能可靠确认其最新主版本，
> 写错会让 workflow 直接解析失败。想升级的话，在仓库 Actions 页面或各 action 的 releases 页
> 确认最新 tag 后自行替换即可（改法就是 `uses:` 后面那一行）。
