<template>
  <div class="photo-detail-page">
    <div v-if="photoStore.currentPhoto" class="photo-detail-container">
      <div class="photo-display">
        <img
          :src="getImageUrl(photoStore.currentPhoto)"
          :alt="photoStore.currentPhoto.description || photoStore.currentPhoto.originalName"
          @error="onImageError"
        >
      </div>

      <div class="photo-info-panel">
        <div class="photo-basic-info">
          <h1>{{ photoStore.currentPhoto.originalName }}</h1>
          <p class="photo-description">{{ photoStore.currentPhoto.description }}</p>

          <div class="photo-meta">
            <div class="meta-item">
              <strong>文件名:</strong> {{ photoStore.currentPhoto.filename }}
            </div>
            <div class="meta-item">
              <strong>文件大小:</strong> {{ formatFileSize(photoStore.currentPhoto.fileSize) }}
            </div>
            <div class="meta-item">
              <strong>上传时间:</strong> {{ formatDate(photoStore.currentPhoto.createdAt) }}
            </div>
            <div class="meta-item">
              <strong>更新时间:</strong> {{ formatDate(photoStore.currentPhoto.updatedAt) }}
            </div>
            <div class="meta-item">
              <strong>可见性:</strong>
              <span class="visibility-tag" :class="photoStore.currentPhoto.visibility.toLowerCase()">
                {{ photoStore.currentPhoto.visibility }}
              </span>
            </div>
          </div>

          <div v-if="photoStore.currentPhoto.tags && photoStore.currentPhoto.tags.length > 0" class="tags-section">
            <h3>标签</h3>
            <div class="tags-list">
              <span
                v-for="(tag, index) in photoStore.currentPhoto.tags"
                :key="index"
                class="tag"
              >
                {{ tag }}
              </span>
            </div>
          </div>

          <div v-if="photoStore.currentPhoto.exifData" class="exif-section">
            <h3>EXIF信息</h3>
            <div class="exif-details">
              <div v-if="photoStore.currentPhoto.cameraModel" class="exif-item">
                <strong>相机型号:</strong> {{ photoStore.currentPhoto.cameraModel }}
              </div>
              <div v-if="photoStore.currentPhoto.aperture" class="exif-item">
                <strong>光圈:</strong> {{ photoStore.currentPhoto.aperture }}
              </div>
              <div v-if="photoStore.currentPhoto.shutterSpeed" class="exif-item">
                <strong>快门速度:</strong> {{ photoStore.currentPhoto.shutterSpeed }}
              </div>
              <div v-if="photoStore.currentPhoto.iso" class="exif-item">
                <strong>ISO:</strong> {{ photoStore.currentPhoto.iso }}
              </div>
              <div v-if="photoStore.currentPhoto.focalLength" class="exif-item">
                <strong>焦距:</strong> {{ photoStore.currentPhoto.focalLength }}mm
              </div>
              <div v-if="photoStore.currentPhoto.location" class="exif-item">
                <strong>位置:</strong> {{ photoStore.currentPhoto.location }}
              </div>
            </div>
          </div>
        </div>

        <div class="photo-actions">
          <button @click="downloadPhoto" class="btn btn-primary">
            下载原图
          </button>
          <button @click="editPhoto" class="btn btn-secondary">
            编辑
          </button>
          <button @click="sharePhoto" class="btn btn-secondary">
            分享
          </button>
          <button @click="deletePhoto" class="btn btn-danger">
            删除
          </button>
        </div>
      </div>
    </div>

    <div v-else class="loading">
      加载中...
    </div>
  </div>
</template>

<script>
import { usePhotoStore } from '../stores/index';
import { photoAPI } from '../services/api';

