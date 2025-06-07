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

const gasolineStations = ref([])

const addressInput= ref('');
const searchError = ref('');
const insuranceError = ref('');
const insuranceResult = ref(null);
const insuranceForm = ref({
  age: '',
  gender: 'MALE',
  carType: '',
  carPrice: null,
  engineSize: null,
  carYear: '',
  annualMileage: null,
  licenseYear: '',
  accidentCount: 0,
  drunkDriving: false,
  trafficViolations: 0,
  region: '',
  isDirect: false,
  options: {
    blackBox: false,
    hasChild: false,
    hasMultipleChildren: false,
    adas: false,
    isMilitaryDriver: false,
    usesPublicTransport: false
  }
});


onMounted(async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/oil', {
      withCredentials: true  
    })
    console.log('API 응답:', response.data)
    gasolinePrices.value = response.data.gasoline
    dieselPrices.value = response.data.diesel
    lpgPrices.value = response.data.lpg
    
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
const searchGasStations = async () => {
  // 입력값 검증
  if (!addressInput.value.trim()) {
    searchError.value = '주소를 입력해주세요.';
    gasolineStations.value = [];
    return;
  }

  try {
    searchError.value = ''; // 에러 메시지 초기화
    const { x, y } = await getCoordinatesFromBackend(addressInput.value);

    // 오피넷 API 요청 시 x, y 값을 사용하여 주유소 검색
    const response = await axios.get('http://localhost:8080/api/stations/nearby', {
      params: {
        x: x,
        y: y,
        radius: 5000,
        sort: 1,
        limit: 10
      }
    });

    console.log('API 응답:', response.data);

    // 프론트엔드에서도 10개로 제한하여 처리
    gasolineStations.value = response.data.stations.slice(0, 10);

    // 검색 결과가 없는 경우
    if (gasolineStations.value.length === 0) {
      searchError.value = '주변에 주유소가 없습니다.';
    }

  } catch (err) {
    console.error('API 오류:', err);
    searchError.value = '주소를 찾을 수 없습니다. 올바른 주소를 입력해주세요.';
    gasolineStations.value = [];
  } finally {
    loading.value = false;
  }
};


const getCoordinatesFromBackend = async (address) => {
  try {
    const res = await axios.get('http://localhost:8080/api/geocode', {
      params: { address }
    });
    console.log('좌표 데이터:', res.data);
    return res.data; // { x: "...", y: "..." }를 리턴
  } catch (error) {
    console.error("좌표 변환 에러", error);
    throw error;
  }
};

const sendInsuranceData = async () => {
  try {
    // 필수 필드 유효성 검사
    if (!insuranceForm.value.age || 
        !insuranceForm.value.carType || 
        !insuranceForm.value.carPrice || 
        !insuranceForm.value.engineSize || 
        !insuranceForm.value.carYear || 
        !insuranceForm.value.annualMileage || 
        !insuranceForm.value.licenseYear || 
        !insuranceForm.value.region) {
      insuranceError.value = '모든 필수 항목을 입력해주세요.';
      return;
    }

    // annualMileage가 숫자인지 확인
    const annualMileage = parseInt(insuranceForm.value.annualMileage);
    if (isNaN(annualMileage)) {
      insuranceError.value = '연간 주행거리는 숫자로 입력해주세요.';
      return;
    }

    const requestData = {
      age: parseInt(insuranceForm.value.age),
      gender: insuranceForm.value.gender || 'MALE',
      carType: insuranceForm.value.carType,
      carPrice: parseInt(insuranceForm.value.carPrice),
      engineSize: parseInt(insuranceForm.value.engineSize),
      carYear: parseInt(insuranceForm.value.carYear),
      annualMileage: annualMileage, // 검증된 값 사용
      licenseYear: parseInt(insuranceForm.value.licenseYear),
      accidentCount: parseInt(insuranceForm.value.accidentCount || '0'),
      drunkDriving: insuranceForm.value.drunkDriving || false,
      trafficViolations: parseInt(insuranceForm.value.trafficViolations || '0'),
      region: insuranceForm.value.region,
      isDirect: insuranceForm.value.isDirect || false,
      options: {
        blackBox: insuranceForm.value.options.blackBox || false,
        hasChild: insuranceForm.value.options.hasChild || false,
        hasMultipleChildren: insuranceForm.value.options.hasMultipleChildren || false,
        adas: insuranceForm.value.options.adas || false,
        isMilitaryDriver: insuranceForm.value.options.isMilitaryDriver || false,
        usesPublicTransport: insuranceForm.value.options.usesPublicTransport || false
      }
    };

    console.log('보내는 데이터:', requestData); // 디버깅용 

    const response = await axios.post('http://localhost:8080/api/calculate-insurance', requestData);
    console.log('백엔드 응답 데이터:', response.data); // 응답 데이터 확인용 로그
    insuranceResult.value = response.data;
    insuranceError.value = ''; // 성공 시 에러 메시지 초기화
  } catch (error) {
    console.error('보험료 계산 중 오류 발생:', error);
    insuranceError.value = '보험료 계산 중 오류가 발생했습니다.';
    insuranceResult.value = null;
  }
};

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

