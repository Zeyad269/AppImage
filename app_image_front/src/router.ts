import { createWebHistory, createRouter } from "vue-router";
import { RouteRecordRaw } from "vue-router";

const routes: Array<RouteRecordRaw> = [

  // First page (home page)
  {
    path: "/",
    name: "home",
    component: () => import("./components/Home.vue"),
    props: true
  },

  // Second page (Interface when an image is clicked from the home page)
  {
    path: "/imagetools",
    name: "imagetools",
    component: () => import("./components/Tools.vue"),
    props: true
  },

];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;