export default {
  name: 'PhotoDetailView',
  props: ['id'],
  setup() {
    const photoStore = usePhotoStore();
    return { photoStore };
  },
  async mounted() {
    await this.loadPhoto();
  },
  methods: {
    async loadPhoto() {
      try {
        // 在实际应用中，我们会通过API获取特定照片
        // 这里我们假设照片已经在store中
        if (!this.photoStore.currentPhoto) {
          // 如果当前没有选中的照片，尝试从API获取
          const response = await photoAPI.getPhoto(this.$route.params.id);
          this.photoStore.setCurrentPhoto(response.data);
        }
      } catch (error) {
        console.error('Failed to load photo:', error);
        this.$router.push('/gallery');
      }
    },

    async downloadPhoto() {
      try {
        const photo = this.photoStore.currentPhoto;

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

    editPhoto() {
      const photoId = this.photoStore.currentPhoto.id;
      this.$router.push(`/photo/${photoId}/edit`);
    },

    sharePhoto() {
      const photo = this.photoStore.currentPhoto;
      const shareUrl = `${window.location.origin}/photo/${photo.id}`;

      if (navigator.share) {
        navigator.share({
          title: photo.originalName,
          text: photo.description || 'Check out this photo!',
          url: shareUrl
        }).catch(console.error);
      } else {
        // 复制到剪贴板
        navigator.clipboard.writeText(shareUrl).then(() => {
          alert('链接已复制到剪贴板');
        }).catch(console.error);
      }
    },

    async deletePhoto() {
      if (!confirm(`确定要删除照片 "${this.photoStore.currentPhoto.originalName}" 吗？`)) {
        return;
      }

      try {
        await this.photoStore.deletePhoto(this.photoStore.currentPhoto.id);
        this.$router.push('/gallery');
      } catch (error) {
        console.error('Delete failed:', error);
        alert('删除失败，请重试');
      }
    },

    formatFileSize(bytes) {
      if (bytes === 0) return '0 Bytes';
      const k = 1024;
      const sizes = ['Bytes', 'KB', 'MB', 'GB'];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    },

    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleDateString('zh-CN');
    },

    getDefaultImage() {
      // 返回一个简单的SVG作为默认图片
      return 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="300" height="300" viewBox="0 0 300 300"><rect width="300" height="300" fill="#f0f0f0"/><text x="150" y="150" font-family="Arial" font-size="16" text-anchor="middle" dy=".3em" fill="#666">No Image</text></svg>';
    },

    getRawDefaultImage() {
      // RAW文件专用的默认图像
      return 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="300" height="300"><rect width="300" height="300" fill="#e3f2fd"/><text x="150" y="150" font-family="Arial" font-size="14" text-anchor="middle" dy=".3em" fill="#1565c0">RAW File</text><text x="150" y="180" font-family="Arial" font-size="12" text-anchor="middle" dy=".3em" fill="#1976d2">No Preview Available</text></svg>';
    },

    getImageUrl(photo) {
      if (!photo) return this.getDefaultImage();

      // 对于RAW文件，尝试多个可能的预览图字段
      if (photo.fileType === 'RAW') {
        // 尝试获取RAW文件的JPEG预览图
        const possiblePreviewUrls = [
          photo.previewUrl,           // 预览图URL
          photo.jpegUrl,              // JPEG格式URL
          photo.convertedUrl,         // 转换后URL
          photo.thumbnailUrl,         // 缩略图URL
          photo.fileUrl?.replace(/\.(cr2|nef|arw|raw|dng)$/i, '.jpg')  // 尝试将扩展名改为.jpg
        ];

        const validUrl = possiblePreviewUrls.find(url =>
          url && typeof url === 'string' && url.trim().length > 0
        );

        if (validUrl) {
          console.log(`Found RAW preview URL for ${photo.originalName}:`, validUrl);
          return validUrl;
        }

        // 如果没有找到预览图，显示RAW专用图像并记录警告
        console.warn(`RAW file ${photo.originalName} has no preview available. File type: ${photo.fileType}, Thumbnail URL: ${photo.thumbnailUrl}`);
        return this.getRawDefaultImage();
      }

      // 在详情页优先使用原图以获得最佳质量
      const url = photo.fileUrl || photo.thumbnailUrl;
      return url || this.getDefaultImage();
    },

    onImageError(event) {
      event.target.src = this.getDefaultImage();
    }
  }
}
</script>

<style scoped>
.photo-detail-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.photo-detail-container {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 2rem;
  align-items: start;
}

.photo-display {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.photo-display img {
  width: 100%;
  height: auto;
  display: block;
}

.photo-info-panel {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.photo-basic-info h1 {
  margin: 0 0 1rem 0;
  font-size: 1.5rem;
}

.photo-description {
  color: #666;
  margin: 0 0 1.5rem 0;
  line-height: 1.6;
}

.photo-meta {
  margin-bottom: 1.5rem;
}

.meta-item {
  margin-bottom: 0.75rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid #eee;
}

.visibility-tag {
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: bold;
}

.visibility-tag.public {
  background-color: #d4edda;
  color: #155724;
}

.visibility-tag.private {
  background-color: #f8d7da;
  color: #721c24;
}

.visibility-tag.friends {
  background-color: #d1ecf1;
  color: #0c5460;
}

.tags-section h3,
.exif-section h3 {
  margin: 1.5rem 0 1rem 0;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid #eee;
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.tag {
  background-color: #e9ecef;
  color: #495057;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
}

.exif-details {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.exif-item strong {
  display: inline-block;
  width: 100px;
  color: #333;
}

.photo-actions {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid #eee;
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  text-decoration: none;
  display: inline-block;
  text-align: center;
  transition: opacity 0.3s;
  text-align: center;
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

.loading {
  text-align: center;
  padding: 3rem;
  font-size: 1.2rem;
  color: #666;
}

@media (max-width: 768px) {
  .photo-detail-container {
    grid-template-columns: 1fr;
  }
}
</style>