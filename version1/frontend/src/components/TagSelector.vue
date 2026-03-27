<template>
  <div class="tag-selector">
    <div class="selected-tags-preview" v-if="selectedTags.length > 0">
      <h4>已选标签:</h4>
      <div class="selected-tags-list">
        <span
          v-for="(tag, index) in selectedTags"
          :key="index"
          class="selected-tag"
        >
          {{ tag }}
          <button @click="removeTag(tag)" class="remove-tag-btn" title="移除标签">
            ×
          </button>
        </span>
      </div>
    </div>

    <div class="available-tags-section">
      <h4>可选标签:</h4>
      <div class="tag-categories">
        <div v-for="(tags, category) in categorizedTags" :key="category" class="tag-category">
          <h5 class="category-title">{{ category }}</h5>
          <div class="category-tags">
            <label
              v-for="tag in tags"
              :key="tag"
              class="tag-checkbox-label"
              :class="{ 'tag-checked': isTagSelected(tag) }"
            >
              <input
                type="checkbox"
                :value="tag"
                :checked="isTagSelected(tag)"
                @change="toggleTag(tag)"
                class="tag-checkbox"
              >
              <span class="tag-text">{{ tag }}</span>
            </label>
          </div>
        </div>
      </div>

      <div class="custom-tag-input">
        <input
          type="text"
          v-model="customTag"
          placeholder="输入自定义标签..."
          @keyup.enter="addCustomTag"
          class="custom-tag-input-field"
        >
        <button @click="addCustomTag" class="btn btn-small btn-primary">
          添加
        </button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TagSelector',
  props: {
    availableTags: {
      type: Array,
      default: () => []
    },
    modelValue: {
      type: Array,
      default: () => []
    },
    aiTags: {
      type: Array,
      default: () => []
    }
  },
  emits: ['update:modelValue'],
  data() {
    return {
      customTag: ''
    }
  },
  computed: {
    selectedTags() {
      return this.modelValue;
    },
    categorizedTags() {
      // 分类逻辑：AI标签、常用标签、已选标签
      const categories = {};

      // AI推荐标签类别 - 直接显示所有AI返回的标签，不过滤
      const aiTags = this.aiTags.filter(tag => !this.selectedTags.includes(tag) && tag !== '等待AI分析...');
      categories['AI推荐标签'] = aiTags.length > 0 ? aiTags : ['等待AI分析...'];

      // 常用标签类别（排除已选和AI标签）
      const commonTags = this.availableTags.filter(tag =>
        !this.selectedTags.includes(tag) && !this.aiTags.includes(tag)
      );
      if (commonTags.length > 0) {
        categories['常用标签'] = commonTags;
      }

      // 已选标签类别
      if (this.selectedTags.length > 0) {
        categories['已选标签'] = this.selectedTags;
      }

      return categories;
    }
  },
  methods: {
    isTagSelected(tag) {
      return this.selectedTags.includes(tag);
    },

    toggleTag(tag) {
      const newSelectedTags = [...this.selectedTags];
      const tagIndex = newSelectedTags.indexOf(tag);

      if (tagIndex === -1) {
        newSelectedTags.push(tag);
      } else {
        newSelectedTags.splice(tagIndex, 1);
      }

      this.$emit('update:modelValue', newSelectedTags);
    },

    removeTag(tag) {
      const newSelectedTags = this.selectedTags.filter(t => t !== tag);
      this.$emit('update:modelValue', newSelectedTags);
    },

    addCustomTag() {
      const tag = this.customTag.trim();
      if (tag && !this.selectedTags.includes(tag)) {
        const newSelectedTags = [...this.selectedTags, tag];
        this.$emit('update:modelValue', newSelectedTags);
        this.customTag = '';
      }
    }
  }
}
</script>

