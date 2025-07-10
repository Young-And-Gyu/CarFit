<template>
  <div class="max-w-xl mx-auto mt-10 p-6 bg-white rounded shadow">
    <h2 class="text-2xl font-bold mb-6">게시글 작성</h2>
    <form @submit.prevent="submitBoard">
      <div class="mb-4">
        <label class="block mb-1 font-semibold">제목</label>
        <input v-model="title" type="text" class="w-full border rounded px-3 py-2" required />
      </div>
      <div class="mb-4">
        <label class="block mb-1 font-semibold">내용</label>
        <textarea v-model="content" class="w-full border rounded px-3 py-2" rows="8" required></textarea>
      </div>
      <button type="submit" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">등록</button>
    </form>
    <div v-if="message" class="mt-4 text-green-600">{{ message }}</div>
    <div v-if="error" class="mt-4 text-red-600">{{ error }}</div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'

const title = ref('')
const content = ref('')
const message = ref('')
const error = ref('')

const submitBoard = async () => {
  message.value = ''
  error.value = ''
  try {
    await axios.post('/api/boards', {
      title: title.value,
      content: content.value
    })
    message.value = '게시글이 성공적으로 등록되었습니다.'
    title.value = ''
    content.value = ''
  } catch (e) {
    error.value = '게시글 등록에 실패했습니다.'
  }
}
</script>

<style scoped>
</style> 