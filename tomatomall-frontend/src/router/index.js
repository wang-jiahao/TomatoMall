import { createRouter, createWebHistory } from 'vue-router';
import HomePage from '../pages/index.vue';
import CartPage from "../pages/CartPage.vue";
import WarehousePage from "../pages/WarehousePage.vue";
import LoginPage from '../pages/LoginPage.vue'
import RegisterPage from '../pages/RegisterPage.vue'
import ProfilePage from '../pages/ProfilePage.vue'
import ProductDetailPage from '../pages/ProductDetailPage.vue'
import AdminProductPage from '../pages/AdminProductPage.vue'

const routes = [
    {
        path: '/',
        name: 'index',
        component: HomePage
    },
    {
        path: '/cart',
        name: 'cart',
        component: CartPage,
    },
    {
        path: '/warehouse',
        name: 'warehouse',
        component: WarehousePage,
    },
    {
        path: '/login',
        name: 'login',
        component: LoginPage
    },
    {
        path: '/register',
        name: 'register',
        component: RegisterPage
    },
    {
        path: '/profile',
        name: 'profile',
        component: ProfilePage,
        meta: { requiresAuth: true }
    },
    {
        path: '/product/:id',
        name: 'product-detail',
        component: ProductDetailPage
    },
    {
        path: '/admin/products',
        name: 'admin-products',
        component: AdminProductPage,
        meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
        path: '/checkout',
        name: 'checkout',
        component: () => import('../pages/CheckoutPage.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/payment/:orderId',
        name: 'payment',
        component: () => import('../pages/PaymentPage.vue'),
        meta: { requiresAuth: true }
    },
    {
        path: '/advertisements',
        name: 'advertisements',
        component: () => import('../pages/AdvertisementsPage.vue')
    },
    {
        path: '/admin/advertisements',
        name: 'admin-advertisements',
        component: () => import('../pages/AdminAdvertisementsPage.vue'),
        meta: { requiresAuth: true, requiresAdmin: true }
    }
    //其他路由配置
];

const router = createRouter({
    history: createWebHistory(),
    routes
});



export default router;

/*router.beforeEach((to, from, next) => {
    const role = localStorage.getItem('role')
    if (to.meta.requiresAdmin && role !== 'admin') {
        next('/login')
    } else {
        next()
    }
})*/
