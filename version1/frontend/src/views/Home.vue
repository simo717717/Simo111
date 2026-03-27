<template>
  <div class="home-container">
    <Sidebar v-if="authStore.isAuthenticated" />

    <main class="main-content" :class="{ 'with-sidebar': authStore.isAuthenticated }">
      <div class="home-content">
        <header class="home-header">
          <h1>欢迎回来，{{ authStore.user?.username || '用户' }}！</h1>
          <p class="subtitle">一个专业的照片管理平台，支持多种格式包括RAW格式</p>
        </header>

        <div v-if="!authStore.isAuthenticated" class="unauthorized-section">
          <div class="unauthorized-card">
            <h2>欢迎来到照片管理系统</h2>
            <p>请登录或注册以开始使用所有功能</p>
            <div class="auth-buttons">
              <router-link to="/login" class="btn btn-primary btn-large">登录</router-link>
              <router-link to="/register" class="btn btn-secondary btn-large">注册</router-link>
            </div>
          </div>
        </div>

        <div v-else class="dashboard">
          <div class="welcome-banner">
            <div class="banner-content">
              <h2>📸 开始管理您的照片</h2>
              <p>上传、整理、分享您的精彩瞬间</p>
            </div>
            <router-link to="/upload" class="btn-upload">
              <span class="upload-icon">+</span>
              上传新照片
            </router-link>
          </div>

          <div class="quick-stats">
            <div class="stat-card">
              <div class="stat-icon">🖼️</div>
              <div class="stat-info">
                <h3>{{ photoStats.total || 0 }}</h3>
                <p>总照片数</p>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">📁</div>
              <div class="stat-info">
                <h3>{{ photoStats.albums || 0 }}</h3>
                <p>相册数</p>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">👥</div>
              <div class="stat-info">
                <h3>{{ photoStats.friends || 0 }}</h3>
                <p>好友数</p>
              </div>
            </div>
          </div>

          <div class="quick-actions">
            <h2>快速访问</h2>
            <div class="action-grid">
              <router-link to="/gallery" class="action-card">
                <div class="action-icon">🖼️</div>
                <h3>我的图库</h3>
                <p>浏览和管理所有照片</p>
              </router-link>

              <router-link to="/upload" class="action-card">
                <div class="action-icon">📤</div>
                <h3>上传照片</h3>
                <p>上传新照片或RAW格式</p>
              </router-link>

              <router-link to="/profile" class="action-card">
                <div class="action-icon">👤</div>
                <h3>个人资料</h3>
                <p>查看和编辑个人资料</p>
              </router-link>

              <router-link to="/albums" class="action-card">
                <div class="action-icon">📁</div>
                <h3>相册管理</h3>
                <p>创建和管理相册</p>
              </router-link>

              <router-link to="/friends" class="action-card">
                <div class="action-icon">👥</div>
                <h3>好友</h3>
                <p>查看和添加好友</p>
              </router-link>

              <router-link to="/settings" class="action-card">
                <div class="action-icon">⚙️</div>
                <h3>设置</h3>
                <p>系统和个人设置</p>
              </router-link>
            </div>
          </div>

          <div class="recent-photos">
            <h2>最近上传</h2>
            <div v-if="recentPhotos.length > 0" class="photos-grid">
              <div
                v-for="photo in recentPhotos"
                :key="photo.id"
                class="photo-item"
                @click="viewPhoto(photo)"
              >
                <img
                  :src="getPhotoImageUrl(photo)"
                  :alt="photo.originalName"
                  @error="handleImageError($event, photo)"
                />
                <div class="photo-info">
                  <p class="photo-name">{{ photo.originalName }}</p>
                  <p class="photo-date">{{ formatDate(photo.uploadTime) }}</p>
                </div>
              </div>
            </div>
            <div v-else class="empty-recent">
              <p>暂无最近上传的照片</p>
              <router-link to="/upload" class="btn-link">立即上传</router-link>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import { useAuthStore, usePhotoStore } from '../stores/index';
import Sidebar from '../components/Sidebar.vue';
import { onMounted, computed } from 'vue';

export default {
  name: 'HomeView',
  components: {
    Sidebar
  },
  setup() {
    const authStore = useAuthStore();
    const photoStore = usePhotoStore();

    // 照片统计数据 - 使用computed使其响应式
    const photoStats = computed(() => ({
      total: photoStore.photos?.length || 0,
      albums: 0, // 后续从API获取
      friends: 0  // 后续从API获取
    }));

    // 获取最近上传的照片 - 使用computed
    const recentPhotos = computed(() =>
      photoStore.photos || []
    );

    const getPhotoImageUrl = (photo) => {
      if (!photo) {
        return getDefaultImage();
      }

      // 优先使用缩略图，如果不可用则使用原图
      const url = photo.thumbnailUrl || photo.fileUrl;

      if (url) {
        // 确保URL是完整的（如果是相对路径）
        if (url.startsWith('http') || url.startsWith('/')) {
          return url;
        }
        // 如果是相对路径，添加API前缀
        return `/api/${url}`;
      }

      // 对于RAW文件特殊处理
      if (photo.fileType === 'RAW') {
        return getRawDefaultImage();
      }

      return getDefaultImage();
    };

    const getDefaultImage = () => {
      // 返回一个简单的SVG作为默认图片
      return 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="300" height="300"><rect width="300" height="300" fill="#f0f0f0"/><text x="150" y="150" font-family="Arial" font-size="16" text-anchor="middle" dy=".3em" fill="#666">No Image</text></svg>';
    };

    const getRawDefaultImage = () => {
      // RAW文件专用的默认图像
      return 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="300" height="300"><rect width="300" height="300" fill="#e3f2fd"/><text x="150" y="150" font-family="Arial" font-size="14" text-anchor="middle" dy=".3em" fill="#1565c0">RAW File</text><text x="150" y="180" font-family="Arial" font-size="12" text-anchor="middle" dy=".3em" fill="#1976d2">No Preview Available</text></svg>';
    };

    const formatDate = (dateString) => {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
      });
    };

    const viewPhoto = (photo) => {
      // 跳转到照片详情页
      window.location.href = `/photo/${photo.id}`;
    };

    const handleImageError = (event, photo) => {
      // 图片加载失败时，使用默认图片
      console.warn(`Failed to load image for photo ${photo.id}:`, photo.originalName);
      event.target.src = getDefaultImage();
      event.target.onerror = null; // 防止无限循环
    };

    // 组件挂载时获取照片数据
    onMounted(async () => {
      try {
        await photoStore.fetchPhotos({ page: 0, size: 20 });
      } catch (error) {
        console.error('Failed to fetch photos in home:', error);
      }
    });

    return {
      authStore,
      photoStats,
      recentPhotos,
      getPhotoImageUrl,
      formatDate,
      viewPhoto,
      handleImageError
    };
  }
}
</script>

