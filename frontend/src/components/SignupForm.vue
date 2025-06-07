<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()
const userId = ref('')
const userIdError = ref('')
const password = ref('')
const passwordConfirm = ref('')
const name = ref('')
const email = ref('')
const address = ref('')
const birthDate = ref('')
const allAgreed = ref(false)
const termsAgreed = ref(false)
const privacyAgreed = ref(false)
const marketingAgreed = ref(false)
const isLoading = ref(false)
const signupError = ref('')

const emit = defineEmits(['signup'])

onMounted(() => {
  // 시스템의 다크모드 설정 확인
  const isDarkMode = window.matchMedia('(prefers-color-scheme: dark)').matches
  document.documentElement.classList.toggle('dark', isDarkMode)
  
  // 다크모드 변경 감지
  window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', e => {
    document.documentElement.classList.toggle('dark', e.matches)
  })
})

const validateUserId = () => {
  const userIdRegex = /^[a-zA-Z0-9]{6,}$/
  if (!userId.value) {
    userIdError.value = '아이디를 입력해주세요.'
    return false
  }
  if (!userIdRegex.test(userId.value)) {
    userIdError.value = '아이디는 6자 이상의 영문 혹은 숫자 조합이어야 합니다.'
    return false
  }
  userIdError.value = ''
  return true
}

const handleSignup = async () => {
  try {
    if (!validateUserId()) {
      return
    }
    if (password.value !== passwordConfirm.value) {
      alert('비밀번호가 일치하지 않습니다.')
      return
    }

    isLoading.value = true
    signupError.value = ''

    const response = await axios.post("http://localhost:8080/user/signup", {
      userId: userId.value,
      password: password.value,
      name: name.value,
      email: email.value,
      address: address.value,
      birthDate: birthDate.value,
      termsAgreed: termsAgreed.value,
      privacyAgreed: privacyAgreed.value,
      marketingAgreed: marketingAgreed.value
    }, {
      withCredentials: true
    });

    if (response.status === 200) {
      alert('회원가입이 완료되었습니다.')
      emit('signup', response.data)
      router.push('/login')
    } else {
      signupError.value = response.data || '회원가입 중 오류가 발생했습니다.'
    }
  } catch (error) {
    console.error('회원가입 에러:', error)
    console.error('에러 응답 데이터:', error.response?.data)
    console.error('에러 상태 코드:', error.response?.status)
    
    if (error.response?.status === 400) {
      userIdError.value = '이미 존재하는 아이디입니다.'
    } 
    else {
      signupError.value = error.response?.data?.message || '회원가입 중 오류가 발생했습니다.'
    }
  } finally {
    isLoading.value = false
  }
}

const toggleAllAgreements = () => {
  const isAllTrue = termsAgreed.value && privacyAgreed.value && marketingAgreed.value
  
  termsAgreed.value = !isAllTrue
  privacyAgreed.value = !isAllTrue
  marketingAgreed.value = !isAllTrue
  allAgreed.value = !isAllTrue
}

