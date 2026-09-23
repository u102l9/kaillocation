#!/usr/bin/env bash
# 在 GitHub Actions Runner 上安装本项目所需的 Android SDK 组件。
#
# 不使用 android-actions/setup-android：它默认执行 `sdkmanager tools`，
# 而新版 cmdline-tools(16.0) 已移除 tools 包，sdkmanager 会以退出码 1 失败，
# 导致 job 在此之前就挂掉。这里改为自行定位 sdkmanager 并精确安装。
set -euo pipefail

SDKROOT="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-/usr/local/lib/android/sdk}}"
echo "SDK 根目录: $SDKROOT"

echo "ANDROID_HOME=$SDKROOT" >> "$GITHUB_ENV"
echo "ANDROID_SDK_ROOT=$SDKROOT" >> "$GITHUB_ENV"
echo "$SDKROOT/platform-tools" >> "$GITHUB_PATH"

# 预先写入 license 摘要，避免 sdkmanager 交互式提问卡住（CI 无 TTY）
mkdir -p "$SDKROOT/licenses"
printf '\n8933bad161af4178b1185d1a37f9d1cabe7029c\n24333f8a63b6825ea9c5514f83c2829b004d1fee\n' \
  > "$SDKROOT/licenses/android-sdk-license"
printf '\n84831b9409646a918e30573bab4c9c91346d8abd\n' \
  > "$SDKROOT/licenses/android-sdk-preview-license"

# 定位 sdkmanager：优先 latest，其次任意已安装版本
SM=""
if [ -x "$SDKROOT/cmdline-tools/latest/bin/sdkmanager" ]; then
  SM="$SDKROOT/cmdline-tools/latest/bin/sdkmanager"
else
  for cand in "$SDKROOT"/cmdline-tools/*/bin/sdkmanager; do
    [ -x "$cand" ] && SM="$cand" && break
  done
fi

# 都没有则下载一份 cmdline-tools
if [ -z "$SM" ]; then
  echo "未找到可用的 sdkmanager，下载 cmdline-tools"
  cd /tmp
  curl -fsSLO https://dl.google.com/android/repository/commandlinetools-linux-12266719_latest.zip
  rm -rf /tmp/cmdline-tools-tmp
  mkdir -p /tmp/cmdline-tools-tmp "$SDKROOT/cmdline-tools/latest"
  unzip -q -o commandlinetools-linux-12266719_latest.zip -d /tmp/cmdline-tools-tmp
  cp -r /tmp/cmdline-tools-tmp/cmdline-tools/. "$SDKROOT/cmdline-tools/latest/"
  SM="$SDKROOT/cmdline-tools/latest/bin/sdkmanager"
fi
chmod +x "$SM" 2>/dev/null || true
echo "使用 sdkmanager: $SM"

# 再确认一遍 license（失败不影响，摘要文件已写入）
yes 2>/dev/null | "$SM" --licenses >/dev/null 2>&1 || true

echo "== 安装 platform-tools / platform-36 / build-tools / cmake =="
"$SM" --install "platform-tools" "platforms;android-36" "build-tools;36.0.0" "cmake;3.22.1"

echo "== 安装 NDK =="
# NewBlackbox/Bcore 指定 ndkVersion 29.0.13846066；若该版本下架则退而用 29.x 最新
if "$SM" --install "ndk;29.0.13846066"; then
  echo "已安装 ndk;29.0.13846066"
else
  NDK_PKG="$("$SM" --list 2>/dev/null | grep -oE 'ndk;29\.[0-9.]+' | sort -uV | tail -1 || true)"
  if [ -n "$NDK_PKG" ]; then
    echo "29.0.13846066 不可用，改装 $NDK_PKG"
    "$SM" --install "$NDK_PKG"
  else
    echo "::warning::未找到 NDK 29.x，将依赖 AGP 自动下载"
  fi
fi

echo "== 已安装组件 =="
"$SM" --list_installed || true

# 让 AGP 明确知道 SDK 位置
echo "sdk.dir=$SDKROOT" > "${LOCAL_PROPERTIES_PATH:-local.properties}"
echo "已写入 ${LOCAL_PROPERTIES_PATH:-local.properties}"
