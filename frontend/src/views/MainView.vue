<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const gasolinePrices = ref([])
const dieselPrices = ref([])
const lpgPrices= ref([])
const loading = ref(true)
const error = ref(null)
const selectedRegion = ref('전국')
const isDarkMode = ref(false)

onMounted(async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/oil')
    console.log('API 응답:', response.data)
    gasolinePrices.value = response.data.gasoline
    dieselPrices.value = response.data.diesel
    lpgPrices.value=response.data.lpg
    
    // 시스템의 다크모드 설정 확인
    isDarkMode.value = window.matchMedia('(prefers-color-scheme: dark)').matches
    
    // 다크모드 변경 감지
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', e => {
      isDarkMode.value = e.matches
      document.documentElement.classList.toggle('dark', e.matches)
    })
    
    // 초기 다크모드 설정 적용
    document.documentElement.classList.toggle('dark', isDarkMode.value)
  } catch (err) {
    console.error('API 오류:', err)
    error.value = '데이터를 불러오는 중 오류가 발생했습니다.'
  } finally {
    loading.value = false
  }
})

const filteredData = (data) => {
  if (selectedRegion.value === '전국') {
    // 전국 데이터는 모든 지역의 평균값으로 계산
    const total = data.reduce((acc, curr) => {
      return {
        PRICE: acc.PRICE + Number(curr.PRICE),
        DIFF: acc.DIFF + Number(curr.DIFF)
      }
    }, { PRICE: 0, DIFF: 0 })
    
    return {
      PRICE: (total.PRICE / data.length).toFixed(2),
      DIFF: (total.DIFF / data.length).toFixed(2)
    }
  }
  return data.find(item => item.SIDONM === selectedRegion.value) || data[0] || {}
}
</script>

