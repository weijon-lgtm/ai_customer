<template>
  <div class="chat-container">
    <div class="background-decoration">
      <div class="floating-shape shape-1"></div>
      <div class="floating-shape shape-2"></div>
      <div class="floating-shape shape-3"></div>
      <div class="floating-shape shape-4"></div>
    </div>
    
    <el-card class="chat-card">
      <template #header>
        <div class="header">
          <div class="logo-container">
            <div class="logo-icon">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 2C13.1 2 14 2.9 14 4C14 5.1 13.1 6 12 6C10.9 6 10 5.1 10 4C10 2.9 10.9 2 12 2M21 9V7L15 1L9 7V9C9 10.1 9.9 11 11 11V16L12 17L13 16V11C14.1 11 15 10.1 15 9M11 3.5L12.5 2L14 3.5V9H10V3.5Z"/>
              </svg>
            </div>
          </div>
          <h2 class="title">AI智能客服</h2>
          <p class="subtitle">贴心服务，随时为您解答</p>
          <div class="conversation-info" v-if="messages.length > 1">
            <span>对话轮数: {{ Math.floor((messages.length - 1) / 2) }}</span>
          </div>
        </div>
      </template>
      
      <div class="chat-messages" ref="messagesContainer">
        <div 
          v-for="(message, index) in messages" 
          :key="index" 
          class="message" 
          :class="message.isUser ? 'user' : 'assistant'"
        >
          <div class="message-avatar">
            <el-avatar 
              :size="36" 
              :style="{ 
                backgroundColor: message.isUser ? '#667eea' : '#67C23A',
                boxShadow: '0 4px 12px rgba(102, 126, 234, 0.3)'
              }"
            >
              <el-icon v-if="message.isUser"><User /></el-icon>
              <el-icon v-else><Service /></el-icon>
            </el-avatar>
          </div>
          <div class="message-content">
            <div class="message-bubble">
              <div v-if="message.isUser">{{ message.content }}</div>
              <div v-else v-html="formatMessage(message.content)"></div>
            </div>
          </div>
        </div>
        
        <div v-if="loading" class="message assistant">
          <div class="message-avatar">
            <el-avatar 
              :size="36" 
              :style="{ 
                backgroundColor: '#67C23A',
                boxShadow: '0 4px 12px rgba(103, 194, 58, 0.3)'
              }"
            >
              <el-icon><Service /></el-icon>
            </el-avatar>
          </div>
          <div class="message-content">
            <div class="message-bubble thinking-bubble">
              <div class="thinking-content">
                <span class="thinking-text">正在思考中</span>
                <div class="thinking-dots">
                  <span class="dot"></span>
                  <span class="dot"></span>
                  <span class="dot"></span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="chat-input">
        <div class="input-actions" v-if="messages.length > 1">
          <el-button 
            size="small" 
            type="info" 
            plain 
            @click="clearConversation"
            :disabled="loading"
            class="clear-button"
          >
            清除对话
          </el-button>
        </div>
        
        <el-input
          v-model="inputMessage"
          placeholder="请输入您的消息..."
          @keyup.enter="sendMessage"
          :disabled="loading"
          size="large"
          class="custom-input"
        >
          <template #append>
            <el-button 
              :icon="Promotion" 
              @click="sendMessage" 
              :loading="loading"
              type="primary"
              class="send-button"
            >
              发送
            </el-button>
          </template>
        </el-input>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, nextTick, getCurrentInstance, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Service, Promotion } from '@element-plus/icons-vue'

