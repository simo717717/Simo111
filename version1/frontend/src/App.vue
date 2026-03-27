<template>
  <div id="app">
    <!-- 顶部导航栏 - 仅未登录或特定页面显示 -->
    <nav v-if="showNavbar" class="navbar">
      <div class="nav-brand">
        <router-link to="/">照片管理系统</router-link>
      </div>
      <div class="nav-menu">
        <template v-if="!isLoggedIn">
          <router-link to="/login" class="nav-link">登录</router-link>
          <router-link to="/register" class="nav-link">注册</router-link>
        </template>
        <template v-else>
          <router-link to="/" class="nav-link">首页</router-link>
          <router-link to="/gallery" class="nav-link">图库</router-link>
          <router-link to="/upload" class="nav-link">上传</router-link>
          <router-link to="/profile" class="nav-link">个人资料</router-link>
          <button @click="logout" class="nav-link logout-btn">退出</button>
        </template>
      </div>
    </nav>

    <!-- 侧边栏 - 登录后显示 -->
    <Sidebar v-if="showSidebar" />

    <main class="main-content" :class="{ 'with-sidebar': showSidebar, 'with-navbar': showNavbar }">
      <router-view />
    </main>
  </div>
</template>

<script>
import { useAuthStore } from './stores/index';
import Sidebar from './components/Sidebar.vue';

export default {
  name: 'App',
  components: {
    Sidebar
  },
  setup() {
    const authStore = useAuthStore();
    return { authStore };
  },
  computed: {
    isLoggedIn() {
      return this.authStore.isAuthenticated;
    },
    // 显示顶部导航栏的条件
    showNavbar() {
      // 登录页面不显示导航栏
      if (this.$route.path === '/login' || this.$route.path === '/register') {
        return false;
      }
      // 未登录时显示导航栏
      if (!this.isLoggedIn) {
        return true;
      }
      // 登录后不显示导航栏（由侧边栏代替）
      return false;
    },
    // 显示侧边栏的条件
    showSidebar() {
      // 登录页面不显示侧边栏
      if (this.$route.path === '/login' || this.$route.path === '/register') {
        return false;
      }
      // 登录后显示侧边栏
      return this.isLoggedIn;
    }
  },
  created() {
    // 页面加载时尝试恢复用户状态
    if (this.authStore.isAuthenticated && !this.authStore.user) {
      this.authStore.fetchCurrentUser().catch(error => {
        console.warn('Failed to restore user session:', error);
        // 如果token无效，自动登出
        if (error.response?.status === 401) {
          this.authStore.logout();
        }
      });
    }
  },
  methods: {
    logout() {
      this.authStore.logout();
    }
  }
}
</script>

<style>
@import './styles/global.css';

/* 本地样式可以在这里继续定义 */
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-brand a {
  font-size: 1.5rem;
  font-weight: bold;
  color: #333;
  text-decoration: none;
}

.nav-menu {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.nav-link {
  padding: 0.5rem 1rem;
  text-decoration: none;
  color: #333;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.nav-link:hover {
  background-color: #f0f0f0;
}

.logout-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: inherit;
}

.main-content {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
  transition: margin-left 0.3s ease, padding-top 0.3s ease;
}

/* 当有侧边栏时调整主内容 */
.main-content.with-sidebar {
  margin-left: 280px;
  max-width: none;
  padding: 2rem 3rem;
}

/* 当有导航栏时调整主内容上边距 */
.main-content.with-navbar {
  padding-top: calc(2rem + 60px); /* 导航栏高度 */
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .main-content.with-sidebar {
    margin-left: 0;
    padding: 2rem;
  }
}

@media (max-width: 768px) {
  .navbar {
    padding: 1rem;
  }

  .nav-brand a {
    font-size: 1.2rem;
  }

  .nav-menu {
    gap: 0.5rem;
  }

  .nav-link {
    padding: 0.5rem 0.75rem;
    font-size: 0.9rem;
  }

  .main-content {
    padding: 1rem;
  }

  .main-content.with-navbar {
    padding-top: calc(1rem + 60px);
  }
}
</style>