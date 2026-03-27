# 照片管理系统

一个基于Spring Boot和Vue 3的专业照片管理系统，支持多种图像格式（包括RAW格式）、智能标签和社交功能。

## 🚀 项目概述

照片管理系统是一个现代化的照片管理平台，采用前后端分离架构，支持多种图像格式（包括专业RAW格式），提供智能化的照片管理和社交功能。

## ✨ 功能特性

### 当前阶段（基础功能）
- ✅ 用户注册与登录（JWT认证）
- ✅ 个性化表单验证提示
- ✅ 图片上传（支持JPEG、PNG、RAW格式）
- ✅ 图片存储与配额管理
- ✅ RAW格式预览图生成
- ✅ EXIF元数据提取与展示
- ✅ 公开/私有权限控制

### 后续计划
- AI智能标签识别（ResNet）
- 自然语言搜索（CLIP模型）
- 自动相册聚类
- 社交互动（图片论坛、约拍）
- 高级权限管理（链接分享、好友可见）

## 🛠️ 技术栈

### 后端
- **框架**: Spring Boot 3.x
- **语言**: Java 17+
- **数据库**: MySQL 8.0
- **认证**: JWT Token
- **依赖管理**: Maven
- **图片处理**: Thumbnailator, metadata-extractor

### 前端
- **框架**: Vue 3
- **构建工具**: Vite
- **状态管理**: Pinia
- **路由**: Vue Router
- **HTTP客户端**: Axios
- **样式**: Sass

## 📁 项目结构

```
photo-management-system/
├── backend/                    # Spring Boot后端
│   ├── src/main/java/com/photo/
│   │   ├── controller/         # 控制器层
│   │   ├── service/           # 业务逻辑层
│   │   ├── entity/            # 实体类
│   │   ├── repository/        # 数据访问层
│   │   ├── dto/               # 数据传输对象
│   │   ├── config/            # 配置类
│   │   └── util/              # 工具类
│   ├── src/main/resources/
│   │   ├── application.yml    # 配置文件
│   │   └── static/            # 静态资源
│   └── pom.xml                # 依赖管理
└── frontend/                   # Vue 3前端
    ├── src/
    │   ├── components/        # 公共组件
    │   ├── views/             # 页面组件
    │   ├── composables/       # 组合函数
    │   ├── services/          # API服务
    │   ├── stores/            # Pinia状态管理
    │   ├── router/            # 路由配置
    │   └── assets/            # 静态资源
    ├── package.json
    └── vite.config.js
```

## 🚀 快速开始

### 环境要求
- Java 17+
- Node.js 16+
- MySQL 8.0+

### 后端设置

1. **数据库配置**
   - 创建MySQL数据库 `photo_management`
   - 执行 `database_init.sql` 脚本创建表结构

2. **配置文件修改**
   ```yaml
   # 修改 backend/src/main/resources/application.yml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/photo_management
       username: your_mysql_username
       password: your_mysql_password
   ```

3. **运行后端**
   ```bash
   cd backend
   mvn spring-boot:run
   ```

### 前端设置

1. **安装依赖**
   ```bash
   cd frontend
   npm install
   ```

2. **运行开发服务器**
   ```bash
   npm run dev
   ```

3. **构建生产版本**
   ```bash
   npm run build
   ```

## 🔐 认证机制

系统使用JWT（JSON Web Token）进行身份验证：

- 登录后返回access token和refresh token
- access token有效期为24小时
- 请求需要认证的API时，在Header中携带`Authorization: Bearer <token>`

## 📷 图片处理

- 支持常见图片格式（JPEG, PNG, GIF, WebP等）
- 支持专业RAW格式（CR2, NEF, ARW, DNG等）
- 自动生成缩略图
- 提取EXIF元数据（相机型号、光圈、快门、ISO等）

## 🗂️ 数据库设计

主要数据表包括：
- `users`: 用户信息表
- `photos`: 照片信息表
- `photo_tags`: 照片标签表
- `albums`: 相册表
- `friends`: 好友关系表

## 🔧 API 接口

### 认证接口
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `GET /api/auth/me` - 获取当前用户信息
- `PUT /api/auth/profile` - 更新用户资料

### 照片接口
- `POST /api/photos/upload` - 上传照片
- `GET /api/photos` - 获取照片列表
- `GET /api/photos/{id}` - 获取照片详情
- `DELETE /api/photos/{id}` - 删除照片
- `GET /api/photos/search?tag={tag}` - 按标签搜索

## 🧪 测试

### 后端测试
```bash
cd backend
mvn test
```

### 前端测试
```bash
cd frontend
npm run test
```

## 🚀 部署

### 生产环境部署
1. 构建后端应用
   ```bash
   cd backend
   mvn clean package -DskipTests
   ```

2. 构建前端应用
   ```bash
   cd frontend
   npm run build
   ```

3. 部署到服务器

## 🤝 贡献

欢迎提交Issue和Pull Request来帮助改进这个项目！

## 📄 许可证

[MIT License](LICENSE)

---

感谢您使用照片管理系统！