export default {
  name: 'Chat',
  setup() {
    const { proxy } = getCurrentInstance()
    
    const messages = ref([])
    const inputMessage = ref('')
    const loading = ref(false)
    const userId = ref(null)
    const messagesContainer = ref(null)
    
    onMounted(() => {
      const user = JSON.parse(localStorage.getItem('user') || '{}')
      if (user && user.id) {
        userId.value = user.id
      } else {
        userId.value = Math.floor(Math.random() * 1000000)
        console.log('生成临时用户ID:', userId.value)
      }
      
      // Add welcome message
      messages.value.push({
        content: '您好！今天我可以为您做些什么？',
        isUser: false
      })
    })
    
    const scrollToBottom = () => {
      nextTick(() => {
        if (messagesContainer.value) {
          messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
        }
      })
    }
    
    const formatMessage = (message) => {
      try {
        if (!message || typeof message !== 'string' || message.trim() === '') {
          console.warn('formatMessage 收到无效消息:', message)
          return '<p>消息内容为空</p>'
        }
        
        let formatted = message
          .replace(/\n\n+/g, '</p><p>')
          .replace(/\n/g, '<br>')
          .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
          .replace(/\*(.*?)\*/g, '<em>$1</em>')
          .replace(/###\s*(.*?)$/gm, '<h3>$1</h3>')
          .replace(/##\s*(.*?)$/gm, '<h2>$1</h2>')
          .replace(/^\s*-\s+(.*?)$/gm, '<li>$1</li>')
          .replace(/(<li>.*?<\/li>)/gs, '<ul>$1</ul>')
        
        if (!formatted.includes('<p>') && !formatted.includes('<h') && !formatted.includes('<ul>')) {
          formatted = `<p>${formatted}</p>`
        }
        
        formatted = formatted.replace(/<p><\/p>/g, '')
        
        return formatted
        
      } catch (error) {
        console.error('formatMessage 错误:', error, '原始消息:', message)
        return `<p>消息格式化失败</p>`
      }
    }
    
    // 构建对话历史
    const buildConversationHistory = () => {
      const history = []
      
      // 跳过欢迎消息，从第二条消息开始处理
      for (let i = 1; i < messages.value.length; i++) {
        const message = messages.value[i]
        if (message && message.content && message.content.trim()) {
          history.push({
            role: message.isUser ? 'user' : 'assistant',
            content: message.content.trim()
          })
        }
      }
      
      console.log('构建的对话历史:', history)
      return history
    }
    
    // 清除对话功能
    const clearConversation = async () => {
      try {
        await ElMessageBox.confirm('确定要清除所有对话记录吗？', '确认清除', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        })
        
        // 保留欢迎消息，清除其他消息
        messages.value = messages.value.slice(0, 1)
        ElMessage.success('对话已清除')
        
      } catch (error) {
        console.log('用户取消清除操作')
      }
    }
    
    const sendMessage = async () => {
      if (!inputMessage.value.trim()) return
      
      const userMessageText = inputMessage.value.trim()
      
      // Add user message
      messages.value.push({
        content: userMessageText,
        isUser: true
      })
      
      // 构建对话历史（不包括刚添加的用户消息，因为它会作为当前问题发送）
      const conversationHistory = buildConversationHistory()
      // 移除最后一条消息（当前问题），因为它会单独作为question字段发送
      const historyWithoutCurrent = conversationHistory.slice(0, -1)
      
      // 构建请求数据 - 只有在真正有历史对话时才发送history字段
      const requestData = {
        question: userMessageText,
        userId: userId.value
      }
      
      // 只有在有实际历史对话时才添加 history 字段
      if (historyWithoutCurrent.length > 0) {
        requestData.history = historyWithoutCurrent
        console.log('发送多轮对话请求，历史对话数量:', historyWithoutCurrent.length)
      } else {
        console.log('发送首轮对话请求，无历史对话')
      }
      
      console.log('发送请求数据:', requestData)
      
      inputMessage.value = ''
      loading.value = true
      
      scrollToBottom()
      
      try {
        if (proxy && proxy.$axios) {
          const response = await proxy.$axios.post('/qa/message', requestData)
          console.log('通过Gateway的响应:', response)
          console.log('响应数据详情:', response.data)
          
          if (response.data) {
            let finalMessage = '';
            
            if (response.data.message) {
              finalMessage = response.data.message;
              console.log('使用 response.data.message:', finalMessage);
            } else if (response.data.answer) {
              finalMessage = response.data.answer;
              console.log('使用 response.data.answer:', finalMessage);
            } else if (response.data.content) {
              finalMessage = response.data.content;
              console.log('使用 response.data.content:', finalMessage);
            } else if (typeof response.data === 'string') {
              finalMessage = response.data;
              console.log('直接使用字符串响应:', finalMessage);
            } else {
              finalMessage = '收到响应但格式不正确';
              console.warn('未识别的响应格式:', response.data);
            }
            
            messages.value.push({
              content: finalMessage || '抱歉，未收到有效回复',
              isUser: false
            });
            
          } else {
            messages.value.push({
              content: '服务器响应为空',
              isUser: false
            });
          }
          
        } else {
          throw new Error('Axios实例未找到，请检查应用配置')
        }
        
      } catch (error) {
        console.error('发送消息失败:', error)
        console.error('错误详情:', error.response?.data || error.message)
        
        ElMessage.error('发送消息失败: ' + error.message)
        
        let errorMessage = '抱歉，发送消息时出现错误';
        if (error.response) {
          errorMessage = `服务器错误 (${error.response.status}): ${error.response.data?.message || error.response.statusText}`;
        } else if (error.request) {
          errorMessage = '网络连接错误，请检查网络设置';
        } else {
          errorMessage = `请求配置错误: ${error.message}`;
        }
        
        messages.value.push({
          content: errorMessage,
          isUser: false
        })
      } finally {
        loading.value = false
        scrollToBottom()
      }
    }
    
    return {
      messages,
      inputMessage,
      loading,
      messagesContainer,
      sendMessage,
      formatMessage,
      clearConversation,
      User,
      Service,
      Promotion
    }
  }
}
</script>

<style scoped>
.chat-container {
  position: relative;
  width: 100%;
  height: calc(100vh - 80px);
  min-height: calc(100vh - 80px);
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  overflow: hidden;
  box-sizing: border-box;
}

/* 背景装饰元素 */
.background-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

.floating-shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.08;
  animation: float 8s ease-in-out infinite;
}