<style scoped>
.home-container {
  display: flex;
  min-height: 100vh;
  background-color: #f8f9fa;
}

.main-content {
  flex: 1;
  padding: 2rem;
  transition: margin-left 0.3s ease;
}

.main-content.with-sidebar {
  margin-left: 280px;
}

.home-content {
  max-width: 1200px;
  margin: 0 auto;
}

.home-header {
  margin-bottom: 2.5rem;
}

.home-header h1 {
  font-size: 2.5rem;
  color: #333;
  margin-bottom: 0.5rem;
}

.subtitle {
  font-size: 1.1rem;
  color: #666;
}

.unauthorized-section {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
}

.unauthorized-card {
  background: white;
  padding: 3rem;
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  text-align: center;
  max-width: 500px;
  width: 100%;
}

.unauthorized-card h2 {
  font-size: 2rem;
  margin-bottom: 1rem;
  color: #333;
}

.unauthorized-card p {
  font-size: 1.1rem;
  color: #666;
  margin-bottom: 2rem;
}

.auth-buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

.btn-large {
  padding: 1rem 2rem;
  font-size: 1.1rem;
  min-width: 150px;
}

/* 仪表板样式 */
.welcome-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #007bff, #0056b3);
  color: white;
  padding: 2rem;
  border-radius: 12px;
  margin-bottom: 2rem;
}

.banner-content h2 {
  font-size: 1.8rem;
  margin-bottom: 0.5rem;
}

.banner-content p {
  opacity: 0.9;
  font-size: 1.1rem;
}

.btn-upload {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem 2rem;
  background-color: white;
  color: #007bff;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 600;
  font-size: 1.1rem;
  transition: transform 0.2s, box-shadow 0.2s;
}

.btn-upload:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.upload-icon {
  font-size: 1.5rem;
  font-weight: bold;
}

.quick-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2.5rem;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  padding: 1.5rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  font-size: 2.5rem;
}

.stat-info h3 {
  font-size: 2rem;
  color: #333;
  margin: 0 0 0.25rem 0;
}

.stat-info p {
  color: #666;
  margin: 0;
}

.quick-actions {
  margin-bottom: 2.5rem;
}

.quick-actions h2 {
  font-size: 1.8rem;
  color: #333;
  margin-bottom: 1.5rem;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.5rem;
}

.action-card {
  display: flex;
  flex-direction: column;
  padding: 1.5rem;
  background: white;
  border-radius: 12px;
  text-decoration: none;
  color: #333;
  transition: transform 0.2s, box-shadow 0.2s;
  border: 1px solid #e0e0e0;
}

.action-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  border-color: #007bff;
}

.action-icon {
  font-size: 2.5rem;
  margin-bottom: 1rem;
}

.action-card h3 {
  font-size: 1.3rem;
  margin: 0 0 0.5rem 0;
}

.action-card p {
  color: #666;
  margin: 0;
  font-size: 0.95rem;
  flex: 1;
}

.recent-photos {
  margin-bottom: 2rem;
}

.recent-photos h2 {
  font-size: 1.8rem;
  color: #333;
  margin-bottom: 1.5rem;
}

.photos-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 1.5rem;
}

.photo-item {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.photo-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.photo-item img {
  width: 100%;
  height: 150px;
  object-fit: cover;
}

.photo-info {
  padding: 1rem;
}

.photo-name {
  font-weight: 600;
  margin: 0 0 0.25rem 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.photo-date {
  font-size: 0.875rem;
  color: #666;
  margin: 0;
}

.empty-recent {
  text-align: center;
  padding: 3rem;
  background: white;
  border-radius: 12px;
  border: 2px dashed #ddd;
}

.empty-recent p {
  color: #666;
  margin-bottom: 1rem;
}

.btn-link {
  color: #007bff;
  text-decoration: none;
  font-weight: 600;
}

.btn-link:hover {
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .main-content.with-sidebar {
    margin-left: 0;
  }

  .home-header h1 {
    font-size: 2rem;
  }

  .welcome-banner {
    flex-direction: column;
    gap: 1.5rem;
    text-align: center;
  }

  .quick-stats {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .main-content {
    padding: 1rem;
  }

  .home-header h1 {
    font-size: 1.8rem;
  }

  .unauthorized-card {
    padding: 2rem;
  }

  .quick-stats {
    grid-template-columns: 1fr;
  }

  .action-grid {
    grid-template-columns: 1fr;
  }

  .photos-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .photos-grid {
    grid-template-columns: 1fr;
  }

  .auth-buttons {
    flex-direction: column;
  }

  .btn-large {
    width: 100%;
  }
}
</style>