<template>
  <div class="register-container">
    <div class="register-form">
      <h2>注册新账户</h2>

      <form @submit.prevent="handleRegister">
        <div class="form-row">
          <div class="form-group half-width">
            <label for="firstName">名字</label>
            <input
              id="firstName"
              v-model="form.firstName"
              type="text"
              placeholder="请输入名字"
            >
          </div>
          <div class="form-group half-width">
            <label for="lastName">姓氏</label>
            <input
              id="lastName"
              v-model="form.lastName"
              type="text"
              placeholder="请输入姓氏"
            >
          </div>
        </div>

        <div class="form-group">
          <label for="email">邮箱</label>
          <input
            id="email"
            v-model="form.email"
            type="email"
            placeholder="请输入邮箱地址"
            :class="{ 'error': errors.email }"
          >
          <span v-if="errors.email" class="error-message">{{ errors.email }}</span>
        </div>

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

        <div class="form-group">
          <label for="confirmPassword">确认密码</label>
          <input
            id="confirmPassword"
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            :class="{ 'error': errors.confirmPassword }"
          >
          <span v-if="errors.confirmPassword" class="error-message">{{ errors.confirmPassword }}</span>
        </div>

        <div class="form-group">
          <label for="bio">个人简介</label>
          <textarea
            id="bio"
            v-model="form.bio"
            placeholder="简单介绍一下自己"
            rows="3"
          ></textarea>
        </div>

        <button type="submit" class="btn-register" :disabled="loading">
          {{ loading ? '注册中...' : '注册' }}
        </button>
      </form>

      <div class="form-footer">
        <p>已有账户？<router-link to="/login">立即登录</router-link></p>
      </div>
    </div>
  </div>
</template>

<script>
import { useAuthStore } from '../../stores/index';

export default {
  name: 'RegisterView',
  setup() {
    const authStore = useAuthStore();
    return { authStore };
  },
  data() {
    return {
      form: {
        firstName: '',
        lastName: '',
        email: '',
        username: '',
        password: '',
        confirmPassword: '',
        bio: ''
      },
      errors: {},
      loading: false
    }
  },
  methods: {
    async handleRegister() {
      // 清除之前的错误
      this.errors = {};

      // 验证表单
      let isValid = true;

      if (!this.form.firstName.trim()) {
        this.errors.firstName = '请输入名字';
        isValid = false;
      }

      if (!this.form.lastName.trim()) {
        this.errors.lastName = '请输入姓氏';
        isValid = false;
      }

      if (!this.form.email.trim()) {
        this.errors.email = '请输入邮箱';
        isValid = false;
      } else if (!/\S+@\S+\.\S+/.test(this.form.email)) {
        this.errors.email = '请输入有效的邮箱地址';
        isValid = false;
      }

      if (!this.form.username.trim()) {
        this.errors.username = '请输入用户名';
        isValid = false;
      }

      if (!this.form.password) {
        this.errors.password = '请输入密码';
        isValid = false;
      } else if (this.form.password.length < 6) {
        this.errors.password = '密码至少需要6位';
        isValid = false;
      }

      if (!this.form.confirmPassword) {
        this.errors.confirmPassword = '请确认密码';
        isValid = false;
      } else if (this.form.password !== this.form.confirmPassword) {
        this.errors.confirmPassword = '两次输入的密码不一致';
        isValid = false;
      }

      if (!isValid) {
        return;
      }

      this.loading = true;

      try {
        const userData = {
          firstName: this.form.firstName,
          lastName: this.form.lastName,
          email: this.form.email,
          username: this.form.username,
          password: this.form.password,
          bio: this.form.bio
        };

        const response = await this.authStore.register(userData);
        // 注册成功，跳转到登录页面
        this.$router.push('/login');
      } catch (error) {
        console.error('Registration failed:', error);
        if (error.response?.data?.error) {
          alert(error.response.data.error);
        } else {
          alert('注册失败，请重试');
        }
      } finally {
        this.loading = false;
      }
    }
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
}

.register-form {
  width: 100%;
  max-width: 500px;
  padding: 2rem;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.form-row {
  display: flex;
  gap: 1rem;
}

.half-width {
  flex: 1;
}

.form-group {
  margin-bottom: 1.5rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: bold;
}

input, textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

input.error {
  border-color: #dc3545;
}

textarea {
  resize: vertical;
}

.error-message {
  color: #dc3545;
  font-size: 0.875rem;
  margin-top: 0.25rem;
  display: block;
}

.btn-register {
  width: 100%;
  padding: 0.75rem;
  background-color: #28a745;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
}

.btn-register:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.form-footer {
  margin-top: 1.5rem;
  text-align: center;
}

.form-footer a {
  color: #007bff;
  text-decoration: none;
}
</style>