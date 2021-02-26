# Horizon Manager

使用 Jetpack Compose 构建

## 模块

### app

应用主体

- `legacyservice` - 过时的 Service，面条代码，但还在使用。应当完善 `service` 模块，并用其代替。
- `ui`
- `utils`

### service

提供功能支持

- `mod` - 用于管理设备中已安装的模组
- `pack` - 用于管理设备中已安装的分包
- `utils` - 其他工具函数

### webapi

网络服务

- `iccn` - 用于访问 ICCN 论坛，目前仅支持注册与登录
- `mgrinfo` - 用于获取
- `news` - 用于获取资讯
- `pack` - 用于获取分包信息
- `mod` - 用于获取在线模组信息

## TODO

无

## 已知 BUGs

- 登录界面齿轮无法转动
- 登录界面动画异常
- 无进度的进度指示器动画异常