.shape-1 {
  width: 100px;
  height: 100px;
  background: linear-gradient(45deg, #fff, #f0f0f0);
  top: 15%;
  left: 8%;
  animation-delay: 0s;
}

.shape-2 {
  width: 140px;
  height: 140px;
  background: linear-gradient(45deg, #fff, #e0e0e0);
  top: 65%;
  right: 10%;
  animation-delay: -3s;
}

.shape-3 {
  width: 80px;
  height: 80px;
  background: linear-gradient(45deg, #fff, #f5f5f5);
  bottom: 25%;
  left: 15%;
  animation-delay: -6s;
}

.shape-4 {
  width: 120px;
  height: 120px;
  background: linear-gradient(45deg, #fff, #ebebeb);
  top: 8%;
  right: 25%;
  animation-delay: -1.5s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-25px) rotate(180deg); }
}

.chat-card {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 900px;
  height: calc(100vh - 120px);
  max-height: 750px;
  min-height: 500px;
  border-radius: 20px;
  border: none;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.15), 
              0 10px 40px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(15px);
  background: rgba(255, 255, 255, 0.95);
  overflow: hidden;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
}

.chat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 35px 70px rgba(0, 0, 0, 0.2), 
              0 15px 50px rgba(0, 0, 0, 0.15);
}

.chat-card :deep(.el-card__header) {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  padding: 16px 24px;
  border-bottom: none;
  flex-shrink: 0;
}

.chat-card :deep(.el-card__body) {
  flex: 1;
  padding: 0;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.98);
  min-height: 0;
}

.header {
  text-align: center;
}

.logo-container {
  margin-bottom: 8px;
}

.logo-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  backdrop-filter: blur(10px);
  margin: 0 auto 8px;
}

.logo-icon svg {
  width: 20px;
  height: 20px;
  color: white;
}

.title {
  margin: 0 0 4px 0;
  font-size: 20px;
  font-weight: 600;
  color: white;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.subtitle {
  margin: 0 0 6px 0;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.85);
  font-weight: 300;
}

.conversation-info {
  margin-top: 6px;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.7);
  background: rgba(255, 255, 255, 0.1);
  padding: 3px 10px;
  border-radius: 10px;
  display: inline-block;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: linear-gradient(145deg, #f8f9fa 0%, #e9ecef 100%);
  min-height: 0;
}

.message {
  display: flex;
  margin-bottom: 20px;
  align-items: flex-start;
  animation: messageSlideIn 0.4s ease-out;
}

@keyframes messageSlideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  margin: 0 12px;
  flex-shrink: 0;
  transition: transform 0.2s ease;
}

.message-avatar:hover {
  transform: scale(1.1);
}

.message-content {
  max-width: 70%;
  word-wrap: break-word;
  word-break: break-word;
}

.message-bubble {
  padding: 16px 20px;
  border-radius: 16px;
  word-wrap: break-word;
  word-break: break-word;
  white-space: pre-wrap;
  line-height: 1.6;
  overflow-wrap: break-word;
  hyphens: auto;
  position: relative;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.user .message-bubble {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border-bottom-right-radius: 6px;
}

.user .message-bubble:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.3);
}

.assistant .message-bubble {
  background: white;
  color: #2c3e50;
  border: 1px solid rgba(102, 126, 234, 0.1);
  border-bottom-left-radius: 6px;
}

.assistant .message-bubble:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
  border-color: rgba(102, 126, 234, 0.2);
}

.thinking-bubble {
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  border: 1px solid rgba(103, 194, 58, 0.2);
}

.thinking-content {
  display: flex;
  align-items: center;
  gap: 10px;
}

.thinking-text {
  color: #67C23A;
  font-size: 14px;
  font-weight: 500;
}

.thinking-dots {
  display: flex;
  gap: 4px;
}

