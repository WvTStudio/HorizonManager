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
- `pack` - 用于获取分包
- `mod` - 用于获取模组

## TODO

### 功能

- 在线资源的 Mod 详情页面
- 支持编辑 Mod 的设置
- 当分包、本地/在线模组解析失败时，显示成一个 “解析失败” 的卡片
- 分包/MOD 更新功能
- 状态栏颜色随主题改变
- 检测是否安装了 Horizon 和 Minecraft，没有则推荐安装
- 使资讯支持 Markdown 格式
- 在线模组每个卡片显示下载进度和安装状态

### Code

- 使用 kotlinx.serialization 替代 org.json
- 完善 service 模块，并代替 legacyservice 包

## 已知 BUGs

- 登录界面齿轮无法转动
- 登录界面动画异常
- 无进度的进度指示器动画异常