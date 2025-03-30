import { createRouter, createWebHistory } from 'vue-router';
import HomePage from '../pages/index.vue';
import CartPage from "../pages/CartPage.vue";
import WarehousePage from "../pages/WarehousePage.vue";
import LoginPage from '../pages/LoginPage.vue'
import RegisterPage from '../pages/RegisterPage.vue'
import ProfilePage from '../pages/ProfilePage.vue'

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
    }
    //其他路由配置
];

const router = createRouter({
    history: createWebHistory(),
    routes
});



export default router;
