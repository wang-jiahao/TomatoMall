import './assets/main.css'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import { createApp } from 'vue'
import App from './App.vue'
import router from './router/index.js'
import axios from './utils/request'

const app = createApp(App)
app.config.globalProperties.$axios = axios

app.use(ElementPlus)
app.use(router)

app.mount('#app')
