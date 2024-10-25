import { createApp } from 'vue'
import './style.css'
import './vue-style.css'
import App from './App.vue'
import router from './router'
import Toast, { POSITION, PluginOptions } from 'vue-toastification'
import 'vue-toastification/dist/index.css';

const app = createApp(App);

const options: PluginOptions = {
    position: POSITION.TOP_CENTER
};

app.use(Toast, options);
app.use(router).mount('#app');