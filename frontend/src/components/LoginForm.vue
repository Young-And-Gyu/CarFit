<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const userId = ref('')
const password = ref('')
const isDarkMode = ref(false)

const emit = defineEmits(['login'])

onMounted(() => {
  // 시스템의 다크모드 설정 확인
  isDarkMode.value = window.matchMedia('(prefers-color-scheme: dark)').matches
  
  // 다크모드 변경 감지
  window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', e => {
    isDarkMode.value = e.matches
    document.documentElement.classList.toggle('dark', e.matches)
  })
  
  // 초기 다크모드 설정 적용
  document.documentElement.classList.toggle('dark', isDarkMode.value)
})

const handleLogin = () => {
  emit('login', {
    userId: userId.value,
    password: password.value
  })
}

const goToSignup = () => {
  router.push('/signup')
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-100 dark:bg-gray-900 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full bg-white dark:bg-gray-800 shadow-xl rounded-2xl p-8">
      <div class="space-y-8">
        <div>
          <h1 class="text-center text-4xl font-bold text-gray-900 dark:text-white mb-2">
            CarFit
          </h1>
          <p class="text-center text-sm text-gray-500 dark:text-gray-400">
            안전한 차량 관리의 시작
          </p>
        </div>
        <form class="space-y-6" @submit.prevent="handleLogin">
          <div class="space-y-4">
            <div class="relative">
              <input
                v-model="userId"
                type="text"
                required
                class="appearance-none rounded-lg relative block w-full px-4 py-3 border border-gray-300 dark:border-gray-600
                       placeholder-gray-500 text-gray-900 dark:text-white
                       focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-orange-500
                       dark:bg-gray-700 dark:placeholder-gray-400
                       transition-colors duration-200"
                placeholder="아이디"
              />
            </div>
            <div class="relative">
              <input
                v-model="password"
                type="password"
                required
                class="appearance-none rounded-lg relative block w-full px-4 py-3 border border-gray-300 dark:border-gray-600
                       placeholder-gray-500 text-gray-900 dark:text-white
                       focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-orange-500
                       dark:bg-gray-700 dark:placeholder-gray-400
                       transition-colors duration-200"
                placeholder="비밀번호"
              />
            </div>
          </div>

          <div class="space-y-3">
            <button
              type="submit"
              class="group relative w-full flex justify-center py-3 px-4 border border-transparent
                     text-sm font-medium rounded-lg text-white bg-orange-500 hover:bg-orange-600
                     focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-orange-500
                     transition-colors duration-200"
            >
              로그인
            </button>
            <button
              type="button"
              @click="goToSignup"
              class="group relative w-full flex justify-center py-3 px-4 border border-orange-500
                     text-sm font-medium rounded-lg text-orange-500 bg-white hover:bg-orange-50
                     dark:bg-transparent dark:text-orange-400 dark:hover:bg-orange-900/20
                     focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-orange-500
                     transition-colors duration-200"
            >
              회원가입
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.appearance-none:focus {
  outline: 2px solid transparent;
  outline-offset: 2px;
}

/* 다크모드에서 input autofill 스타일 수정 */
@media (prefers-color-scheme: dark) {
  input:-webkit-autofill,
  input:-webkit-autofill:hover,
  input:-webkit-autofill:focus {
    -webkit-box-shadow: 0 0 0px 1000px #374151 inset;
    -webkit-text-fill-color: white;
    transition: background-color 5000s ease-in-out 0s;
  }
}
</style> 