<style scoped>
.tag-selector {
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  padding: 1.5rem;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.selected-tags-preview {
  margin-bottom: 1.5rem;
  padding-bottom: 1.25rem;
  border-bottom: 1px solid #f0f0f0;
}

.selected-tags-preview h4 {
  margin: 0 0 0.75rem 0;
  font-size: 1rem;
  color: #333;
  font-weight: 600;
}

.selected-tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.selected-tag {
  display: inline-flex;
  align-items: center;
  background-color: #4a6cf7;
  color: white;
  padding: 0.5rem 0.875rem;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 500;
  gap: 0.5rem;
  box-shadow: 0 1px 3px rgba(74, 108, 247, 0.2);
  transition: all 0.2s ease;
}

.selected-tag:hover {
  background-color: #3a5ce5;
  transform: translateY(-1px);
  box-shadow: 0 2px 5px rgba(74, 108, 247, 0.3);
}

.remove-tag-btn {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  font-size: 1.1rem;
  line-height: 1;
  padding: 0;
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  opacity: 0.8;
  transition: opacity 0.2s ease;
}

.remove-tag-btn:hover {
  opacity: 1;
  background-color: rgba(255, 255, 255, 0.2);
}

.available-tags-section h4 {
  margin: 0 0 1rem 0;
  font-size: 1rem;
  color: #333;
  font-weight: 600;
}

.tag-categories {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  margin-bottom: 1.5rem;
}

.tag-category {
  border: 1px solid #f0f0f0;
  border-radius: 10px;
  padding: 1.25rem;
  background-color: #fafafa;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.category-title {
  margin: 0 0 0.875rem 0;
  font-size: 0.9rem;
  font-weight: 600;
  color: #555;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid #e8e8e8;
}

.category-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.tag-checkbox-label {
  display: inline-flex;
  align-items: center;
  padding: 0.625rem 0.875rem;
  border: 1.5px solid #e0e0e0;
  border-radius: 8px;
  background-color: white;
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
  position: relative;
}

.tag-checkbox-label:hover {
  border-color: #4a6cf7;
  background-color: #f8f9ff;
  transform: translateY(-1px);
  box-shadow: 0 2px 5px rgba(74, 108, 247, 0.1);
}

.tag-checkbox-label.tag-checked {
  background-color: #eef2ff;
  border-color: #4a6cf7;
  color: #4a6cf7;
  font-weight: 500;
}

/* 隐藏原生复选框，使用自定义样式 */
.tag-checkbox {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
}

/* 自定义复选框 */
.tag-checkbox-label::before {
  content: '';
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid #ccc;
  border-radius: 4px;
  margin-right: 0.625rem;
  transition: all 0.2s ease;
  background-color: white;
}

.tag-checkbox-label:hover::before {
  border-color: #4a6cf7;
}

.tag-checkbox-label.tag-checked::before {
  background-color: #4a6cf7;
  border-color: #4a6cf7;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12' fill='none'%3E%3Cpath d='M10 3L4.5 8.5L2 6' stroke='white' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3C/path%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: center;
}

.tag-text {
  font-size: 0.85rem;
}

.custom-tag-input {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.custom-tag-input-field {
  flex: 1;
  padding: 0.75rem;
  border: 1.5px solid #ddd;
  border-radius: 8px;
  font-size: 0.9rem;
  transition: border-color 0.2s ease;
}

.custom-tag-input-field:focus {
  outline: none;
  border-color: #4a6cf7;
  box-shadow: 0 0 0 2px rgba(74, 108, 247, 0.1);
}

.btn {
  padding: 0.625rem 1.25rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
  text-decoration: none;
  display: inline-block;
  text-align: center;
  transition: all 0.2s ease;
}

.btn-small {
  padding: 0.5rem 0.875rem;
  font-size: 0.85rem;
}

.btn-primary {
  background-color: #4a6cf7;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: #3a5ce5;
  transform: translateY(-1px);
  box-shadow: 0 2px 5px rgba(74, 108, 247, 0.3);
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>