.dot {
  width: 8px;
  height: 8px;
  background: linear-gradient(135deg, #67C23A, #85ce61);
  border-radius: 50%;
  animation: thinking 1.5s infinite;
}

.dot:nth-child(1) { animation-delay: 0s; }
.dot:nth-child(2) { animation-delay: 0.3s; }
.dot:nth-child(3) { animation-delay: 0.6s; }

@keyframes thinking {
  0%, 60%, 100% {
    opacity: 0.3;
    transform: scale(1);
  }
  30% {
    opacity: 1;
    transform: scale(1.3);
  }
}

.chat-input {
  flex-shrink: 0;
  padding: 24px;
  background: rgba(255, 255, 255, 0.98);
  border-top: 1px solid rgba(102, 126, 234, 0.1);
  backdrop-filter: blur(10px);
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}

.clear-button {
  border-radius: 8px;
  border: 1px solid rgba(102, 126, 234, 0.3);
  color: #667eea;
  transition: all 0.3s ease;
}

.clear-button:hover {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  transform: translateY(-1px);
}

.custom-input :deep(.el-input__wrapper) {
  border-radius: 16px;
  border: 2px solid rgba(102, 126, 234, 0.2);
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.1);
  transition: all 0.3s ease;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.custom-input :deep(.el-input-group__append) {
  background: transparent !important;
  border: none !important;
  padding: 0 !important;
  box-shadow: none !important;
}

.custom-input :deep(.el-input-group__append .el-button) {
  margin: 0 !important;
  border: none !important;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
}

.custom-input :deep(.el-input__wrapper:hover) {
  border-color: #667eea;
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.2);
  transform: translateY(-1px);
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1),
              0 8px 24px rgba(102, 126, 234, 0.2);
}

.custom-input :deep(.el-input__inner) {
  font-size: 15px;
  color: #2c3e50;
  height: 44px;
}

.send-button {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  border: none !important;
  border-radius: 12px;
  font-weight: 600;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  outline: none !important;
  box-shadow: none !important;
}

.send-button:focus {
  outline: none !important;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.3) !important;
}

.send-button:hover:not(:disabled) {
  background: linear-gradient(135deg, #5a6fd8 0%, #6a42a0 100%) !important;
  border: none !important;
}

.send-button:active {
  background: linear-gradient(135deg, #4f63d2 0%, #5d3a94 100%) !important;
  border: none !important;
}

.send-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s ease;
}

.send-button:hover::before {
  left: 100%;
}

.send-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4) !important;
  background: linear-gradient(135deg, #5a6fd8 0%, #6a42a0 100%) !important;
}

/* 消息气泡内的格式化内容 */
.message-bubble :deep(p) {
  margin: 0 0 10px 0;
}

.message-bubble :deep(p:last-child) {
  margin-bottom: 0;
}

.message-bubble :deep(ul), .message-bubble :deep(ol) {
  margin: 10px 0;
  padding-left: 24px;
}

.message-bubble :deep(li) {
  margin-bottom: 6px;
}

.message-bubble :deep(strong) {
  font-weight: 600;
  color: inherit;
}

.message-bubble :deep(em) {
  font-style: italic;
  color: inherit;
}

.message-bubble :deep(h2), .message-bubble :deep(h3) {
  margin: 16px 0 10px 0;
  font-weight: 600;
  color: inherit;
}

.message-bubble :deep(h2) {
  font-size: 1.3em;
}

.message-bubble :deep(h3) {
  font-size: 1.15em;
}

/* 自定义滚动条 */
.chat-messages::-webkit-scrollbar {
  width: 8px;
}

.chat-messages::-webkit-scrollbar-track {
  background: rgba(102, 126, 234, 0.1);
  border-radius: 4px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 4px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(135deg, #5a6fd8, #6a42a0);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-container {
    padding: 10px;
    height: calc(100vh - 60px);
    min-height: calc(100vh - 60px);
  }
  
  .chat-card {
    height: calc(100vh - 80px);
    border-radius: 16px;
    max-height: none;
    min-height: 400px;
    max-width: 100%;
  }
  
  .chat-card :deep(.el-card__header) {
    padding: 14px 16px;
  }
  
  .title {
    font-size: 18px;
  }
  
  .subtitle {
    font-size: 11px;
  }
  
  .message-content {
    max-width: 85%;
  }
  
  .chat-messages {
    padding: 16px;
  }
  
  .chat-input {
    padding: 16px;
  }
  
  .floating-shape {
    display: none;
  }
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .chat-card {
    background: rgba(30, 30, 30, 0.95);
  }
  
  .chat-card :deep(.el-card__body) {
    background: rgba(40, 40, 40, 0.98);
  }
  
  .chat-messages {
    background: linear-gradient(145deg, #2c2c2c 0%, #3a3a3a 100%);
  }
  
  .assistant .message-bubble {
    background: rgba(50, 50, 50, 0.9);
    color: #e0e0e0;
    border-color: rgba(102, 126, 234, 0.2);
  }
  
  .custom-input :deep(.el-input__wrapper) {
    background: rgba(50, 50, 50, 0.95);
    border-color: rgba(102, 126, 234, 0.3);
  }
  
  .custom-input :deep(.el-input__inner) {
    color: #e0e0e0;
  }
  
  .chat-input {
    background: rgba(40, 40, 40, 0.98);
    border-top-color: rgba(102, 126, 234, 0.2);
  }
}
</style>