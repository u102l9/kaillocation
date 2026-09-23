# 配置 OpenCellID API Key 教程

> OpenCellID（Open Database of Cell Towers）是全球最大的**开放式基站数据库**。Kail Location 的「基站模拟 → 从网络获取基站」功能，就是用当前位置坐标查询 OpenCellID 数据库中该区域附近的基站，一键填入模拟清单。
>
> 全流程：**官网注册 → 邮箱激活 → Dashboard 拿到 Access Token → 填进 App 设置**，全程免费，约 3 分钟。

---

## 第一步：打开官网并点击 SIGN UP

1. 浏览器打开 <https://opencellid.org。
2. 点击右上角 **SIGN UP** 进入注册页
---

## 第二步：填写注册表单

注册页标题 **Create your account**，共 5 步：

| # | 项目 | 内容 |
| --- | --- | --- |
| 1 | **Work email** | 填写能正常收件的邮箱（建议 Gmail，如 QQ 邮箱也可；示例 `kaillali23143@gmail.com`），划掉原来默认的绿色提示后输入即可 |
| 2 | **Full name** | 随便填个名字即可（示例 `kail`） |
| 3 | **Use Case** | 下拉选 **Personal**（个人用途） |
| 4 | **reCAPTCHA 人机身份验证** | 勾选「进行人机身份验证」复选框（有时会弹图片九宫格，按提示选图） |
| 5 | **Sign Up** | 点橙色 **Sign Up** 按钮提交 |

> 填错邮箱会导致收不到激活邮件，务仔细检查。

---

## 第三步：确认注册成功并收激活邮件

1. 提交后页面显示 **Thank you for signing up!**，并提示"已向你的邮箱发送邮件，若几分钟内没收到请检查垃圾箱或联系 support"（对应截图 `opencellid3.png`）。
2. 打开邮箱（示例 Gmail），看到主题为 **「感谢您注册 OpenCelliD」** 的邮件，发件人是 `OpenCelliD Project <hello@opencellid.org>`（对应截图 `opencellid4.png`）。

---

## 第四步：通过邮件链接进入 Dashboard

1. 邮件正文："您已成功注册 OpenCelliD 项目……要完成注册，请点击以下链接（链接仅在几分钟内有效）"。
2. **点击邮件里的 magic auth 链接**（形如 `https://my.opencellid.org/dashboard/magicauth?i=...`），即完成激活并自动登录 Dashboard：
   
3. 登录后页面顶部显示 **Hi, <你的昵称>!**（示例 `Hi kail!`），左侧菜单：**Access Tokens / Analytics / API Documentation / Account Details**（对应截图 `opencellid5.png`）。

---

## 第五步：Access Tokens 查看并复制 API Key

1. 确保左侧选中 **Access Tokens**（默认即此页），右侧标题 **Manage your Access Tokens**，说明"需要 Access Tokens 访问 API，建议每个应用/网站单独一个 token"。
2. 若列表里没有 token：点右上角橙色 **Create Access Token** 新建一个。
3. 列表表格中（Label / Access Token / Created On / View 列），点击 **「Show Token」** 链接——token 默认隐藏，点击后显示明文字符串（形如 `aG9uc...` 长串）。
4. **复制整串字符**（注意：区分大小写，别复制到前后空格），这就是 **OpenCellID API Key**。

> 提示：token 不会明文全显示可选中时直接拖动选中复制；建议存到备忘录备用（Token 页面也有 View 可查看详情）。

---

## 第六步：填写到 Kail Location

1. 打开应用 → 侧边栏 ☰ → **设置** → **设置**；
2. 向下滚动到 **「其他」** 分组，找到 **「OpenCellID API Key」** 项（当前显示"未设置"，描述"用于通过经纬度查询周边基站，需先在 opencellid.org 注册获取"）（对应截图 `opencellid6.png`，就是箭头指的那一行）；
3. 点击该项 → 弹窗中输入/粘贴刚才复制的 API Key → 点 **确定**；
4. 返回该行应显示你粘贴的 key（不再是"未设置"）。

---

## 第七步：验证是否生效

1. 侧边栏 ☰ → **运行模式** 切换为 **Root 模式**；
2. 进入 **基站模拟** 页 → 点 **「从网络获取」** 按钮；
3. 弹出「扫描到的基站」列表 → 点 **全部添加** → 基站加入清单，配置成功；
4. 若提示 `请先在设置中配置 OpenCellID API Key` → 回到第六步检查（常见：复制带空格）；
5. 若提示 `未查询到基站数据，该区域可能无覆盖或API限制` → 换个大城市市中心位置再试（查询用的是真实定位坐标）。

---

## 常见问题排查

| 问题 | 解决办法 |
| --- | --- |
| 网站能打开但注册按钮没反应 | 换 Google Chrome 无痕模式；换网络（用手机热点）；社区求助 community.opencellid.org |
| 收不到激活邮件 | 等几分钟；查垃圾邮件（Spam）；确认邮箱拼写；重新注册换邮箱 |
| 激活链接过期/点了没反应 | 手动访问 https://my.opencellid.org/dashboard 用邮箱登录（免密邮件链接方式） |
| Dashboard 找不到 token | 确认左侧选的是 **Access Tokens** 页；无 token 点 **Create Access Token** |
| 看过 Show Token 但没显示 | 重新下滑/刷新页面再点一次；部分浏览器拦截弹层，换 Chrome |
| App 粘贴不了/多个空格 | 粘贴到备忘录整理后再复制一遍，确保首尾无空格（Key 不含空格） |
| 配置后请求仍失败 | 本机访问 opencellid.org 不稳定；稍后或换网络再试（App 内可开「启用日志」看详细日志） |

---
