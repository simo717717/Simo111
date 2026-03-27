<template>
  <div class="photo-edit-page">
    <h1>编辑照片</h1>

    <div v-if="photoStore.currentPhoto" class="edit-container">
      <div class="photo-preview-section">
        <div class="photo-preview">
          <img
            :src="getImageUrl(photoStore.currentPhoto)"
            :alt="photoStore.currentPhoto.description || photoStore.currentPhoto.originalName"
            @error="onImageError"
          >
        </div>
        <div class="photo-info-quick">
          <h2>{{ photoStore.currentPhoto.originalName }}</h2>
          <div class="file-info">
            <div class="info-item">
              <strong>文件类型:</strong>
              <span class="file-type-tag" :class="photoStore.currentPhoto.fileType.toLowerCase()">
                {{ photoStore.currentPhoto.fileType }}
              </span>
            </div>
            <div class="info-item">
              <strong>文件大小:</strong> {{ formatFileSize(photoStore.currentPhoto.fileSize) }}
            </div>
            <div class="info-item">
              <strong>上传时间:</strong> {{ formatDate(photoStore.currentPhoto.createdAt) }}
            </div>
          </div>
        </div>
      </div>

      <div class="edit-form-section">
        <form @submit.prevent="saveChanges" class="edit-form">
          <div class="form-group">
            <label for="description">照片描述</label>
            <textarea
              id="description"
              v-model="formData.description"
              placeholder="为这张照片添加描述..."
              rows="4"
            ></textarea>
          </div>

          <div class="form-group">
            <label>标签</label>
            <TagSelector
              :availableTags="availableTags"
              :aiTags="aiGeneratedTags"
              v-model="formData.tags"
            />
            <p class="help-text">勾选AI标签或添加自定义标签</p>
            <div v-if="photoStore.currentPhoto.tags && photoStore.currentPhoto.tags.length > 0" class="current-tags">
              <span class="current-tags-label">当前标签:</span>
              <span
                v-for="(tag, index) in photoStore.currentPhoto.tags"
                :key="index"
                class="tag"
              >
                {{ tag }}
              </span>
            </div>
          </div>

          <div class="form-group">
            <label for="visibility">可见性</label>
            <select id="visibility" v-model="formData.visibility">
              <option value="PUBLIC">公开</option>
              <option value="PRIVATE">私有</option>
              <option value="FRIENDS">好友可见</option>
            </select>
          </div>

          <div class="form-group">
            <label class="checkbox-label">
              <input type="checkbox" v-model="formData.autoTag">
              自动生成AI标签
            </label>
            <p class="help-text">勾选此项将使用AI重新分析照片内容并生成标签</p>
          </div>

          <div class="form-actions">
            <button type="submit" class="btn btn-primary" :disabled="saving">
              {{ saving ? '保存中...' : '保存修改' }}
            </button>
            <button type="button" class="btn btn-secondary" @click="cancelEdit">
              取消
            </button>
            <button type="button" class="btn btn-danger" @click="deletePhoto">
              删除照片
            </button>
          </div>
        </form>
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
import TagSelector from '../components/TagSelector.vue';

