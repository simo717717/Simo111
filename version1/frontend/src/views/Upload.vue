<template>
  <div class="upload-page">
    <h1>上传照片</h1>

    <div class="upload-container">
      <div
        class="drop-zone"
        :class="{ 'drag-over': isDragOver }"
        @dragover.prevent="handleDragOver"
        @dragleave.prevent="isDragOver = false"
        @drop.prevent="handleDrop"
        @click="triggerFileSelect"
      >
        <div class="drop-zone-content">
          <div class="upload-icon">📁</div>
          <p v-if="!selectedFile">拖拽照片到这里或点击选择文件</p>
          <p v-else>{{ selectedFile.name }} ({{ formatFileSize(selectedFile.size) }})</p>
          <input
            type="file"
            ref="fileInput"
            accept="image/*,.dng,.cr2,.nef,.arw,.orf,.rw2,.pef,.sr2,.x3f,.raf"
            @change="handleFileSelect"
            style="display: none"
          >
        </div>
      </div>

      <!-- 图片预览 -->
      <div v-if="previewUrl" class="image-preview">
        <h3>图片预览</h3>
        <div class="preview-container">
          <img :src="previewUrl" :alt="selectedFile.name" @load="onPreviewLoad" @error="onPreviewError">
        </div>
        <p class="preview-filename">{{ selectedFile.name }}</p>
      </div>

      <div v-if="selectedFile" class="upload-form">
    <div class="form-group">
      <label for="description">照片描述</label>
      <textarea
        id="description"
        v-model="formData.description"
        placeholder="为这张照片添加描述..."
        rows="3"
      ></textarea>
    </div>

    <div class="form-group">
      <label>标签</label>
      <TagSelector
        :availableTags="availableTags"
        :aiTags="aiTagsForDisplay"
        v-model="formData.tags"
      />
      <p class="help-text">勾选AI标签或添加自定义标签</p>
    </div>

    <div class="form-group">
      <label for="visibility">可见性</label>
      <select id="visibility" v-model="formData.visibility">
        <option value="PUBLIC">公开</option>
        <option value="PRIVATE">私有</option>
        <option value="FRIENDS">好友</option>
      </select>
    </div>

    <div class="form-group">
      <label class="checkbox-label">
        <input type="checkbox" v-model="formData.autoTag">
        自动生成AI标签
      </label>
      <p class="help-text">勾选此项将使用AI分析照片内容并自动生成标签</p>
    </div>

        <div class="form-actions">
          <button
            @click="resetForm"
            class="btn btn-secondary"
          >
            重新选择
          </button>
          <button
            @click="uploadPhoto"
            :disabled="isUploading"
            class="btn btn-primary"
          >
            {{ isUploading ? '上传中...' : '上传照片' }}
          </button>
        </div>
      </div>
    </div>

    <div v-if="uploadResult" class="upload-result">
      <div v-if="uploadResult.success" class="success-message">
        照片上传成功！
      </div>
      <div v-else class="error-message">
        上传失败: {{ uploadResult.message }}
      </div>
    </div>
  </div>
</template>

<script>
import { usePhotoStore } from '../stores/index';
import TagSelector from '../components/TagSelector.vue';
import { aiAPI } from '../services/api.js';

