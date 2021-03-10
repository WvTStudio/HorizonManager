# Horizon Manager

使用 Jetpack Compose 构建

## 模块

### app

应用主体

- `legacyservice` - 过时的 Service，面条代码，但还在使用。应当完善 `service` 模块，并用其代替。
- `ui` - 应用的 UI，包括 Jetpack Compose UI 和 Activity
- `utils` - 部分过时的工具
- `HorizonManagerApplication.kt` 提供依赖

### service

提供功能支持

- `mod` - 用于管理设备中已安装的模组
- `pack` - 用于管理设备中已安装的分包
- `utils` - 其他工具函数

### webapi

网络服务

- `iccn` - 用于访问 ICCN 论坛，目前仅支持注册与登录
- `mgrinfo` - 用于获取管理器的更新信息、公告、群组等
- `news` - 用于获取资讯
- `hzpack` - 用于获取分包
- `mod` - 用于获取模组
- `level` - 用于管理存档
- `respack` - 用于管理资源包

## TODO

### 功能

- P1 使资讯支持 Markdown 格式
- P1 各分包和 Minecraft 本身的资源包管理功能
- P2 在线资源的 Mod 详情页面
- P3 支持编辑 Mod 的设置
- P3 分包/MOD 更新功能
- P3 在线模组每个卡片显示下载进度和安装状态
- P4 当分包、本地/在线模组解析失败时，显示成一个 “解析失败” 的卡片

### Code

- P1 完善 service 模块，并代替 legacyservice 包
- P1 将所有与 package manifest 关联的用例换成 PackageManifest, mod.info 用例换成 ModInfo
- P4 使用 kotlinx.serialization 替代 org.json

## 已知 BUGs

- AnimatedVisibility 的动画异常
- FileSelector 当快速左滑文件夹时，即使没有达到临界点也会被判定为触发成功