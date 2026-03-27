const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('./views/Home.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('./views/auth/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('./views/auth/Register.vue')
  },
  {
    path: '/gallery',
    name: 'Gallery',
    component: () => import('./views/Gallery.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/upload',
    name: 'Upload',
    component: () => import('./views/Upload.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('./views/Profile.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/photo/:id',
    name: 'PhotoDetail',
    component: () => import('./views/PhotoDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/photo/:id/edit',
    name: 'PhotoEdit',
    component: () => import('./views/PhotoEdit.vue'),
    meta: { requiresAuth: true }
  }
]
export default routes