export default {
  name: 'UploadView',
  components: {
    TagSelector
  },
  setup() {
    const photoStore = usePhotoStore();
    return { photoStore };
  },
  data() {
    return {
      selectedFile: null,
      previewUrl: null,
      isDragOver: false,
      isUploading: false,
      formData: {
        description: '',
        tags: [],
        visibility: 'PRIVATE',
        autoTag: false
      },
      // 可用标签列表（核心类别标签）
      availableTags: ['动物', '建筑', '风景', '人像', '美食', '交通工具', '自然'],
      // AI生成的标签（初始为空，勾选autoTag后生成）
      aiGeneratedTags: [],
      aiAnalyzing: false,
      uploadResult: null
    }
  },
  watch: {
    'formData.autoTag'(newVal) {
      if (newVal && this.selectedFile) {
        // 调用AI分析API生成标签
        this.generateAITags();
      } else {
        this.aiGeneratedTags = [];
      }
    }
  },
  computed: {
    aiTagsForDisplay() {
      if (this.aiAnalyzing) {
        return ['正在分析中...'];
      }
      return this.aiGeneratedTags;
    }
  },
  beforeDestroy() {
    // 释放预览URL
    if (this.previewUrl) {
      URL.revokeObjectURL(this.previewUrl);
    }
  },
  methods: {
    async generateAITags() {
      if (!this.selectedFile) {
        this.aiGeneratedTags = [];
        return;
      }

      this.aiAnalyzing = true;

      try {
        // 创建FormData对象
        const formData = new FormData();
        formData.append('file', this.selectedFile);

        // 调用后端AI分析API
        const response = await aiAPI.analyzeImage(formData);

        console.log('AI分析响应:', response.data);
        if (response.data.success && response.data.tags) {
          this.aiGeneratedTags = response.data.tags;
          console.log('AI标签:', this.aiGeneratedTags);
        } else {
          console.error('AI分析失败:', response.data.message);
          this.aiGeneratedTags = [];
        }
      } catch (error) {
        console.error('调用AI分析API失败:', error);
        this.aiGeneratedTags = [];
      } finally {
        this.aiAnalyzing = false;
      }
    },

    triggerFileSelect() {
      this.$refs.fileInput.click();
    },

    handleDragOver() {
      this.isDragOver = true;
    },

    handleDrop(e) {
      this.isDragOver = false;
      const files = e.dataTransfer.files;
      if (files.length > 0) {
        this.selectFile(files[0]);
      }
    },

    handleFileSelect(e) {
      const file = e.target.files[0];
      if (file) {
        this.selectFile(file);
      }
    },

    selectFile(file) {
      // 验证文件类型
      const validTypes = [
        'image/jpeg',
        'image/png',
        'image/gif',
        'image/webp',
        'image/tiff',
        'image/bmp'
      ];

      // 也允许RAW格式文件
      const rawTypes = ['.dng', '.cr2', '.nef', '.arw', '.orf', '.rw2', '.pef', '.sr2', '.x3f', '.raf'];
      const fileExt = file.name.toLowerCase().split('.').pop();

      if (!validTypes.includes(file.type) && !rawTypes.includes('.' + fileExt)) {
        alert('请选择有效的图片文件 (JPEG, PNG, GIF, WEBP, TIFF, BMP 或 RAW 格式)');
        return;
      }

      // 验证文件大小 (最大10MB)
      if (file.size > 100 * 1024 * 1024) {
        alert('文件大小不能超过100MB');
        return;
      }

      // 释放之前的预览URL
      if (this.previewUrl) {
        URL.revokeObjectURL(this.previewUrl);
        this.previewUrl = null;
      }

      this.selectedFile = file;
      this.uploadResult = null;

      // 创建图片预览URL（仅限可预览的图片格式）
      if (file.type.startsWith('image/') && !file.type.includes('raw')) {
        this.previewUrl = URL.createObjectURL(file);
      }
    },

    async uploadPhoto() {
      if (!this.selectedFile) {
        alert('请选择要上传的照片');
        return;
      }

      this.isUploading = true;
      this.uploadResult = null;

      try {
        const formData = new FormData();
        formData.append('file', this.selectedFile);
        formData.append('description', this.formData.description);
        formData.append('visibility', this.formData.visibility);

        // 添加标签
        this.formData.tags.forEach((tag) => {
          formData.append('tags', tag);
        });

        // 添加AI自动标签选项
        formData.append('autoTag', this.formData.autoTag.toString());

        await this.photoStore.uploadPhoto(formData);

        this.uploadResult = {
          success: true,
          message: '照片上传成功！'
        };

        // 重置表单
        this.resetForm();
      } catch (error) {
        console.error('Upload error:', error);
        this.uploadResult = {
          success: false,
          message: error.response?.data?.message || '上传失败，请重试'
        };
      } finally {
        this.isUploading = false;
      }
    },

    resetForm() {
      // 释放预览URL
      if (this.previewUrl) {
        URL.revokeObjectURL(this.previewUrl);
        this.previewUrl = null;
      }

      this.selectedFile = null;
      this.formData.description = '';
      this.formData.tags = [];
      this.aiGeneratedTags = [];
      this.formData.visibility = 'PRIVATE';
      this.formData.autoTag = false;
      this.$refs.fileInput.value = '';
    },

    onPreviewLoad() {
      console.log('图片预览加载成功');
    },

    onPreviewError(event) {
      console.error('图片预览加载失败', event);
      // 如果预览失败，清除预览URL并显示错误
      if (this.previewUrl) {
        URL.revokeObjectURL(this.previewUrl);
        this.previewUrl = null;
      }
    },

    formatFileSize(bytes) {
      if (bytes === 0) return '0 Bytes';
      const k = 1024;
      const sizes = ['Bytes', 'KB', 'MB', 'GB'];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }
  }
}
</script>

<style scoped>
.upload-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}

.upload-container {
  background: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.drop-zone {
  border: 2px dashed #ccc;
  border-radius: 8px;
  padding: 3rem 2rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background-color: #fafafa;
}

.drop-zone:hover {
  border-color: #007bff;
  background-color: #f8f9ff;
}

.drop-zone.drag-over {
  border-color: #007bff;
  background-color: #e3f2fd;
  transform: scale(1.02);
}

.upload-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: bold;
}

input, textarea, select {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

textarea {
  resize: vertical;
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
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

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  background-color: #007bff;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: #0056b3;
}

.btn-secondary {
  background-color: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background-color: #545b62;
}

.upload-result {
  margin-top: 1.5rem;
  padding: 1rem;
  border-radius: 4px;
}

.success-message {
  background-color: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.error-message {
  background-color: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
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

.image-preview {
  margin: 2rem 0;
  padding: 1.5rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.image-preview h3 {
  margin: 0 0 1rem 0;
  font-size: 1.2rem;
  color: #333;
}

.preview-container {
  text-align: center;
  margin-bottom: 1rem;
}

.preview-container img {
  max-width: 100%;
  max-height: 400px;
  object-fit: contain;
  border-radius: 4px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
}

.preview-filename {
  font-size: 0.9rem;
  color: #666;
  text-align: center;
  word-break: break-all;
}

</style>