import axios from 'axios';

// 创建axios实例
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8083/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// 请求拦截器 - 添加认证令牌
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器 - 处理认证过期
apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      // 清除认证信息
      localStorage.removeItem('token');
      // 可以触发登出操作
    }
    return Promise.reject(error);
  }
);

// 认证相关API
export const authAPI = {
  // 注册
  register: (userData) => apiClient.post('/auth/register', userData),

  // 登录
  login: (credentials) => apiClient.post('/auth/login', credentials),

  // 获取当前用户信息
  getMe: () => apiClient.get('/auth/me'),

  // 更新用户资料
  updateProfile: (profileData) => apiClient.put('/auth/profile', profileData)
};

// 照片相关API
export const photoAPI = {
  // 上传照片
  upload: (formData) => apiClient.post('/photos/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }),

  // 获取用户照片列表
  getPhotos: (params = {}) => apiClient.get('/photos', { params }),

  // 按可见性获取照片
  getPhotosByVisibility: (visibility, params = {}) =>
    apiClient.get(`/photos/visibility/${visibility}`, { params }),

  // 获取照片详情
  getPhoto: (id) => apiClient.get(`/photos/${id}`),

  // 更新照片信息
  updatePhoto: (id, params) => apiClient.put(`/photos/${id}`, null, { params }),
  downloadPhoto: (id) => apiClient.get(`/photos/${id}/download`, {
    responseType: 'blob'
  }),

  // 搜索照片（按标签）
  searchPhotos: (tag) => apiClient.get(`/photos/search?tag=${tag}`),

  // 删除照片
  deletePhoto: (id) => apiClient.delete(`/photos/${id}`)
};

// AI分析相关API
export const aiAPI = {
  // 分析图像内容并生成智能标签
  analyzeImage: (formData) => apiClient.post('/ai/analyze', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }),

  // 获取AI服务状态
  getAIStatus: () => apiClient.get('/ai/status')
};

export default apiClient;