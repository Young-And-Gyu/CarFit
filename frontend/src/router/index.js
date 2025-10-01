import { createRouter, createWebHistory } from "vue-router";
import MainView from "../views/MainView.vue";
import LoginForm from "../components/LoginForm.vue";
import SignupForm from "../components/SignupForm.vue";
import BoardCreateView from "../views/BoardCreateView.vue";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/",
      name: "home",
      component: MainView,
    },
    {
      path: "/login",
      name: "login",
      component: LoginForm,
    },
    {
      path: "/signup",
      name: "signup",
      component: SignupForm,
    },
    {
      path: "/about",
      name: "about",
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import("../views/AboutView.vue"),
    },
    {
      path: "/board/create",
      name: "board-create",
      component: BoardCreateView,
    },
  ],
});

export default router;
