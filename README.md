# Horizon Manager

使用 Jetpack Compose 构建

## 模块

### app

应用主体

- `ui` - 应用的 UI，包括 Jetpack Compose UI 和 Activity
- `utils` - 部分跟 Context 直接绑定的工具，部分用来连接 service 模块和 webapi 模块的工具
- `HorizonManagerApplication.kt` 提供依赖

### service

提供功能支持

- `hzpack` - 用于描述和管理 Horizon 分包 
- `mod` - 用于描述和管理模组
- `level` 用于描述和管理存档
- `respack` - 用于描述和管理资源包
- `utils` - 其他工具

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

### 界面

- P4 FileSelector 收藏功能的更多细节
  - P1 已固定的文件夹解除收藏时动画移除

### 功能

- P1 各分包和 Minecraft 本身的资源包管理功能
    - P1 支持查看存档的基本信息
    - P2 支持浏览存档使用的行为包和资源包
    - P2 支持调整设置，导出存档
- P1 查看本地 Mod 详情
  - P1 更新功能
  - P1 支持编辑 Mod 的设置
  - P3 如果是在线安装的模组，可以跳转到对应在线页面
  - P3 如果是本地安装的模组，可以搜索是否存在在线模组，并跳转到对应在线页面
- P1 分包/MOD 更新功能
- P1 支持分包的打包导出功能
- P3 在线资源的 Mod 详情页面
- P3 在线模组每个卡片显示下载进度和安装状态
- P5 当分包、本地/在线模组解析失败时，显示成一个 “解析失败” 的卡片

### Code

- P2 使用 kotlinx.serialization 替代 org.json
- P3 优化 FileSelector 的代码
- P3 重写 Custom Theme 的 UI 和 Controller 代码
- P3 查看所有组件的 ContentColor CompositionProvider 层级，防止出错
- P4 使 webapi 和 service 模块不依赖于 android

## 已知 BUGs

- FileSelector 当快速左滑文件夹时，即使没有达到临界点也会被判定为触发成功
- 切换 AppBar 的颜色不是自然过渡，并且会先闪烁成 primaryColor 再切换成 surface Color
