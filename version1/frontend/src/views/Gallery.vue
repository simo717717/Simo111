<template>
  <div class="gallery-page">
    <h1>我的图库</h1>

    <div class="filters">
      <div class="filter-group">
        <label for="visibility-filter">可见性:</label>
        <select id="visibility-filter" v-model="visibilityFilter" @change="fetchPhotos">
          <option value="">全部</option>
          <option value="PUBLIC">公开</option>
          <option value="PRIVATE">私有</option>
          <option value="FRIENDS">好友</option>
        </select>
      </div>

      <div class="filter-group">
        <label for="search-tags">搜索标签:</label>
        <input
          id="search-tags"
          v-model="tagSearch"
          type="text"
          placeholder="输入标签搜索..."
          @keyup.enter="searchByTag"
        >
        <button @click="searchByTag" class="btn btn-search">搜索</button>
      </div>
    </div>

    <div v-if="photoStore.loading" class="loading">
      加载中...
    </div>

    <div v-else-if="photoStore.photos.length === 0" class="empty-state">
      <p>暂无照片，<router-link to="/upload">立即上传</router-link></p>
    </div>

    <div v-else class="gallery-grid">
      <div
        v-for="photo in photoStore.photos"
        :key="photo.id"
        class="photo-card"
        @click="viewPhoto(photo)"
      >
        <div class="photo-image">
          <img
            :src="getPhotoImageUrl(photo)"
            :alt="photo.description || photo.originalName"
            @load="onImageLoad"
            @error="onImageError"
          />
          <div v-if="photo.fileType === 'RAW' && !hasRawPreview(photo)" class="raw-file-overlay">
            <div class="raw-file-info">
              <span class="raw-icon">📷</span>
              <span class="raw-text">RAW 文件</span>
              <span class="raw-hint">需要转换预览</span>
            </div>
          </div>
        </div>
        <div class="photo-info">
          <h3>{{ truncateText(photo.originalName, 30) }}</h3>
          <p class="photo-description">{{ truncateText(photo.description, 50) }}</p>
          <div class="photo-meta">
            <span class="visibility" :class="photo.visibility.toLowerCase()">
              {{ photo.visibility }}
            </span>
            <span class="file-type-tag" :class="photo.fileType ? photo.fileType.toLowerCase() : 'image'">
              {{ photo.fileType || 'IMAGE' }}
            </span>
            <span class="date">{{ formatDate(photo.createdAt) }}</span>
          </div>
          <div v-if="photo.tags && photo.tags.length > 0" class="photo-tags">
            <span
              v-for="(tag, index) in photo.tags.slice(0, 3)"
              :key="index"
              class="tag"
            >
              {{ tag }}
            </span>
            <span v-if="photo.tags.length > 3" class="tag-more">
              +{{ photo.tags.length - 3 }}
            </span>
          </div>
        </div>
        <div class="photo-actions">
          <button @click.stop="downloadPhoto(photo)" class="btn btn-small">下载</button>
          <button @click.stop="editPhoto(photo)" class="btn btn-small btn-secondary">编辑</button>
          <button @click.stop="deletePhoto(photo)" class="btn btn-small btn-danger">删除</button>
        </div>
      </div>
    </div>

    <div v-if="photoStore.pagination.totalPages > 1" class="pagination">
      <button
        @click="changePage(photoStore.pagination.currentPage - 1)"
        :disabled="photoStore.pagination.currentPage === 0"
        class="btn btn-pagination"
      >
        上一页
      </button>

      <span class="page-info">
        {{ photoStore.pagination.currentPage + 1 }} / {{ photoStore.pagination.totalPages }}
      </span>

      <button
        @click="changePage(photoStore.pagination.currentPage + 1)"
        :disabled="photoStore.pagination.currentPage >= photoStore.pagination.totalPages - 1"
        class="btn btn-pagination"
      >
        下一页
      </button>
    </div>
  </div>
</template>

<script>
import { usePhotoStore } from '../stores/index';
import { photoAPI } from '../services/api';