export default {
  name: 'PhotoEditView',
  components: {
    TagSelector
  },
  setup() {
    const photoStore = usePhotoStore();
    return { photoStore };
  },
  data() {
    return {
      formData: {
        description: '',
        visibility: 'PRIVATE',
        autoTag: false,
        tags: []
      },
      // 可用标签列表（核心类别标签）
      availableTags: ['动物', '建筑', '风景', '人像', '美食', '交通工具', '自然'],
      // AI生成的标签（初始为空，勾选autoTag后生成）
      aiGeneratedTags: [],
      saving: false
    }
  },
  watch: {
    'formData.autoTag'(newVal) {
      if (newVal && this.photoStore.currentPhoto) {
        // 模拟AI标签生成（实际应调用API）
        this.generateAITags();
      } else {
        this.aiGeneratedTags = [];
      }
    }
  },
  async mounted() {
    await this.loadPhoto();
    this.initializeForm();
  },
  methods: {
    async loadPhoto() {
      const photoId = this.$route.params.id;
      try {
        const response = await photoAPI.getPhoto(photoId);
        this.photoStore.setCurrentPhoto(response.data);
      } catch (error) {
        console.error('Failed to load photo:', error);
        alert('加载照片失败，请重试');
        this.$router.push('/gallery');
      }
    },

    initializeForm() {
      if (this.photoStore.currentPhoto) {
        const photo = this.photoStore.currentPhoto;
        this.formData.description = photo.description || '';
        this.formData.visibility = photo.visibility;
        this.formData.autoTag = false; // 默认不重新生成AI标签

        // 设置标签数组
        if (photo.tags && photo.tags.length > 0) {
          this.formData.tags = [...photo.tags];
        }
      }
    },

    generateAITags() {
      // 模拟AI标签生成（实际应调用后端API）
      // 根据当前照片的标签、描述或文件名猜测多个类别
      const photo = this.photoStore.currentPhoto;
      let allTags = [];

      // 定义关键词与类别的映射（支持中英文）
      const keywordMapping = [
        { keywords: ['cat', 'dog', 'pet', 'animal', 'bird', 'fish', '猫', '狗', '宠物', '动物', '鸟', '鱼'], tag: '动物' },
        { keywords: ['mountain', 'landscape', 'nature', 'sea', 'beach', 'forest', 'river', 'lake', 'sunset', 'sunrise', 'view', 'scenery', 'panorama', 'horizon', 'vista', 'countryside', '山', '风景', '自然', '海', '沙滩', '森林', '河', '湖', '日落', '日出', '景色', '全景', '地平线', '乡村'], tag: '风景' },
        { keywords: ['building', 'architecture', 'house', 'church', 'tower', 'bridge', 'city', 'skyscraper', 'office', 'home', 'apartment', 'road', 'street', 'station', 'tunnel', 'urban', 'downtown', 'commercial', 'business', 'mall', 'shop', 'store', 'hotel', 'hospital', 'school', 'university', 'campus', 'facade', 'exterior', 'window', 'door', 'roof', '建筑', '建筑物', '房子', '教堂', '塔', '桥', '城市', '摩天楼', '办公楼', '家', '公寓', '道路', '街道', '车站', '隧道', '城市', '市区', '商业区', '商业', '商场', '商店', '酒店', '医院', '学校', '大学', '校园', '外墙', '外观', '窗户', '门', '屋顶'], tag: '建筑' },
        { keywords: ['portrait', 'people', 'person', 'face', 'human', 'man', 'woman', 'child', '人像', '人物', '人脸', '肖像', '人', '男人', '女人', '儿童'], tag: '人像' },
        { keywords: ['food', 'meal', 'restaurant', 'dish', 'fruit', 'vegetable', 'cake', '美食', '食物', '餐厅', '菜肴', '水果', '蔬菜', '蛋糕'], tag: '美食' },
        { keywords: ['car', 'vehicle', 'bicycle', 'motorcycle', 'bus', 'train', 'plane', 'road', 'street', 'highway', 'bike', 'ship', 'boat', 'truck', 'taxi', 'metro', 'subway', '汽车', '车辆', '自行车', '摩托车', '公交车', '火车', '飞机', '道路', '街道', '公路', '自行车', '船', '卡车', '出租车', '地铁'], tag: '交通工具' },
        { keywords: ['tree', 'flower', 'plant', 'garden', 'park', 'sky', 'cloud', 'grass', 'field', 'leaf', 'wood', 'stone', 'rock', '树', '花', '植物', '花园', '公园', '天空', '云', '草地', '田野', '叶子', '木头', '石头', '岩石'], tag: '自然' }
      ];

      // 辅助函数：在文本中查找所有匹配的类别
      const findAllMatchingTags = (text) => {
        const lowerText = text.toLowerCase();
        const matchedTags = [];
        for (const mapping of keywordMapping) {
          if (mapping.keywords.some(keyword => lowerText.includes(keyword))) {
            matchedTags.push(mapping.tag);
          }
        }
        return matchedTags;
      };

      // 从现有标签推断（收集所有匹配）
      if (photo.tags && photo.tags.length > 0) {
        for (const tag of photo.tags) {
          const matchedTags = findAllMatchingTags(tag);
          if (matchedTags.length > 0) {
            allTags.push(...matchedTags);
          }
        }
      }

      // 从描述推断（收集所有匹配）
      if (photo.description) {
        const matchedTags = findAllMatchingTags(photo.description);
        if (matchedTags.length > 0) {
          allTags.push(...matchedTags);
        }
      }

      // 从文件名推断（收集所有匹配）
      if (photo.originalName) {
        const matchedTags = findAllMatchingTags(photo.originalName);
        if (matchedTags.length > 0) {
          allTags.push(...matchedTags);
        }
      }

      // 如果没有找到任何匹配，随机选择1-2个常见类别
      if (allTags.length === 0) {
        // 如果没有找到任何匹配，使用智能加权选择
        // 优先选择风景和自然，避免不相关的动物标签
        const categoryWeights = [
          { tag: '风景', weight: 40 },
          { tag: '自然', weight: 35 },
          { tag: '建筑', weight: 30 },
          { tag: '人像', weight: 25 },
          { tag: '美食', weight: 15 },
          { tag: '交通工具', weight: 20 },
          { tag: '动物', weight: 5 }  // 通用照片中动物的概率很低
        ];

        const selectedTags = [];
        const totalWeight = categoryWeights.reduce((sum, cat) => sum + cat.weight, 0);

        // 选择1-2个标签
        const numTagsToSelect = Math.floor(Math.random() * 2) + 1; // 1-2个
        while (selectedTags.length < numTagsToSelect) {
          let random = Math.random() * totalWeight;
          for (const category of categoryWeights) {
            random -= category.weight;
            if (random <= 0 && !selectedTags.includes(category.tag)) {
              selectedTags.push(category.tag);
              break;
            }
          }
        }

        allTags.push(...selectedTags);
      }

      this.aiGeneratedTags = [...new Set(allTags)]; // 去重
    },

    async saveChanges() {
      if (this.saving) return;

      this.saving = true;
      try {
        const photoId = this.$route.params.id;

        // 准备查询参数
        const params = {
          description: this.formData.description,
          visibility: this.formData.visibility,
          autoTag: this.formData.autoTag.toString()
        };

        // 处理标签
        if (this.formData.tags && this.formData.tags.length > 0) {
          params.tags = this.formData.tags.join(',');
        }

        // 调用更新API
        const response = await photoAPI.updatePhoto(photoId, params);

        // 更新store中的当前照片
        this.photoStore.setCurrentPhoto(response.data);

        alert('照片信息已成功更新！');

        // 返回照片详情页
        this.$router.push(`/photo/${photoId}`);
      } catch (error) {
        console.error('Failed to save changes:', error);
        alert('保存失败，请重试');
      } finally {
        this.saving = false;
      }
    },

    cancelEdit() {
      const photoId = this.$route.params.id;
      this.$router.push(`/photo/${photoId}`);
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

      // 在编辑页优先使用原图以获得最佳质量
      const url = photo.fileUrl || photo.thumbnailUrl;
      return url || this.getDefaultImage();
    },

    getDefaultImage() {
      // 返回一个简单的SVG作为默认图片
      return 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="300" height="300" viewBox="0 0 300 300"><rect width="300" height="300" fill="#f0f0f0"/><text x="150" y="150" font-family="Arial" font-size="16" text-anchor="middle" dy=".3em" fill="#666">No Image</text></svg>';
    },

    getRawDefaultImage() {
      // RAW文件专用的默认图像
      return 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="300" height="300"><rect width="300" height="300" fill="#e3f2fd"/><text x="150" y="150" font-family="Arial" font-size="14" text-anchor="middle" dy=".3em" fill="#1565c0">RAW File</text><text x="150" y="180" font-family="Arial" font-size="12" text-anchor="middle" dy=".3em" fill="#1976d2">No Preview Available</text></svg>';
    },

    onImageError(event) {
      event.target.src = this.getDefaultImage();
    }
  }
}
</script>

