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

- 当分包、本地/在线模组解析失败时，显示成一个 “解析失败” 的卡片
- 分包/MOD 更新功能
- 关闭 Mod 图标的像素过滤，以正确显示低分辨率图标
- 不同源的在线模组显示为不同的卡片样式，并拥有各自的功能
- 实现文件夹收藏功能
- 状态栏颜色随主题改变
- 检测是否安装了 Horizon 和 Minecraft，没有则推荐安装
- 使资讯支持 Markdown 格式

### Code

- 将 FileSelector 改为 ViewModel 方案，优化 FileSelector 性能
- 重写 OnlineMod，消除 legacyservice.WebAPI 依赖

## 已知 BUGs

- 登录界面齿轮无法转动
- 登录界面动画异常
- 无进度的进度指示器动画异常