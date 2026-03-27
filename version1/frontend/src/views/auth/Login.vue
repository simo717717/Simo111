<template>
  <div class="login-container">
    <h1 class="system-title">照片智能管理与分类系统</h1>
    <div class="login-form">
      <h2>登录</h2>

      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="username">用户名</label>
          <input
            id="username"
            v-model="form.username"
            type="text"
            placeholder="请输入用户名"
            :class="{ 'error': errors.username }"
          >
          <span v-if="errors.username" class="error-message">{{ errors.username }}</span>
        </div>

        <div class="form-group">
          <label for="password">密码</label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :class="{ 'error': errors.password }"
          >
          <span v-if="errors.password" class="error-message">{{ errors.password }}</span>
        </div>

        <button type="submit" class="btn-login" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>

      <div class="form-footer">
        <p>还没有账户？<router-link to="/register">立即注册</router-link></p>
      </div>
    </div>
  </div>
</template>

<script>
import { useAuthStore } from '../../stores/index';

export default {
  name: 'LoginView',
  setup() {
    const authStore = useAuthStore();
    return { authStore };
  },
  data() {
    return {
      form: {
        username: '',
        password: ''
      },
      errors: {},
      loading: false
    }
  },
  methods: {
    async handleLogin() {
      // 清除之前的错误
      this.errors = {};

      // 简单验证
      if (!this.form.username.trim()) {
        this.errors.username = '请输入用户名';
        return;
      }

      if (!this.form.password) {
        this.errors.password = '请输入密码';
        return;
      }

      this.loading = true;

      try {
        const response = await this.authStore.login(this.form);
        // 登录成功，跳转到首页或其他页面
        this.$router.push('/');
      } catch (error) {
        console.error('Login failed:', error);
        // 显示错误消息
        if (error.response?.data?.error) {
          alert(error.response.data.error);
        } else {
          alert('登录失败，请检查用户名和密码');
        }
      } finally {
        this.loading = false;
      }
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  height: 100vh;
  width: 100vw;
  background-image: url('../../assets/p1.jpg');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  position: fixed;
  top: 0;
  left: 0;
  margin: 0;
  padding: 0;
  overflow: hidden;
}

.login-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.05); /* 再降低50%透明度 */
  z-index: 1;
}

.system-title {
  position: absolute;
  top: 2.5rem;
  left: 2.5rem;
  font-size: 2.2rem;
  font-weight: 700;
  color: white;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  z-index: 3;
  margin: 0;
  padding: 0;
  line-height: 1.2;
  max-width: 600px;
}

.login-form {
  width: 100%;
  max-width: 380px; /* 稍微减小宽度 */
  margin-right: 10%;
  padding: 2.5rem;
  background-color: rgba(255, 255, 255, 0.1625); /* 再降低50%透明度 */
  border-radius: 16px; /* 增加圆角 */
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.35); /* 增强阴影 */
  position: relative;
  z-index: 2;
  backdrop-filter: blur(15px) saturate(180%); /* 增加模糊和饱和度 */
  border: 1px solid rgba(255, 255, 255, 0.2); /* 添加边框 */
}

.login-form h2 {
  text-align: center;
  margin-bottom: 2rem;
  color: #333;
  font-size: 1.8rem;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1); /* 添加文字阴影 */
}

.form-group {
  margin-bottom: 1.5rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #444; /* 稍微加深颜色 */
}

input {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 1px solid rgba(0, 0, 0, 0.15); /* 更柔和的边框 */
  border-radius: 8px; /* 增加圆角 */
  font-size: 1rem;
  transition: all 0.3s ease;
  background-color: rgba(255, 255, 255, 0.7); /* 降低透明度以匹配卡片 */
  backdrop-filter: blur(5px);
}

input:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.2), 0 4px 12px rgba(0, 123, 255, 0.1);
  background-color: rgba(255, 255, 255, 0.75);
}

input.error {
  border-color: #dc3545;
}

input.error:focus {
  box-shadow: 0 0 0 3px rgba(220, 53, 69, 0.2);
}

.error-message {
  color: #dc3545;
  font-size: 0.875rem;
  margin-top: 0.25rem;
  display: block;
}

.btn-login {
  width: 100%;
  padding: 0.875rem;
  background: linear-gradient(135deg, #007bff, #0056b3); /* 渐变背景 */
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(0, 123, 255, 0.3);
  position: relative;
  overflow: hidden;
}

.btn-login::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.1), rgba(255, 255, 255, 0));
  opacity: 0;
  transition: opacity 0.3s ease;
}

.btn-login:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(0, 123, 255, 0.4);
}

.btn-login:hover:not(:disabled)::before {
  opacity: 1;
}

.btn-login:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(0, 123, 255, 0.3);
}

.btn-login:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  background: #6c757d;
}

.form-footer {
  margin-top: 1.5rem;
  text-align: center;
  color: #666;
}

.form-footer a {
  color: #007bff;
  text-decoration: none;
  font-weight: 600;
}

.form-footer a:hover {
  text-decoration: underline;
}

/* 响应式调整 */
@media (max-width: 480px) {
  .system-title {
    font-size: 1.5rem;
    top: 1.5rem;
    left: 1.5rem;
    max-width: 300px;
  }

  .login-form {
    margin: 1rem;
    padding: 2rem;
  }

  .login-form h2 {
    font-size: 1.5rem;
  }
}
</style>