<style scoped>
.photo-edit-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.edit-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
  margin-top: 2rem;
}

.photo-preview-section {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.photo-preview {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.photo-preview img {
  width: 100%;
  height: auto;
  display: block;
}

.photo-info-quick h2 {
  margin: 0 0 1rem 0;
  font-size: 1.3rem;
  word-break: break-all;
}

.file-info {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.file-type-tag {
  padding: 0.2rem 0.75rem;
  border-radius: 4px;
  font-size: 0.8rem;
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

.edit-form-section {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.edit-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

label {
  font-weight: bold;
  color: #333;
}

textarea,
input[type="text"],
select {
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

textarea {
  resize: vertical;
  min-height: 100px;
}

.current-tags {
  margin-top: 0.5rem;
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  align-items: center;
}

.current-tags-label {
  font-size: 0.9rem;
  color: #666;
}

.tag {
  background-color: #e9ecef;
  color: #495057;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.8rem;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
}

.help-text {
  font-size: 0.85rem;
  color: #666;
  margin-top: 0.25rem;
}

.form-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1rem;
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
}

.btn-primary {
  background-color: #007bff;
  color: white;
}

.btn-primary:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
  opacity: 0.6;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.btn-danger {
  background-color: #dc3545;
  color: white;
}

.btn:hover:not(:disabled) {
  opacity: 0.9;
}

.loading {
  text-align: center;
  padding: 3rem;
  font-size: 1.2rem;
  color: #666;
}

@media (max-width: 768px) {
  .edit-container {
    grid-template-columns: 1fr;
  }

  .form-actions {
    flex-direction: column;
  }
}
</style>