// stores/index.js
import { createPinia } from 'pinia';
import { defineStore } from 'pinia';
import { authAPI, photoAPI } from '../services/api';

export { createPinia };

// 认证相关store
export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: localStorage.getItem('token') || null,
    isAuthenticated: !!localStorage.getItem('token')
  }),

  getters: {
    getUser: (state) => state.user,
    getToken: (state) => state.token,
    getIsAuthenticated: (state) => state.isAuthenticated
  },

  actions: {
    async login(credentials) {
      try {
        const response = await authAPI.login(credentials);
        const { token, user } = response.data;

        this.token = token;
        this.user = user;
        this.isAuthenticated = true;

        localStorage.setItem('token', token);
        return response;
      } catch (error) {
        console.error('Login error:', error);
        throw error;
      }
    },

    async register(userData) {
      try {
        const response = await authAPI.register(userData);
        const { token, user } = response.data;

        this.token = token;
        this.user = user;
        this.isAuthenticated = true;

        localStorage.setItem('token', token);
        return response;
      } catch (error) {
        console.error('Registration error:', error);
        throw error;
      }
    },

    setAuth(user, token) {
      this.user = user;
      this.token = token;
      this.isAuthenticated = true;
      localStorage.setItem('token', token);
    },

    logout() {
      this.user = null;
      this.token = null;
      this.isAuthenticated = false;
      localStorage.removeItem('token');
    },

    updateUser(userInfo) {
      this.user = { ...this.user, ...userInfo };
    },

    async fetchCurrentUser() {
      try {
        const response = await authAPI.getMe();
        this.user = response.data;
        return response;
      } catch (error) {
        console.error('Failed to fetch current user:', error);
        if (error.response?.status === 401) {
          this.logout();
        }
        throw error;
      }
    }
  },

  persist: {
    key: 'auth',
    storage: localStorage,
  }
});

// 照片相关store
export const usePhotoStore = defineStore('photo', {
  state: () => ({
    photos: [],
    currentPhoto: null,
    loading: false,
    pagination: {
      currentPage: 1,
      totalPages: 1,
      totalItems: 0,
      itemsPerPage: 10
    }
  }),

  getters: {
    getAllPhotos: (state) => state.photos,
    getCurrentPhoto: (state) => state.currentPhoto,
    isLoading: (state) => state.loading
  },

  actions: {
    setPhotos(photos) {
      this.photos = photos;
    },

    setCurrentPhoto(photo) {
      this.currentPhoto = photo;
    },

    setLoading(status) {
      this.loading = status;
    },

    async fetchPhotos(params = {}) {
      this.setLoading(true);
      try {
        let response;

        // 如果有可见性筛选，使用专门的可见性端点
        if (params.visibility) {
          const { visibility, ...otherParams } = params;
          response = await photoAPI.getPhotosByVisibility(visibility, otherParams);
        } else {
          response = await photoAPI.getPhotos(params);
        }

        this.setPhotos(response.data.content || response.data);
        if (response.data.totalPages !== undefined) {
          this.updatePagination({
            currentPage: response.data.number,
            totalPages: response.data.totalPages,
            totalItems: response.data.totalElements,
            itemsPerPage: response.data.size
          });
        }
        return response;
      } catch (error) {
        console.error('Failed to fetch photos:', error);
        throw error;
      } finally {
        this.setLoading(false);
      }
    },

    async uploadPhoto(formData) {
      this.setLoading(true);
      try {
        const response = await photoAPI.upload(formData);
        this.addPhoto(response.data);
        return response;
      } catch (error) {
        console.error('Failed to upload photo:', error);
        throw error;
      } finally {
        this.setLoading(false);
      }
    },

    async deletePhoto(photoId) {
      try {
        await photoAPI.deletePhoto(photoId);
        this.removePhoto(photoId);
        return true;
      } catch (error) {
        console.error('Failed to delete photo:', error);
        throw error;
      }
    },

    addPhoto(photo) {
      this.photos.unshift(photo);
    },

    removePhoto(photoId) {
      this.photos = this.photos.filter(photo => photo.id !== photoId);
    },

    updatePagination(pagination) {
      this.pagination = { ...this.pagination, ...pagination };
    }
  },

  persist: {
    key: 'photo',
    storage: localStorage,
  }
});