export default {
  name: 'GalleryView',
  setup() {
    const photoStore = usePhotoStore();
    return { photoStore };
  },
  data() {
    return {
      visibilityFilter: '',
      tagSearch: '',
      currentPage: 0,
      itemsPerPage: 10,
    }
  },
  async mounted() {
    await this.fetchPhotos();
  },
  methods: {
    async fetchPhotos() {
      try {
        const params = {
          page: this.currentPage,
          size: this.itemsPerPage
        };

        if (this.visibilityFilter) {
          params.visibility = this.visibilityFilter;
        }

        await this.photoStore.fetchPhotos(params);
      } catch (error) {
        console.error('Failed to fetch photos:', error);
        alert('加载照片失败，请重试');
      }
    },

    async searchByTag() {
      if (!this.tagSearch.trim()) {
        await this.fetchPhotos();
        return;
      }

      try {
        const response = await photoAPI.searchPhotos(this.tagSearch.trim());
        this.photoStore.setPhotos(response.data);
      } catch (error) {
        console.error('Search failed:', error);
        alert('搜索失败，请重试');
      }
    },

    async changePage(page) {
      if (page < 0 || page >= this.photoStore.pagination.totalPages) {
        return;
      }

      this.currentPage = page;
      await this.fetchPhotos();
    },

    viewPhoto(photo) {
      this.photoStore.setCurrentPhoto(photo);
      this.$router.push(`/photo/${photo.id}`);
    },

    async downloadPhoto(photo) {
      try {
        // 使用API下载照片
        const response = await photoAPI.downloadPhoto(photo.id);

        // 创建blob URL
        const blob = new Blob([response.data], { type: response.headers['content-type'] });
        const url = window.URL.createObjectURL(blob);

        // 创建一个隐藏的下载链接
        const link = document.createElement('a');
        link.href = url;
        link.download = photo.originalName;
        document.body.appendChild(link);
        link.click();

        // 清理
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
      } catch (error) {
        console.error('Download failed:', error);
        alert('下载失败，请重试');
      }
    },

    async deletePhoto(photo) {
      if (!confirm(`确定要删除照片 "${photo.originalName}" 吗？`)) {
        return;
      }

      try {
        await this.photoStore.deletePhoto(photo.id);
        // 刷新照片列表
        await this.fetchPhotos();
      } catch (error) {
        console.error('Delete failed:', error);
        alert('删除失败，请重试');
      }
    },

    editPhoto(photo) {
      this.photoStore.setCurrentPhoto(photo);
      this.$router.push(`/photo/${photo.id}/edit`);
    },

    truncateText(text, maxLength) {
      if (!text) return '';
      return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
    },

    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleDateString('zh-CN');
    },

    getPhotoImageUrl(photo) {
      // 调试信息
      console.log('Photo object:', photo);
      console.log('File type:', photo?.fileType);
      console.log('Thumbnail URL:', photo?.thumbnailUrl);
      console.log('File URL:', photo?.fileUrl);
      console.log('Has raw preview?', this.hasRawPreview(photo));

      if (!photo) return this.getDefaultImage();

      // 对于RAW文件，尝试多个可能的预览图字段
      if (photo.fileType === 'RAW') {
        // 优先使用缩略图
        if (photo.thumbnailUrl) {
          return photo.thumbnailUrl;
        }
        // 尝试其他预览URL
        const possiblePreviewUrls = [
          photo.previewUrl,           // 预览图URL
          photo.jpegUrl,              // JPEG格式URL
          photo.convertedUrl,         // 转换后URL
          photo.fileUrl?.replace(/\.(cr2|nef|arw|raw|dng)$/i, '.jpg')  // 尝试将扩展名改为.jpg
        ];
        const validUrl = possiblePreviewUrls.find(url =>
          url && typeof url === 'string' && url.trim().length > 0
        );
        if (validUrl) {
          console.log(`Found RAW preview URL for ${photo.originalName}:`, validUrl);
          return validUrl;
        }
        console.warn(`RAW file ${photo.originalName} has no preview available.`);
        return this.getRawDefaultImage();
      }

      // 对于其他文件，优先使用缩略图，如果不可用则使用原图
      const url = photo.thumbnailUrl || photo.fileUrl;
      return url || this.getDefaultImage();
    },

    getDefaultImage() {
      // 返回一个简单的SVG作为默认图片
      return 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="300" height="300"><rect width="300" height="300" fill="#f0f0f0"/><text x="150" y="150" font-family="Arial" font-size="16" text-anchor="middle" dy=".3em" fill="#666">No Image</text></svg>';
    },

    getRawDefaultImage() {
      // RAW文件专用的默认图像
      return 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="300" height="300"><rect width="300" height="300" fill="#e3f2fd"/><text x="150" y="150" font-family="Arial" font-size="14" text-anchor="middle" dy=".3em" fill="#1565c0">RAW File</text><text x="150" y="180" font-family="Arial" font-size="12" text-anchor="middle" dy=".3em" fill="#1976d2">No Preview Available</text></svg>';
    },

    // 检查RAW文件是否有预览图
    hasRawPreview(photo) {
      if (!photo || photo.fileType !== 'RAW') return false;

      const possiblePreviewUrls = [
        photo.previewUrl,           // 预览图URL
        photo.jpegUrl,              // JPEG格式URL
        photo.convertedUrl,         // 转换后URL
        photo.thumbnailUrl,         // 缩略图URL
        photo.fileUrl?.replace(/\.(cr2|nef|arw|raw|dng)$/i, '.jpg')  // 尝试将扩展名改为.jpg
      ];

      return possiblePreviewUrls.some(url =>
        url && typeof url === 'string' && url.trim().length > 0
      );
    },

    onImageLoad(event) {
      const imgSrc = event.target.src;
      console.log('Image loaded successfully:', imgSrc);
    },

    onImageError(event) {
      const imgSrc = event.target.src;
      console.warn('Image load error for:', imgSrc);

      // 如果是RAW文件，使用RAW专用默认图像
      const isRawFile = imgSrc.includes('/thumbnails/thumb_') && (imgSrc.includes('.nef') || imgSrc.includes('.cr2') || imgSrc.includes('.arw'));
      if (isRawFile) {
        console.log('RAW file preview failed, using RAW default image');
        event.target.src = this.getRawDefaultImage();
      } else {
        event.target.src = this.getDefaultImage();
      }
    }
  } // <--- 修复：这里闭合 methods 对象
} // <--- 修复：这里闭合 export default 对象
</script>

