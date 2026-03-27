# 项目设置和运行指南

## 后端 (Spring Boot)

### 环境要求
- Java 17+
- Maven 3.6+
- MySQL 8.0+

### 设置步骤
1. 安装Java 17+ 和 Maven
2. 配置MySQL数据库
3. 修改 `backend/src/main/resources/application.yml` 中的数据库配置
4. 运行 `mvn clean install` 安装依赖
5. 运行 `mvn spring-boot:run` 启动后端

### 数据库设置
1. 创建数据库 `photo_management`
2. 执行 `database_init.sql` 脚本创建表结构

## 前端 (Vue 3)

### 环境要求
- Node.js 16+
- npm 或 yarn

### 设置步骤
1. 安装 Node.js
2. 进入 `frontend` 目录
3. 运行 `npm install` 安装依赖
4. 运行 `npm run dev` 启动开发服务器

## 完整运行流程

### 方法1: 分别启动
1. 启动后端服务: `cd backend && mvn spring-boot:run`
2. 启动前端服务: `cd frontend && npm run dev`

### 方法2: 使用脚本
创建一个启动脚本同时启动前后端服务

## API 接口
- 后端地址: http://localhost:8080
- 前端地址: http://localhost:3000
- 前端已配置代理将 `/api` 请求转发到后端

## 部署说明
- 后端: 打包为jar文件直接运行
- 前端: 构建后部署到静态服务器