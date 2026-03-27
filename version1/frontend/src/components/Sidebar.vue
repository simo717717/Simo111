<template>
  <aside class="sidebar">
    <div class="sidebar-header">
      <div class="user-info">
        <div class="avatar">
          <img
            :src="userAvatar"
            :alt="authStore.user?.username || '用户'"
            class="avatar-image"
          />
        </div>
        <div class="user-details">
          <h3 class="username">{{ authStore.user?.username || '游客' }}</h3>
          <p class="user-email">{{ authStore.user?.email || '未登录' }}</p>
        </div>
      </div>
    </div>

    <div class="sidebar-menu">
      <nav class="nav-menu">
        <router-link to="/" class="nav-item" exact-active-class="active">
          <span class="nav-icon">🏠</span>
          <span class="nav-text">首页</span>
        </router-link>

        <!-- 照片管理分组 -->
        <div class="nav-section">
          <div class="section-header" @click="toggleSection('photoManagement')">
            <h3 class="section-title">
              <span class="section-arrow">{{ expandedSections.photoManagement ? '▼' : '▶' }}</span>
              照片管理
            </h3>
          </div>
          <div class="submenu" v-show="expandedSections.photoManagement">
            <router-link to="/gallery" class="nav-item submenu-item" active-class="active">
              <span class="nav-icon">🖼️</span>
              <span class="nav-text">我的图库</span>
            </router-link>

            <router-link to="/upload" class="nav-item submenu-item" active-class="active">
              <span class="nav-icon">📤</span>
              <span class="nav-text">上传照片</span>
            </router-link>

            <router-link to="/profile" class="nav-item submenu-item" active-class="active">
              <span class="nav-icon">👤</span>
              <span class="nav-text">个人资料</span>
            </router-link>

            <router-link to="/albums" class="nav-item submenu-item" active-class="active">
              <span class="nav-icon">📁</span>
              <span class="nav-text">相册管理</span>
            </router-link>
          </div>
        </div>

        <!-- 其他功能分组 -->
        <div class="nav-section">
          <div class="section-header" @click="toggleSection('otherFunctions')">
            <h3 class="section-title">
              <span class="section-arrow">{{ expandedSections.otherFunctions ? '▼' : '▶' }}</span>
              其他功能
            </h3>
          </div>
          <div class="submenu" v-show="expandedSections.otherFunctions">
            <router-link to="/friends" class="nav-item submenu-item" active-class="active">
              <span class="nav-icon">👥</span>
              <span class="nav-text">好友</span>
            </router-link>

            <router-link to="/settings" class="nav-item submenu-item" active-class="active">
              <span class="nav-icon">⚙️</span>
              <span class="nav-text">设置</span>
            </router-link>
          </div>
        </div>
      </nav>
    </div>

    <div class="sidebar-footer">
      <div class="stats">
        <div class="stat-item">
          <span class="stat-value">{{ photoStats.total || 0 }}</span>
          <span class="stat-label">照片</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ photoStats.albums || 0 }}</span>
          <span class="stat-label">相册</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ photoStats.friends || 0 }}</span>
          <span class="stat-label">好友</span>
        </div>
      </div>
      <button class="logout-btn" @click="handleLogout">
        <span class="logout-icon">🚪</span>
        <span class="logout-text">退出登录</span>
      </button>
    </div>
  </aside>
</template>

<script>
import { useAuthStore, usePhotoStore } from '../stores/index';
import { onMounted, computed, ref } from 'vue';

export default {
  name: 'Sidebar',
  setup() {
    const authStore = useAuthStore();
    const photoStore = usePhotoStore();

    // 照片统计数据
    const photoStats = computed(() => ({
      total: photoStore.photos?.length || 0,
      albums: 0, // 后续从API获取
      friends: 0  // 后续从API获取
    }));

    // 侧边栏分组展开状态
    const expandedSections = ref({
      photoManagement: false, // 照片管理分组默认收缩
      otherFunctions: false   // 其他功能分组默认收缩
    });

    // 切换分组展开状态
    const toggleSection = (sectionKey) => {
      expandedSections.value[sectionKey] = !expandedSections.value[sectionKey];
    };
    const userAvatar = 'https://ui-avatars.com/api/?name=' +
      encodeURIComponent(authStore.user?.username || 'User') +
      '&background=007bff&color=fff&size=128';

    const handleLogout = () => {
      authStore.logout();
      // 退出后跳转到登录页
      window.location.href = '/login';
    };

    // 组件挂载时尝试获取照片数据
    onMounted(async () => {
      if (photoStore.photos.length === 0) {
        try {
          await photoStore.fetchPhotos({ page: 0, size: 1 });
        } catch (error) {
          console.error('Failed to fetch photos in sidebar:', error);
        }
      }
    });

    return {
      authStore,
      photoStats,
      userAvatar,
      expandedSections,
      toggleSection,
      handleLogout
    };
  }
}
</script>