<style scoped>
.gallery-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.filters {
  display: flex;
  gap: 2rem;
  margin-bottom: 2rem;
  flex-wrap: wrap;
  align-items: end;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

label {
  font-weight: bold;
}

input, select {
  padding: 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.btn-search {
  background-color: #007bff;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
}

.loading {
  text-align: center;
  padding: 2rem;
  font-size: 1.2rem;
}

.empty-state {
  text-align: center;
  padding: 3rem;
  color: #666;
}

.gallery-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.photo-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.photo-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
}

.photo-image {
  height: 200px;
  overflow: hidden;
}

.photo-image img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background-color: #f8f9fa;
}

.photo-info {
  padding: 1rem;
}

.photo-info h3 {
  margin: 0 0 0.5rem 0;
  font-size: 1rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.photo-description {
  color: #666;
  font-size: 0.9rem;
  margin: 0 0 0.5rem 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.photo-meta {
  display: flex;
  justify-content: space-between;
  font-size: 0.8rem;
  color: #999;
  margin-bottom: 0.5rem;
}

.visibility {
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  font-size: 0.7rem;
  font-weight: bold;
}

.visibility.public {
  background-color: #d4edda;
  color: #155724;
}

.visibility.private {
  background-color: #f8d7da;
  color: #721c24;
}

.visibility.friends {
  background-color: #d1ecf1;
  color: #0c5460;
}

.photo-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.3rem;
  margin-bottom: 0.5rem;
}

.tag {
  background-color: #e9ecef;
  color: #495057;
  padding: 0.2rem 0.5rem;
  border-radius: 12px;
  font-size: 0.7rem;
}

.tag-more {
  background-color: #adb5bd;
  color: white;
  padding: 0.2rem 0.5rem;
  border-radius: 12px;
  font-size: 0.7rem;
}

.photo-actions {
  display: flex;
  gap: 0.5rem;
  padding: 0 1rem 1rem 1rem;
}

.btn {
  padding: 0.5rem 1rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  text-decoration: none;
  display: inline-block;
  text-align: center;
  transition: opacity 0.3s;
}

.btn-small {
  padding: 0.25rem 0.5rem;
  font-size: 0.8rem;
}

.btn-primary {
  background-color: #007bff;
  color: white;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.btn-danger {
  background-color: #dc3545;
  color: white;
}

.btn-pagination {
  background-color: #007bff;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
}

.btn-pagination:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
  opacity: 0.6;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 2rem;
}

.file-type-tag {
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  font-size: 0.7rem;
  font-weight: bold;
}

.file-type-tag.raw {
  background-color: #e3f2fd;
  color: #1565c0;
}

.file-type-tag.image {
  background-color: #f3e5f5;
  color: #7b1fa2;
}

.raw-file-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  z-index: 2;
}

.raw-file-info {
  text-align: center;
  padding: 1rem;
  background: rgba(30, 30, 30, 0.8);
  border-radius: 8px;
  max-width: 80%;
}

.raw-icon {
  font-size: 2rem;
  display: block;
  margin-bottom: 0.5rem;
}

.raw-text {
  font-size: 1.1rem;
  font-weight: bold;
  display: block;
  margin-bottom: 0.25rem;
}

.raw-hint {
  font-size: 0.9rem;
  opacity: 0.9;
  display: block;
}

/* 确保photo-image有相对定位 */
.photo-image {
  position: relative;
}

.page-info {
  padding: 0.5rem 1rem;
  color: #333;
}
</style>