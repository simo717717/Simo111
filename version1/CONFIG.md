# 环境配置文件

## 后端配置
- 确保MySQL数据库已安装并运行
- 在 application.yml 中正确配置数据库连接信息
- 运行后端服务: `cd backend && mvn spring-boot:run`

## 前端配置
- 确保Node.js已安装
- 在前端项目根目录下运行: `npm install`
- 启动前端服务: `npm run dev`
- 前端默认运行在 http://localhost:3000

## 数据库配置
- 数据库名: photo_management
- 需要先运行 init_database.sql 脚本创建表结构
- 用户名和密码根据您本地MySQL配置设置

## API 代理配置
- 前端已经配置了代理，将 /api 请求转发到后端服务
- 确保后端服务运行在 http://localhost:8080

## 故障排除
1. 如果注册/登录失败：
   - 检查后端服务是否正常运行
   - 确认数据库连接是否正常
   - 查看浏览器开发者工具中的网络请求错误

2. 如果照片上传失败：
   - 确认用户已登录
   - 检查文件大小和格式限制