# DemoVersion

**Android System Home / Digital Signage Demo**  
**Android 系统级 Home（主屏幕）与数字展示终端示例**

> 一个面向电视盒子、商用显示终端和无人值守设备的 Android（安卓）系统应用示例，覆盖默认 Home（主屏幕）替换、内容远程更新、MQTT（消息队列遥测传输协议）实时事件、媒体预加载、OTA（空中升级）和异常恢复。

---

## 1. Project Overview（项目简介）

DemoVersion 是一个系统级 Android（安卓）展示应用。应用注册为默认 Home（主屏幕），开机后直接进入业务展示界面，并通过服务端配置动态更新图片、视频和互动页面。

项目重点不是单一页面展示，而是完整的内容交付链路：

- 服务端配置拉取；
- 图片与视频资源下载；
- 文件完整性校验和失败重试；
- 本地缓存与视频预加载；
- MQTT（消息队列遥测传输协议）实时消息；
- OTA（空中升级）；
- 异常捕获、日志留存和自动恢复；
- 系统级 Home（主屏幕）与开机自启动。

**English summary:** A Java-based Android system-home application for unattended displays, featuring remote content delivery, media preloading, MQTT events, OTA updates and crash recovery.

---

## 2. Features（功能特性）

| Module（模块） | Capability（能力） |
| --- | --- |
| System Home（系统主屏幕） | 注册 `android.intent.category.HOME`，作为默认 Launcher（桌面）使用 |
| Boot Startup（开机启动） | 接收 `BOOT_COMPLETED` 广播并启动展示界面 |
| Content Update（内容更新） | 通过 Retrofit（网络请求库）和 OkHttp（网络库）获取服务端配置 |
| Media Playback（媒体播放） | 支持图片、视频轮播和多页面展示 |
| Multi-file Download（多文件下载） | 支持并发下载、进度回调、重复任务控制和本地保存 |
| Integrity Check（完整性校验） | 使用 MD5（消息摘要算法）校验资源，失败后最多重试 5 次 |
| MQTT Events（实时消息） | 独立进程运行 MQTT 服务，支持设备主题订阅、断线重连和消息去重 |
| Video Preload（视频预加载） | 基于 VideoCache（视频缓存组件）预取媒体，支持暂停、恢复和取消 |
| OTA（空中升级） | 服务端下发版本信息，客户端下载、校验并安装 APK（安卓应用安装包） |
| Crash Recovery（异常恢复） | 集成 Bugly（腾讯异常监控平台），保存本地日志并重启应用 |
| QR Interaction（扫码互动） | 使用 ZXing（二维码库）生成二维码，支持互动和活动结束页面 |
| Daemon Service（守护服务） | 使用前台服务和独立进程维持关键服务运行 |

---

## 3. Architecture（项目结构）

```text
DemoVersion/
├── app/
│   ├── libs/
│   │   └── YF_RK356x_API.jar          # Vendor-specific hardware API（厂商硬件接口）
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/mssm/demoversion/
│       │   ├── activity/               # UI and interaction pages（界面与交互页面）
│       │   ├── base/                   # Application initialization（应用初始化）
│       │   ├── download/               # Multi-file download and download management（下载管理）
│       │   ├── exception/              # Global crash handling（全局异常处理）
│       │   ├── http/                   # Retrofit/OkHttp network requests（网络请求）
│       │   ├── model/                  # API, MQTT and OTA models（数据模型）
│       │   ├── presenter/              # Callback interfaces（回调接口）
│       │   ├── receiver/               # Boot and alarm receivers（开机与定时广播）
│       │   ├── services/               # MQTT and daemon services（服务）
│       │   ├── util/                   # Cache, logging and utility classes（工具类）
│       │   └── view/                   # Custom views and player wrappers（自定义视图）
│       └── res/                        # Layouts, media resources and configuration（资源文件）
├── build.gradle
├── settings.gradle
└── gradle/wrapper/
```

### Process Model（进程模型）

- Main process（主进程）：展示界面、网络请求、下载和 OTA；
- `:msmqtt_service`：MQTT（消息队列遥测传输协议）消息接收与命令分发；
- `:daemon_service`：前台守护服务、定时刷新和资源维护。

---

## 4. Runtime Flow（运行流程）

### 4.1 Boot and Home（开机与主屏幕）

1. 设备开机后发送 `BOOT_COMPLETED`；
2. `BootReceiver` 启动 `AdvertisePlayActivity`；
3. `AdvertisePlayActivity` 通过 `android.intent.category.HOME` 作为默认桌面；
4. `DaemonService` 以前台服务方式维持运行。

### 4.2 Content Delivery（内容更新）

1. 定时从服务端获取展示配置；
2. 解析图片、视频及页面信息；
3. 下载新增或变更的资源；
4. 使用 MD5（消息摘要算法）校验文件；
5. 校验失败时重试，成功后刷新展示页面；
6. 已有文件避免重复下载，正在进行的任务不会被重复创建。