const formatNumber = (value) => {
  if (!value) return '';
  return value.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
};

const unformatNumber = (value) => {
  if (!value) return '';
  return value.replace(/,/g, '');
};

const handlePriceInput = (event) => {
  const unformatted = unformatNumber(event.target.value);
  const numeric = unformatted.replace(/[^\d]/g, '');
  insuranceForm.value.carPrice = numeric;
  event.target.value = formatNumber(numeric);
};
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

        <div class="space-y-12">
          <!-- 주유소 검색 섹션 -->
          <div class="grid grid-cols-6 gap-6">
            <div class="col-span-3 bg-white dark:bg-gray-800 rounded-xl shadow-lg p-6">
              <h2 class="text-2xl font-bold text-gray-900 dark:text-white mb-6 flex items-center">
                <span class="mr-3">⛽</span>주변 주유소 검색
              </h2>
              <div class="flex gap-4">
                <input 
                  v-model="addressInput" 
                  type="text" 
                  placeholder="주소를 입력하세요" 
                  @keyup.enter="searchGasStations"
                  class="flex-1 p-3 text-gray-900 dark:text-gray-100 bg-white dark:bg-gray-700 border-2 border-gray-300 dark:border-gray-600 rounded-xl shadow-sm focus:border-blue-500 focus:ring-4 focus:ring-blue-100 dark:focus:ring-blue-900 transition-all duration-200"
                />
                <button 
                  @click="searchGasStations"
                  class="px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-xl shadow-sm hover:shadow-md transition-all duration-200 flex items-center"
                >
                  <span class="mr-2">검색</span>
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clip-rule="evenodd" />
                  </svg>
                </button>
              </div>
              <div v-if="searchError" class="mt-4 p-4 bg-orange-50 dark:bg-orange-900/20 border-l-4 border-orange-500 text-orange-700 dark:text-orange-300">
                {{ searchError }}
              </div>
              <ul v-else class="mt-6 space-y-3">
                <li 
                  v-for="station in gasolineStations" 
                  :key="station.UNI_ID"
                  class="p-4 bg-white dark:bg-gray-700 rounded-lg shadow-sm hover:shadow-md transition-all duration-200"
                >
                  <div class="flex justify-between items-center">
                    <span class="text-gray-900 dark:text-gray-100 font-medium">{{ station.OS_NM }}</span>
                    <div class="flex items-center gap-4">
                      <span class="text-gray-600 dark:text-gray-400">{{ station.DISTANCE }}m</span>
                      <span class="text-lg font-bold text-blue-600 dark:text-blue-400">{{ station.PRICE }}원</span>
                    </div>
                  </div>
                </li>
              </ul>
            </div>
            
            <!-- 추가 정보 섹션 -->
            <div class="col-span-3 bg-white dark:bg-gray-800 rounded-xl shadow-lg p-6">
              <h2 class="text-2xl font-bold text-gray-900 dark:text-white mb-6 flex items-center">
                <span class="mr-3">ℹ️</span>보험료 계산
              </h2>
              <div class="space-y-4">
                <!-- 에러 메시지 표시 -->
                <div v-if="insuranceError" class="p-4 bg-red-50 dark:bg-red-900/20 border-l-4 border-red-500 text-red-700 dark:text-red-300">
                  {{ insuranceError }}
                </div>
                
                <!-- 기본 정보 -->
                <div class="grid grid-cols-2 gap-4">
                  <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">나이</label>
                    <input 
                      v-model="insuranceForm.age" 
                      type="number" 
                      class="w-full p-2 border rounded-lg dark:bg-gray-700 dark:border-gray-600"
                      min="18"
                      max="100"
                      placeholder="나이"
                    />
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">성별</label>
                    <select 
                      v-model="insuranceForm.gender" 
                      class="w-full p-2 border rounded-lg dark:bg-gray-700 dark:border-gray-600"
                    >
                      <option value="MALE">남성</option>
                      <option value="FEMALE">여성</option>
                    </select>
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">운전경력</label>
                    <input 
                      v-model="insuranceForm.licenseYear" 
                      type="number" 
                      class="w-full p-2 border rounded-lg dark:bg-gray-700 dark:border-gray-600"
                      min="0"
                      placeholder="면허 취득 기간"
                    />
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">사고 이력</label>
                    <input 
                      v-model="insuranceForm.accidentCount" 
                      type="number" 
                      class="w-full p-2 border rounded-lg dark:bg-gray-700 dark:border-gray-600"
                      min="0"
                      placeholder="사고 횟수"
                    />
                  </div>
                </div>

                <!-- 차량 정보 -->
                <div class="grid grid-cols-2 gap-4">
                  <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">차종</label>
                    <select 
                      v-model="insuranceForm.carType" 
                      class="w-full p-2 border rounded-lg dark:bg-gray-700 dark:border-gray-600"
                    >
                      <option value="경차">경차</option>
                      <option value="소형차">소형차</option>
                      <option value="중형차">중형차</option>
                      <option value="대형차">대형차</option>
                      <option value="SUV">SUV</option>
                      <option value="트럭">트럭</option>
                    </select>
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">배기량 (cc)</label>
                    <input 
                      v-model="insuranceForm.engineSize" 
                      type="number" 
                      class="w-full p-2 border rounded-lg dark:bg-gray-700 dark:border-gray-600"
                      placeholder="배기량"
                    />
                  </div>
                  <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">차량 가격</label>
                    <input 
                      :value="formatNumber(insuranceForm.carPrice)"
                      @input="handlePriceInput"
                      type="text"
                      class="w-full p-2 border rounded-lg dark:bg-gray-700 dark:border-gray-600"
                      min="0"
                      placeholder="차량 가격"
                    />
                  </div>
                   <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">연간 주행거리 (km)</label>
                    <input 
                      v-model="insuranceForm.annualMileage" 
                      type="number" 
                      class="w-full p-2 border rounded-lg dark:bg-gray-700 dark:border-gray-600"
                      min="0"
                      placeholder="연간 주행거리"
                    />
                  </div>
                   <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">연식</label>
                    <input 
                      v-model="insuranceForm.carYear" 
                      type="number" 
                      class="w-full p-2 border rounded-lg dark:bg-gray-700 dark:border-gray-600"
                      min="2000"
                      :max="new Date().getFullYear()"
                      placeholder="차량 연식"
                    />
                  </div>
                   <div>
                    <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">지역</label>
                    <select 
                      v-model="insuranceForm.region" 
                      class="w-full p-2 border rounded-lg dark:bg-gray-700 dark:border-gray-600"
                    >
                      <option value="서울">서울</option>
                      <option value="부산">부산</option>
                      <option value="인천">인천</option>
                      <option value="대구">대구</option>
                      <option value="광주">광주</option>
                      <option value="대전">대전</option>
                      <option value="울산">울산</option>
                      <option value="세종">세종</option>
                      <option value="경기">경기</option>
                      <option value="강원">강원</option>
                      <option value="충북">충북</option>
                      <option value="충남">충남</option>
                      <option value="전북">전북</option>
                      <option value="전남">전남</option>
                      <option value="경북">경북</option>
                      <option value="경남">경남</option>
                      <option value="제주">제주</option>
                    </select>
                  </div>
                </div>

                <!-- 할인 옵션 -->
                <div class="space-y-3">
                  <h3 class="text-lg font-semibold text-gray-900 dark:text-white">추가 옵션</h3>
                  <div class="grid grid-cols-2 gap-4">
                    <label class="flex items-center">
                      <input 
                        type="checkbox" 
                        v-model="insuranceForm.drunkDriving" 
                        class="mr-2"
                      />
                      <span class="text-sm text-gray-700 dark:text-gray-300">음주운전 이력</span>
                    </label>
                    <label class="flex items-center">
                      <input 
                        type="checkbox" 
                        v-model="insuranceForm.isDirect" 
                        class="mr-2"
                      />
                      <span class="text-sm text-gray-700 dark:text-gray-300">다이렉트 가입</span>
                    </label>
                    <label class="flex items-center">
                      <input 
                        type="checkbox" 
                        v-model="insuranceForm.options.blackBox" 
                        class="mr-2"
                      />
                      <span class="text-sm text-gray-700 dark:text-gray-300">블랙박스 설치</span>
                    </label>
                    <label class="flex items-center">
                      <input 
                        type="checkbox" 
                        v-model="insuranceForm.options.hasChild" 
                        class="mr-2"
                      />
                      <span class="text-sm text-gray-700 dark:text-gray-300">만 6세 이하 자녀 1명</span>
                    </label>
                    <label class="flex items-center">
                      <input 
                        type="checkbox" 
                        v-model="insuranceForm.options.hasMultipleChildren" 
                        class="mr-2"
                      />
                      <span class="text-sm text-gray-700 dark:text-gray-300">자녀 2명 이상</span>
                    </label>
                    <label class="flex items-center">
                      <input 
                        type="checkbox" 
                        v-model="insuranceForm.options.adas" 
                        class="mr-2"
                      />
                      <span class="text-sm text-gray-700 dark:text-gray-300">ADAS 장착</span>
                    </label>
                    <label class="flex items-center">
                      <input 
                        type="checkbox" 
                        v-model="insuranceForm.options.isMilitaryDriver" 
                        class="mr-2"
                      />
                      <span class="text-sm text-gray-700 dark:text-gray-300">군 운전병</span>
                    </label>
                    <label class="flex items-center">
                      <input 
                        type="checkbox" 
                        v-model="insuranceForm.options.usesPublicTransport" 
                        class="mr-2"
                      />
                      <span class="text-sm text-gray-700 dark:text-gray-300">대중교통 이용</span>
                    </label>
                  </div>
                </div>

                <button 
                  @click="sendInsuranceData"
                  class="w-full py-3 px-4 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-lg transition-colors duration-200"
                >
                  보험료 계산하기
                </button>

                <!-- 안내 문구 -->
                <div class="mt-4 p-4 bg-gray-50 dark:bg-gray-800/50 rounded-lg">
                  <p class="text-sm text-gray-600 dark:text-gray-400">
                    ※ 본 계산은 간이 계산으로, 실제 보험료와 차이가 있을 수 있습니다. 정확한 보험료는 보험사 상담을 통해 확인하시기 바랍니다.
                  </p>
                </div>

                <!-- 계산 결과 -->
                <div v-if="insuranceResult" class="mt-4 space-y-4">
                  <div class="p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
                    <p class="text-xl font-bold text-blue-900 dark:text-blue-400">
                      예상 보험료: {{ insuranceResult.insuranceFee?.toLocaleString() || '0' }}원
                    </p>
                  </div>
                  
                  <!-- 상세 내역 -->
                  <div class="p-4 bg-gray-50 dark:bg-gray-800/50 rounded-lg">
                    <h4 class="text-lg font-semibold text-gray-900 dark:text-white mb-3">상세 내역</h4>
                    <div class="space-y-2">
                      <p class="text-sm text-gray-700 dark:text-gray-300">
                        기본 보험료: {{ insuranceResult.details?.baseInsurance?.toLocaleString() || '0' }}원
                      </p>
                      <div class="text-sm">
                        <p class="font-medium text-gray-700 dark:text-gray-300 mb-1">할증 항목:</p>
                        <ul class="list-disc list-inside space-y-1 text-gray-600 dark:text-gray-400">
                          <li>운전자 위험도: {{ insuranceResult.details?.driverRisk?.toFixed(1) || '0' }}%</li>
                          <li>지역 위험도: {{ insuranceResult.details?.regionRisk?.toFixed(1) || '0' }}%</li>
                        </ul>
                      </div>
                      <div v-if="insuranceResult.details?.appliedDiscounts && Object.keys(insuranceResult.details.appliedDiscounts).length > 0" class="text-sm">
                        <p class="font-medium text-gray-700 dark:text-gray-300 mb-1">적용된 할인:</p>
                        <ul class="list-disc list-inside space-y-1 text-gray-600 dark:text-gray-400">
                          <li v-for="(discountPercentage, discountName) in insuranceResult.details.appliedDiscounts" :key="discountName">
                            {{ discountName }}: {{ (discountPercentage * 100).toFixed(1) }}%
                          </li>
                        </ul>
                      </div>
                      <p class="text-sm text-gray-700 dark:text-gray-300">
                        총 할인율: {{ (insuranceResult.details?.totalDiscount || 0).toFixed(1) }}%
                      </p>
                    </div>
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