<style scoped>
.sidebar {
  width: 280px;
  height: 100vh;
  background-color: #fff;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
  position: fixed;
  left: 0;
  top: 0;
  z-index: 100;
}

.sidebar-header {
  padding: 1.5rem;
  border-bottom: 1px solid #e0e0e0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  overflow: hidden;
  background-color: #f0f0f0;
  flex-shrink: 0;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-details {
  flex: 1;
  overflow: hidden;
}

.username {
  font-size: 1.2rem;
  font-weight: 600;
  color: #333;
  margin: 0 0 0.25rem 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-email {
  font-size: 0.875rem;
  color: #666;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sidebar-menu {
  flex: 1;
  padding: 1.5rem 0;
  overflow-y: auto;
}

.nav-menu {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  padding: 0 1rem;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1rem 1.25rem;
  text-decoration: none;
  color: #555;
  border-radius: 12px;
  transition: all 0.3s ease;
  background-color: #fafafa;
  border: 1px solid #f0f0f0;
}

.nav-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: #007bff;
  color: #007bff;
  background-color: white;
}

.nav-item.active {
  background-color: rgba(0, 123, 255, 0.1);
  color: #007bff;
  font-weight: 600;
  border-color: #007bff;
  box-shadow: 0 2px 8px rgba(0, 123, 255, 0.15);
}

.nav-icon {
  font-size: 1.25rem;
  width: 24px;
  text-align: center;
}

.nav-text {
  font-size: 1rem;
}

.nav-section {
  margin-top: 1rem;
}

.section-header {
  cursor: pointer;
  user-select: none;
  transition: transform 0.2s, box-shadow 0.2s;
  border-radius: 12px;
  margin: 0 0.5rem;
  padding: 1rem 1.25rem;
  background: white;
  border: 1px solid #e0e0e0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.section-header:hover {
  transform: translateY(-4px);
  border-color: #007bff;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 0.9rem;
  font-weight: 600;
  color: #333;
  margin: 0;
  padding: 0;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.section-arrow {
  font-size: 0.7rem;
  width: 16px;
  text-align: center;
  transition: transform 0.2s ease;
}

.submenu {
  display: flex;
  flex-direction: column;
  gap: 0.125rem;
  margin-top: 0.25rem;
}

.submenu-item {
  padding-left: 2.5rem;
  font-size: 0.95rem;
}

.submenu-item .nav-icon {
  font-size: 1.1rem;
  width: 20px;
}

.nav-divider {
  height: 1px;
  background-color: #e0e0e0;
  margin: 1rem 1rem;
}

.sidebar-footer {
  padding: 1.5rem;
  border-top: 1px solid #e0e0e0;
  background-color: #fafafa;
}

.stats {
  display: flex;
  justify-content: space-around;
  margin-bottom: 1.5rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid #e0e0e0;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: #333;
}

.stat-label {
  font-size: 0.75rem;
  color: #666;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-top: 0.25rem;
}

.logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  width: 100%;
  padding: 0.875rem;
  background-color: #f8f9fa;
  border: 1px solid #ddd;
  border-radius: 8px;
  color: #666;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.logout-btn:hover {
  background-color: #f1f1f1;
  color: #dc3545;
  border-color: #dc3545;
}

.logout-icon {
  font-size: 1.25rem;
}

.logout-text {
  font-size: 1rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    width: 240px;
  }

  .sidebar-header {
    padding: 1rem;
  }

  .avatar {
    width: 48px;
    height: 48px;
  }

  .username {
    font-size: 1.1rem;
  }

  .nav-menu {
    padding: 0 0.75rem;
  }

  .nav-item {
    padding: 0.875rem 1rem;
  }
}
</style>