### 4.3 MQTT Events（实时事件）

1. MQTT 服务按设备 SN（序列号）订阅主题；
2. 接收服务端下发的活动命令；
3. 对短时间重复消息进行抑制；
4. 下载互动页面所需资源；
5. 打开广告、扫码互动或活动结束页面；
6. 连接断开后每 10 秒尝试重连。

### 4.4 OTA（空中升级）

1. 服务端返回版本号、下载地址和 MD5（消息摘要算法）；
2. 客户端下载 APK（安卓应用安装包）；
3. 校验通过后触发安装；
4. 通过系统权限完成应用更新。

### 4.5 Crash Recovery（异常恢复）

1. 自定义 `UncaughtException`（未捕获异常）处理器接管异常；
2. 将堆栈信息写入本地日志；
3. 上传 Bugly（腾讯异常监控平台）；
4. 重启展示应用，减少无人值守设备长时间黑屏的风险。

---

## 5. Tech Stack（技术栈）

- Language（语言）：Java 8；
- Android SDK：minSdk 29，compileSdk/targetSdk 34；
- Build（构建）：Android Gradle Plugin 8.0.2，Gradle 8.0.2；
- Network（网络）：Retrofit 2.9.0、OkHttp 4.9.2、Gson；
- Messaging（消息）：Eclipse Paho MQTT 1.2.5；
- Media（媒体）：DKPlayer、VideoCache、IjkPlayer；
- Image（图片）：Glide；
- Storage（存储）：MMKV、SharedPreferences、本地文件；
- QR Code（二维码）：ZXing；
- Monitoring（监控）：Bugly；
- Hardware（硬件）：`YF_RK356x_API.jar`。

---

## 6. Build and Run（构建与运行）

### Requirements（环境要求）

- Android Studio（建议 Hedgehog 或更高版本）；
- JDK 17（Android Gradle Plugin 8.x 需要）；
- Android SDK 34；
- 可访问 Google Maven、Maven Central 和 JitPack 的网络环境。

### Build（构建）

```bash
./gradlew assembleDebug
```

Windows（Windows 系统）：

```powershell
.\gradlew.bat assembleDebug
```

### Run（运行）

该项目使用系统级能力，完整功能需要：

- 支持 RK356x 的目标设备；
- 厂商提供的 `YF_RK356x_API.jar`；
- 系统签名；
- 相应的系统权限；
- 可烧录或可安装系统应用的设备环境。

> 普通模拟器或普通零售手机只能验证部分界面逻辑，无法完整验证默认 Launcher（桌面）、开机广播、系统权限和厂商硬件接口。

---

## 7. Configuration（配置说明）

运行前需要根据实际环境调整：

- 服务端 API（应用程序接口）地址；
- MQTT（消息队列遥测传输协议）服务器地址、账号和主题；
- 资源下载目录；
- OTA（空中升级）版本接口；
- 设备 SN（序列号）生成规则；
- 系统签名和平台权限。

> 当前代码中的连接信息和签名配置属于历史演示配置，不应直接用于生产环境。

---

## 8. Reliability Design（可靠性设计）

- HTTP（超文本传输协议）请求与文件下载分离；
- 多文件下载使用独立任务管理；
- 通过 MD5（消息摘要算法）避免损坏文件进入播放流程；
- 最多 5 次重试，失败后清理无效文件；
- 下载任务去重，避免重复请求；
- MQTT（消息队列遥测传输协议）断线重连；
- 短时间重复消息抑制；
- 前台服务和守护进程；
- 全局异常捕获、日志留存和自动重启；
- 视频预加载减少播放等待。

---

## 9. Known Limitations（已知限制）

- 当前实现以 Java（Java 语言）为主，没有 Kotlin（Kotlin 语言）源码；
- 依赖特定硬件平台的 `YF_RK356x_API.jar`；
- 服务端地址、MQTT（消息队列遥测传输协议）配置和主题规则硬编码在源码中；
- 系统签名文件和构建口令需要在公开仓库中移除或替换；
- 单元测试仅为 Android Studio 模板，测试覆盖不足；
- 未配置持续集成（CI）；
- 项目为历史演示版本，不保证与当前 Android 版本或最新依赖兼容。

---

## 10. Security Notice（安全说明）

本仓库包含演示性质的系统签名文件、构建配置和测试连接信息。用于公开作品集或生产环境前，应：

1. 删除签名文件、密钥和口令；
2. 将连接信息迁移到本地配置或环境变量；
3. 轮换已经公开的测试凭据；
4. 清理 Git 历史中的敏感信息；
5. 确认公司项目代码、业务资源和媒体素材的使用授权；
6. 不要在生产环境中复用演示签名。

---

## 11. Project Status（项目状态）

Historical demo（历史演示项目），最后提交时间为 2024-01-12。  
当前主要用于展示 Android（安卓）系统应用、内容分发、实时消息、媒体缓存和异常恢复的实现思路。
