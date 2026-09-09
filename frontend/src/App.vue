<script setup>
// Vue 3 组合式 API（Composition API）：当前主流写法
import { onMounted, onUnmounted, ref } from 'vue'
import { createMessage, fetchMessages, fetchStats } from './api.js'

const content = ref('')
const messages = ref([])
const consumed = ref(0)
const sending = ref(false)
const error = ref('')
let timer = null

// 拉取消息列表与统计
async function refresh() {
  try {
    const [msgRes, statRes] = await Promise.all([fetchMessages(), fetchStats()])
    messages.value = msgRes.data
    consumed.value = statRes.data.consumed
    error.value = ''
  } catch (e) {
    error.value = '无法连接后端，请确认服务已启动'
  }
}

// 提交消息 -> 后端落库 -> Kafka 异步消费 -> 状态变为 CONSUMED
async function submit() {
  if (!content.value.trim() || sending.value) return
  sending.value = true
  try {
    await createMessage(content.value.trim())
    content.value = ''
    await refresh()
  } catch (e) {
    error.value = e.response?.data?.message || '提交失败'
  } finally {
    sending.value = false
  }
}

onMounted(() => {
  refresh()
  // 每 2 秒轮询一次，观察消息从 PENDING 变为 CONSUMED 的过程
  timer = setInterval(refresh, 2000)
})
onUnmounted(() => clearInterval(timer))
</script>

<template>
  <main class="container">
    <h1>全栈学习 · 消息面板</h1>
    <p class="desc">
      提交消息后：写入 MySQL → 投递 Kafka → 消费者处理 → 状态变为
      <b>CONSUMED</b>，查询走 Redis 缓存。
    </p>

    <form class="composer" @submit.prevent="submit">
      <input
        v-model="content"
        maxlength="200"
        placeholder="输入一条消息（最多 200 字）"
      />
      <button type="submit" :disabled="sending || !content.trim()">
        {{ sending ? '发送中…' : '发送' }}
      </button>
    </form>

    <p v-if="error" class="error">{{ error }}</p>

    <div class="stats">已被 Kafka 消费者处理：{{ consumed }} 条</div>

    <ul class="list">
      <li v-for="m in messages" :key="m.id">
        <span class="content">{{ m.content }}</span>
        <span :class="['status', m.status === 'CONSUMED' ? 'done' : 'pending']">
          {{ m.status }}
        </span>
      </li>
    </ul>
    <p v-if="!messages.length" class="empty">暂无消息，发一条试试 →</p>
  </main>
</template>

<style>
* { box-sizing: border-box; }
body {
  margin: 0;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  background: #f5f6f8;
  color: #2c3e50;
}
.container {
  max-width: 640px;
  margin: 40px auto;
  padding: 0 16px;
}
h1 { font-size: 22px; }
.desc { color: #7f8c8d; font-size: 13px; }
.composer { display: flex; gap: 8px; margin: 16px 0; }
.composer input {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  font-size: 14px;
}
.composer button {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  background: #409eff;
  color: #fff;
  font-size: 14px;
  cursor: pointer;
}
.composer button:disabled { opacity: 0.5; cursor: not-allowed; }
.error { color: #e74c3c; font-size: 13px; }
.stats { margin: 12px 0; font-size: 13px; color: #34495e; }
.list { list-style: none; padding: 0; }
.list li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  background: #fff;
  border-radius: 8px;
  padding: 10px 14px;
  margin-bottom: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}
.status { font-size: 12px; padding: 2px 8px; border-radius: 10px; }
.status.done { background: #e8f7ee; color: #27ae60; }
.status.pending { background: #fdf3e0; color: #e67e22; }
.empty { color: #95a5a6; font-size: 13px; }
</style>
