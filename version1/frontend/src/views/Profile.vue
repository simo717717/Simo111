<template>
  <div class="profile-page">
    <h1>个人资料</h1>

    <div class="profile-card">
      <div class="profile-header">
        <div class="avatar-section">
          <div class="avatar">
            {{ getInitials(authStore.user) }}
          </div>
          <div class="user-info">
            <h2>{{ authStore.user?.firstName }} {{ authStore.user?.lastName }}</h2>
            <p class="username">@{{ authStore.user?.username }}</p>
            <p class="email">{{ authStore.user?.email }}</p>
          </div>
        </div>

        <div class="stats">
          <div class="stat-item">
            <span class="stat-number">{{ photoCount }}</span>
            <span class="stat-label">照片数</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">{{ tagCount }}</span>
            <span class="stat-label">标签数</span>
          </div>
        </div>
      </div>

      <div class="profile-details">
        <div class="detail-section">
          <h3>个人简介</h3>
          <p v-if="authStore.user?.bio">{{ authStore.user.bio }}</p>
          <p v-else class="no-info">暂无个人简介</p>
        </div>

        <div class="detail-section">
          <h3>账户设置</h3>
          <button @click="showEditModal = true" class="btn btn-edit">编辑资料</button>
        </div>
      </div>
    </div>

    <!-- 编辑资料模态框 -->
    <div v-if="showEditModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>编辑个人资料</h3>
          <button @click="closeModal" class="close-btn">&times;</button>
        </div>

        <div class="modal-body">
          <div class="form-group">
            <label for="edit-firstName">名字</label>
            <input
              id="edit-firstName"
              v-model="editForm.firstName"
              type="text"
            >
          </div>

          <div class="form-group">
            <label for="edit-lastName">姓氏</label>
            <input
              id="edit-lastName"
              v-model="editForm.lastName"
              type="text"
            >
          </div>

          <div class="form-group">
            <label for="edit-bio">个人简介</label>
            <textarea
              id="edit-bio"
              v-model="editForm.bio"
              rows="4"
              placeholder="简单介绍一下自己..."
            ></textarea>
          </div>
        </div>

        <div class="modal-footer">
          <button @click="closeModal" class="btn btn-secondary">取消</button>
          <button @click="saveProfile" :disabled="saving" class="btn btn-primary">
            {{ saving ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { useAuthStore, usePhotoStore } from '../stores/index';

export default {
  name: 'ProfileView',
  setup() {
    const authStore = useAuthStore();
    const photoStore = usePhotoStore();
    return { authStore, photoStore };
  },
  data() {
    return {
      showEditModal: false,
      saving: false,
      editForm: {
        firstName: '',
        lastName: '',
        bio: ''
      }
    }
  },
  computed: {
    photoCount() {
      // 计算用户拥有的照片数量
      return this.photoStore.photos ? this.photoStore.photos.length : 0;
    },
    tagCount() {
      // 计算所有照片的唯一标签数量
      const allTags = new Set();
      this.photoStore.photos.forEach(photo => {
        if (photo.tags) {
          photo.tags.forEach(tag => allTags.add(tag));
        }
      });
      return allTags.size;
    }
  },
  async mounted() {
    // 加载用户信息和照片
    if (!this.authStore.user) {
      await this.authStore.fetchCurrentUser();
    }

    await this.photoStore.fetchPhotos();
  },
  methods: {
    getInitials(user) {
      if (!user) return '?';
      const first = user.firstName ? user.firstName.charAt(0).toUpperCase() : '?';
      const last = user.lastName ? user.lastName.charAt(0).toUpperCase() : '';
      return first + last;
    },

    openEditModal() {
      this.editForm.firstName = this.authStore.user?.firstName || '';
      this.editForm.lastName = this.authStore.user?.lastName || '';
      this.editForm.bio = this.authStore.user?.bio || '';
      this.showEditModal = true;
    },

    closeModal() {
      this.showEditModal = false;
    },

    async saveProfile() {
      this.saving = true;

      try {
        // 在实际应用中，这里会调用API更新用户资料
        // await this.authStore.updateProfile(this.editForm);

        // 更新本地存储的用户信息
        this.authStore.updateUser({
          firstName: this.editForm.firstName,
          lastName: this.editForm.lastName,
          bio: this.editForm.bio
        });

        this.closeModal();
      } catch (error) {
        console.error('Failed to update profile:', error);
        alert('更新失败，请重试');
      } finally {
        this.saving = false;
      }
    }
  }
}
</script>

<style scoped>
.profile-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}

.profile-card {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  flex-wrap: wrap;
  gap: 2rem;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background-color: #007bff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  font-weight: bold;
}

.user-info h2 {
  margin: 0;
  font-size: 1.5rem;
}

.username {
  color: #666;
  margin: 0.25rem 0;
}

.email {
  color: #999;
  margin: 0;
}

.stats {
  display: flex;
  gap: 2rem;
}

.stat-item {
  text-align: center;
}

.stat-number {
  display: block;
  font-size: 1.8rem;
  font-weight: bold;
  color: #007bff;
}

.stat-label {
  font-size: 0.9rem;
  color: #666;
}

.detail-section {
  margin-bottom: 2rem;
}

.detail-section h3 {
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid #eee;
}

.no-info {
  color: #999;
  font-style: italic;
}

.btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  text-decoration: none;
  display: inline-block;
  text-align: center;
  transition: opacity 0.3s;
}

.btn-edit {
  background-color: #007bff;
  color: white;
}

.btn-primary {
  background-color: #007bff;
  color: white;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #999;
}

.modal-body {
  padding: 1.5rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: bold;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.modal-footer {
  padding: 1rem 1.5rem;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
}
</style>