<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 flex justify-center items-center">
    <div class="w-full max-w-7xl px-4 sm:px-6 lg:px-8 py-12">
      <div class="text-center mb-12">
        <h1 class="text-4xl font-extrabold text-gray-900 dark:text-white sm:text-5xl">
          전국 주유소 평균가격
        </h1>
        <p class="mt-3 text-xl text-gray-500 dark:text-gray-400">
          실시간 전국 주유소 가격 정보를 확인하세요
        </p>
      </div>

      <div v-if="loading" class="text-center py-12">
        <div class="animate-spin rounded-full h-16 w-16 border-b-4 border-blue-900 dark:border-blue-400 mx-auto"></div>
        <p class="mt-6 text-lg text-gray-700 dark:text-gray-300">데이터를 불러오는 중입니다...</p>
      </div>

      <div v-else-if="error" class="bg-red-50 dark:bg-red-900/20 border-l-8 border-red-500 text-red-800 dark:text-red-300 p-6 rounded-lg shadow">
        {{ error }}
      </div>

      <div v-else class="space-y-12">
        <!-- 지역 선택 및 요약 정보 -->
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-6">
          <div class="flex items-center justify-between mb-6">
            <select v-model="selectedRegion" 
                    class="w-64 p-3 text-lg border-2 border-gray-300 dark:border-gray-600 rounded-xl shadow-sm 
                           focus:border-blue-500 focus:ring-4 focus:ring-blue-100 dark:focus:ring-blue-900 transition-all duration-200
                           bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100">
              <option value="전국">전국</option>
              <option v-for="oil in gasolinePrices" :key="oil.SIDONM" :value="oil.SIDONM">
                {{ oil.SIDONM }}
              </option>
            </select>
            <div class="text-gray-500 dark:text-gray-400">
              {{ new Date().toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' }) }} 기준
            </div>
          </div>

          <div class="grid grid-cols-3 gap-6">
            <!-- 휘발유 카드 -->
            <div class="bg-gradient-to-br from-yellow-50 via-white to-yellow-100 dark:from-yellow-900/20 dark:via-gray-800 dark:to-yellow-900/20 
                        p-6 rounded-xl shadow-lg transform hover:scale-[1.02] transition-all duration-300 border border-yellow-100 dark:border-yellow-900/30">
              <div class="flex items-center justify-between mb-4">
                <h3 class="text-2xl font-bold text-yellow-900 dark:text-yellow-400 flex items-center">
                  <span class="mr-3 text-3xl">⛽</span>휘발유
                </h3>
                <div class="text-3xl font-extrabold text-yellow-900 dark:text-yellow-400">
                  {{ Number(filteredData(gasolinePrices).PRICE || 0).toLocaleString() }}
                  <span class="text-lg ml-1">원</span>
                </div>
              </div>
              <div class="flex justify-end">
                <div class="text-lg font-semibold px-4 py-2 rounded-lg" 
                     :class="{'bg-red-100 text-red-600 dark:bg-red-900/30 dark:text-red-400': filteredData(gasolinePrices).DIFF > 0, 
                              'bg-blue-100 text-blue-600 dark:bg-blue-900/30 dark:text-blue-400': filteredData(gasolinePrices).DIFF < 0}">
                  {{ filteredData(gasolinePrices).DIFF > 0 ? '▲' : '▼' }} 
                  {{ Math.abs(Number(filteredData(gasolinePrices).DIFF || 0)).toFixed(2) }}
                </div>
              </div>
            </div>

            <!-- 경유 카드 -->
            <div class="bg-gradient-to-br from-green-50 via-white to-green-100 dark:from-green-900/20 dark:via-gray-800 dark:to-green-900/20 
                        p-6 rounded-xl shadow-lg transform hover:scale-[1.02] transition-all duration-300 border border-green-100 dark:border-green-900/30">
              <div class="flex items-center justify-between mb-4">
                <h3 class="text-2xl font-bold text-green-900 dark:text-green-400 flex items-center">
                  <span class="mr-3 text-3xl">🛢️</span>경유
                </h3>
                <div class="text-3xl font-extrabold text-green-900 dark:text-green-400">
                  {{ Number(filteredData(dieselPrices).PRICE || 0).toLocaleString() }}
                  <span class="text-lg ml-1">원</span>
                </div>
              </div>
              <div class="flex justify-end">
                <div class="text-lg font-semibold px-4 py-2 rounded-lg"
                     :class="{'bg-red-100 text-red-600 dark:bg-red-900/30 dark:text-red-400': filteredData(dieselPrices).DIFF > 0, 
                              'bg-blue-100 text-blue-600 dark:bg-blue-900/30 dark:text-blue-400': filteredData(dieselPrices).DIFF < 0}">
                  {{ filteredData(dieselPrices).DIFF > 0 ? '▲' : '▼' }} 
                  {{ Math.abs(Number(filteredData(dieselPrices).DIFF || 0)).toFixed(2) }}
                </div>
              </div>
            </div>
            <!-- LPG 카드 -->
            <div class="bg-gradient-to-br from-blue-50 via-white to-blue-100 dark:from-blue-900/20 dark:via-gray-800 dark:to-blue-900/20 
                        p-6 rounded-xl shadow-lg transform hover:scale-[1.02] transition-all duration-300 border border-blue-100 dark:border-blue-900/30">
              <div class="flex items-center justify-between mb-4">
                <h3 class="text-2xl font-bold text-blue-900 dark:text-blue-400 flex items-center">
                  <span class="mr-3 text-3xl">⛽</span>LPG
                </h3>
                <div class="text-3xl font-extrabold text-blue-900 dark:text-blue-400">
                  {{ Number(filteredData(lpgPrices).PRICE || 0).toLocaleString() }}
                  <span class="text-lg ml-1">원</span>
                </div>
              </div>
              <div class="flex justify-end">
                <div class="text-lg font-semibold px-4 py-2 rounded-lg" 
                     :class="{'bg-red-100 text-red-600 dark:bg-red-900/30 dark:text-red-400': filteredData(lpgPrices).DIFF > 0, 
                              'bg-blue-100 text-blue-600 dark:bg-blue-900/30 dark:text-blue-400': filteredData(lpgPrices).DIFF < 0}">
                  {{ filteredData(lpgPrices).DIFF > 0 ? '▲' : '▼' }} 
                  {{ Math.abs(Number(filteredData(lpgPrices).DIFF || 0)).toFixed(2) }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 지역별 가격 비교 테이블 -->
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-6">
          <h3 class="text-2xl font-bold text-gray-900 dark:text-white mb-6">지역별 가격 비교</h3>
          <div class="overflow-x-auto">
            <table class="w-full">
              <thead>
                <tr class="bg-gray-50 dark:bg-gray-700">
                  <th class="px-6 py-4 text-left text-gray-800 dark:text-gray-200 font-bold rounded-l-lg">지역</th>
                  <th class="px-6 py-4 text-right text-gray-800 dark:text-gray-200 font-bold">휘발유(원)</th>
                  <th class="px-6 py-4 text-right text-gray-800 dark:text-gray-200 font-bold">변동</th>
                  <th class="px-6 py-4 text-right text-gray-800 dark:text-gray-200 font-bold">경유(원)</th>
                  <th class="px-6 py-4 text-right text-gray-800 dark:text-gray-200 font-bold rounded-r-lg">변동</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-gray-200 dark:divide-gray-700">
                <tr v-for="(gasoline, index) in gasolinePrices" :key="index"
                    class="hover:bg-gray-50 dark:hover:bg-gray-700/50 transition-colors duration-200">
                  <td class="px-6 py-4 font-medium text-gray-900 dark:text-gray-100">{{ gasoline.SIDONM }}</td>
                  <td class="px-6 py-4 text-right font-semibold text-gray-900 dark:text-gray-100">
                    {{ Number(gasoline.PRICE).toLocaleString() }}
                  </td>
                  <td class="px-6 py-4 text-right">
                    <span class="px-3 py-1 rounded-full text-sm" 
                          :class="{'bg-red-100 text-red-600 dark:bg-red-900/30 dark:text-red-400': gasoline.DIFF > 0, 
                                  'bg-blue-100 text-blue-600 dark:bg-blue-900/30 dark:text-blue-400': gasoline.DIFF < 0}">
                      {{ gasoline.DIFF > 0 ? '▲' : '▼' }}
                      {{ Math.abs(Number(gasoline.DIFF)).toFixed(2) }}
                    </span>
                  </td>
                  <td class="px-6 py-4 text-right font-semibold text-gray-900 dark:text-gray-100">
                    {{ Number(dieselPrices[index]?.PRICE || 0).toLocaleString() }}
                  </td>
                  <td class="px-6 py-4 text-right">
                    <span class="px-3 py-1 rounded-full text-sm" 
                          :class="{'bg-red-100 text-red-600 dark:bg-red-900/30 dark:text-red-400': dieselPrices[index]?.DIFF > 0, 
                                  'bg-blue-100 text-blue-600 dark:bg-blue-900/30 dark:text-blue-400': dieselPrices[index]?.DIFF < 0}">
                      {{ dieselPrices[index]?.DIFF > 0 ? '▲' : '▼' }}
                      {{ Math.abs(Number(dieselPrices[index]?.DIFF || 0)).toFixed(2) }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.grid {
  transition: all 0.4s ease;
}

table {
  border-collapse: separate;
  border-spacing: 0;
}

tbody tr {
  border-radius: 8px;
}

tbody tr:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

select {
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 20 20'%3e%3cpath stroke='%236b7280' stroke-linecap='round' stroke-linejoin='round' stroke-width='1.5' d='M6 8l4 4 4-4'/%3e%3c/svg%3e");
  background-position: right 1rem center;
  background-repeat: no-repeat;
  background-size: 1.5em 1.5em;
  padding-right: 3rem;
  -webkit-appearance: none;
  -moz-appearance: none;
  appearance: none;
}

.transform {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.hover\:scale-\[1\.02\]:hover {
  transform: scale(1.02);
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}
</style>