const checkAllAgreed = () => {
  allAgreed.value = termsAgreed.value && privacyAgreed.value && marketingAgreed.value
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
            회원가입
          </p>
        </div>
        
        <form class="space-y-6" @submit.prevent="handleSignup">
          <div class="space-y-4">
            <!-- 아이디 -->
            <div class="relative">
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">아이디</label>
              <input
                v-model="userId"
                type="text"
                required
                @input="validateUserId"
                class="appearance-none rounded-lg relative block w-full px-4 py-3 border border-gray-300 dark:border-gray-600
                       placeholder-gray-500 text-gray-900 dark:text-white
                       focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-orange-500
                       dark:bg-gray-700 dark:placeholder-gray-400
                       transition-colors duration-200"
                placeholder="6자 이상의 영문 혹은 숫자 조합"
              />
              <p v-if="userIdError" class="mt-1 text-sm text-red-500">{{ userIdError }}</p>
            </div>

            <!-- 비밀번호 -->
            <div class="relative">
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">비밀번호</label>
              <input
                v-model="password"
                type="password"
                required
                class="appearance-none rounded-lg relative block w-full px-4 py-3 border border-gray-300 dark:border-gray-600
                       placeholder-gray-500 text-gray-900 dark:text-white
                       focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-orange-500
                       dark:bg-gray-700 dark:placeholder-gray-400
                       transition-colors duration-200"
                placeholder="비밀번호를 입력해주세요"
              />
            </div>

            <!-- 비밀번호 확인 -->
            <div class="relative">
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">비밀번호 확인</label>
              <input
                v-model="passwordConfirm"
                type="password"
                required
                class="appearance-none rounded-lg relative block w-full px-4 py-3 border border-gray-300 dark:border-gray-600
                       placeholder-gray-500 text-gray-900 dark:text-white
                       focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-orange-500
                       dark:bg-gray-700 dark:placeholder-gray-400
                       transition-colors duration-200"
                placeholder="비밀번호 확인"
              />
            </div>

            <!-- 이름 -->
            <div class="relative">
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">이름</label>
              <input
                v-model="name"
                type="text"
                required
                class="appearance-none rounded-lg relative block w-full px-4 py-3 border border-gray-300 dark:border-gray-600
                       placeholder-gray-500 text-gray-900 dark:text-white
                       focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-orange-500
                       dark:bg-gray-700 dark:placeholder-gray-400
                       transition-colors duration-200"
                placeholder="이름"
              />
            </div>

            <!-- 이메일 -->
            <div class="relative">
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">이메일</label>
              <input
                v-model="email"
                type="email"
                required
                class="appearance-none rounded-lg relative block w-full px-4 py-3 border border-gray-300 dark:border-gray-600
                       placeholder-gray-500 text-gray-900 dark:text-white
                       focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-orange-500
                       dark:bg-gray-700 dark:placeholder-gray-400
                       transition-colors duration-200"
                placeholder="이메일 주소"
              />
            </div>

            <!-- 주소 -->
            <div class="relative">
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">주소</label>
              <input
                v-model="address"
                type="text"
                required
                class="appearance-none rounded-lg relative block w-full px-4 py-3 border border-gray-300 dark:border-gray-600
                       placeholder-gray-500 text-gray-900 dark:text-white
                       focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-orange-500
                       dark:bg-gray-700 dark:placeholder-gray-400
                       transition-colors duration-200"
                placeholder="주소"
              />
            </div>

            <!-- 생년월일 -->
            <div class="relative">
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">생년월일</label>
              <input
                v-model="birthDate"
                type="text"
                required
                class="appearance-none rounded-lg relative block w-full px-4 py-3 border border-gray-300 dark:border-gray-600
                       placeholder-gray-500 text-gray-900 dark:text-white
                       focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-orange-500
                       dark:bg-gray-700 dark:placeholder-gray-400
                       transition-colors duration-200"
                placeholder="YYYY/MM/DD"
              />
            </div>
          </div>

          <!-- 약관 동의 -->
          <div class="space-y-4">
            <div class="flex items-center">
              <input
                type="checkbox"
                v-model="allAgreed"
                @change="toggleAllAgreements"
                class="h-4 w-4 text-orange-500 focus:ring-orange-500 border-gray-300 rounded
                       dark:border-gray-600 dark:bg-gray-700"
              />
              <label class="ml-2 block text-sm text-gray-900 dark:text-gray-300">
                전체 동의합니다.
              </label>
            </div>
            
            <div class="space-y-2 pl-6">
              <div class="flex items-center">
                <input
                  type="checkbox"
                  v-model="termsAgreed"
                  @change="checkAllAgreed"
                  required
                  class="h-4 w-4 text-orange-500 focus:ring-orange-500 border-gray-300 rounded
                         dark:border-gray-600 dark:bg-gray-700"
                />
                <label class="ml-2 block text-sm text-gray-500 dark:text-gray-400">
                  이용약관 동의 (필수)
                </label>
              </div>
              
              <div class="flex items-center">
                <input
                  type="checkbox"
                  v-model="privacyAgreed"
                  @change="checkAllAgreed"
                  required
                  class="h-4 w-4 text-orange-500 focus:ring-orange-500 border-gray-300 rounded
                         dark:border-gray-600 dark:bg-gray-700"
                />
                <label class="ml-2 block text-sm text-gray-500 dark:text-gray-400">
                  개인정보 수집 이용 동의 (필수)
                </label>
              </div>
              
              <div class="flex items-center">
                <input
                  type="checkbox"
                  v-model="marketingAgreed"
                  @change="checkAllAgreed"
                  class="h-4 w-4 text-orange-500 focus:ring-orange-500 border-gray-300 rounded
                         dark:border-gray-600 dark:bg-gray-700"
                />
                <label class="ml-2 block text-sm text-gray-500 dark:text-gray-400">
                  마케팅 수신 동의 (선택)
                </label>
              </div>
            </div>
          </div>

          <button
            type="submit"
            :disabled="isLoading"
            class="w-full flex justify-center py-3 px-4 border border-transparent rounded-lg
                   text-sm font-medium text-white bg-orange-500 hover:bg-orange-600
                   focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-orange-500
                   transition-colors duration-200
                   disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {{ isLoading ? '처리중...' : '회원가입' }}
          </button>
          
          <p v-if="signupError" class="mt-2 text-sm text-red-500 text-center">
            {{ signupError }}
          </p>
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