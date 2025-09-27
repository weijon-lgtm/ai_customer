<template>
  <div class="register-container">
    <div class="background-decoration">
      <div class="floating-shape shape-1"></div>
      <div class="floating-shape shape-2"></div>
      <div class="floating-shape shape-3"></div>
      <div class="floating-shape shape-4"></div>
    </div>
    
    <el-card class="register-card">
      <template #header>
        <div class="header">
          <div class="logo-container">
            <div class="logo-icon">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/>
              </svg>
            </div>
          </div>
          <h2 class="title">创建账号</h2>
          <p class="subtitle">加入AI智能客服平台</p>
        </div>
      </template>
      
      <div class="form-container">
        <el-form 
          :model="registerForm" 
          :rules="rules" 
          ref="registerFormRef"
        >
          <el-form-item prop="username" class="form-item">
            <el-input 
              v-model="registerForm.username" 
              placeholder="请输入用户名" 
              :prefix-icon="User"
              size="large"
              class="custom-input"
            />
          </el-form-item>
          
          <el-form-item prop="password" class="form-item">
            <el-input 
              v-model="registerForm.password" 
              type="password" 
              placeholder="请输入密码" 
              :prefix-icon="Lock"
              size="large"
              show-password
              class="custom-input"
            />
          </el-form-item>
          
          <el-form-item prop="email" class="form-item">
            <el-input 
              v-model="registerForm.email" 
              placeholder="请输入邮箱" 
              :prefix-icon="Message"
              size="large"
              class="custom-input"
            />
          </el-form-item>
          
          <el-form-item class="form-item">
            <el-button 
              type="primary" 
              @click="register" 
              :loading="loading" 
              class="register-button"
              size="large"
            >
              <span v-if="!loading">立即注册</span>
              <span v-else>注册中...</span>
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="divider">
          <span>或者</span>
        </div>
        
        <div class="login-link">
          <span>已有账号？</span>
          <router-link to="/login">立即登录</router-link>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, getCurrentInstance } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Message } from '@element-plus/icons-vue'

export default {
  name: 'Register',
  setup() {
    const { proxy } = getCurrentInstance()
    const router = useRouter()
    
    const registerFormRef = ref(null)
    const registerForm = ref({
      username: '',
      password: '',
      email: ''
    })
    
    const rules = ref({
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '长度应为3到20个字符', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码至少需要6个字符', trigger: 'blur' }
      ],
      email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
      ]
    })
    
    const loading = ref(false)
    
    // 错误消息翻译函数
    const translateErrorMessage = (message) => {
      const errorTranslations = {
        'Registration successful': '注册成功',
        'Registration failed, please try again': '注册失败，请重试',
        'Username already exists': '用户名已存在',
        'This username is already taken, please try another one': '该用户名已被占用，请尝试其他用户名',
        'Registration failed, please try again later': '注册失败，请稍后重试',
        'Email already exists': '邮箱已存在',
        'Invalid email format': '邮箱格式无效',
        'Password too weak': '密码强度太弱',
        'Server error': '服务器错误',
        'Network error': '网络错误'
      }
      
      return errorTranslations[message] || message || '注册失败，请重试'
    }
    
    const register = () => {
      registerFormRef.value.validate((valid) => {
        if (valid) {
          loading.value = true
          // 确保使用JSON格式发送，设置正确的headers
          proxy.$axios({
            method: 'post',
            url: '/account/register',
            data: registerForm.value,
            headers: {
              'Content-Type': 'application/json'
            }
          })
          .then(response => {
            loading.value = false
            if (response.data.code === 200) {
              ElMessage.success('注册成功')
              router.push('/login')
            } else {
              // 使用温和的信息提示，翻译错误消息
              const translatedMessage = translateErrorMessage(response.data.message)
              ElMessage.info(translatedMessage)
            }
          })
          .catch(error => {
            loading.value = false
            console.error('Register error:', error)
            
            // 检查是否是用户名已存在的错误
            const errorMsg = error.response?.data?.message || error.message
            if (errorMsg.includes('already exists') || 
                errorMsg.includes('用户名已存在') || 
                errorMsg.includes('Username already exists')) {
              ElMessage({
                message: '该用户名已被占用，请尝试其他用户名',
                type: 'error',
                showClose: true,
                duration: 4000,
                showIcon: false  // 关键：不显示图标
              })
            } else if (errorMsg.includes('email') || errorMsg.includes('邮箱')) {
              ElMessage({
                message: '邮箱格式错误或已被使用',
                type: 'error',
                showClose: true,
                duration: 4000,
                showIcon: false
              })
            } else {
              ElMessage({
                message: '注册失败，请稍后重试',
                type: 'error',
                showClose: true,
                duration: 3000,
                showIcon: false  // 关键：不显示图标
              })
            }
          })
        }
      })
    }
    
    return {
      registerFormRef,
      registerForm,
      rules,
      loading,
      register,
      translateErrorMessage,
      User,
      Lock,
      Message
    }
  }
}
</script>

<style scoped>
.register-container {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  overflow: hidden;
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
  opacity: 0.1;
  animation: float 6s ease-in-out infinite;
}

.shape-1 {
  width: 80px;
  height: 80px;
  background: linear-gradient(45deg, #fff, #f0f0f0);
  top: 20%;
  left: 10%;
  animation-delay: 0s;
}

.shape-2 {
  width: 120px;
  height: 120px;
  background: linear-gradient(45deg, #fff, #e0e0e0);
  top: 60%;
  right: 15%;
  animation-delay: -2s;
}

.shape-3 {
  width: 60px;
  height: 60px;
  background: linear-gradient(45deg, #fff, #f5f5f5);
  bottom: 30%;
  left: 20%;
  animation-delay: -4s;
}

.shape-4 {
  width: 100px;
  height: 100px;
  background: linear-gradient(45deg, #fff, #ebebeb);
  top: 10%;
  right: 30%;
  animation-delay: -1s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(180deg); }
}

.register-card {
  position: relative;
  z-index: 2;
  width: 480px;
  max-width: 90vw;
  border-radius: 20px;
  border: none;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15), 
              0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95);
  overflow: hidden;
  transition: all 0.3s ease;
}

.register-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 30px 60px rgba(0, 0, 0, 0.2), 
              0 12px 40px rgba(0, 0, 0, 0.15);
}

.register-card :deep(.el-card__header) {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  padding: 32px 24px;
  border-bottom: none;
}

.register-card :deep(.el-card__body) {
  padding: 32px 24px;
}

.header {
  text-align: center;
}

.logo-container {
  margin-bottom: 20px;
}

.logo-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 50px;
  height: 50px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  backdrop-filter: blur(10px);
  margin: 0 auto 16px;
}

.logo-icon svg {
  width: 26px;
  height: 26px;
  color: white;
}

.title {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: white;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.subtitle {
  margin: 0;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  font-weight: 300;
}

.form-container {
  margin-top: -10px;
}

.form-item {
  margin-bottom: 20px;
}

.custom-input :deep(.el-input__wrapper) {
  border-radius: 14px;
  border: 2px solid #f0f0f0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  padding: 6px 14px;
  background: white;
}

.custom-input :deep(.el-input__wrapper:hover) {
  border-color: #667eea;
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.15);
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1),
              0 6px 16px rgba(102, 126, 234, 0.15);
}

.custom-input :deep(.el-input__inner) {
  font-size: 15px;
  color: #2c3e50;
  height: 44px;
}

.custom-input :deep(.el-input__prefix-inner) {
  color: #667eea;
  margin-right: 8px;
}

.register-button {
  width: 100%;
  height: 48px;
  border-radius: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  font-size: 16px;
  font-weight: 600;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.register-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s ease;
}

.register-button:hover::before {
  left: 100%;
}

.register-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(102, 126, 234, 0.4);
}

.register-button:active {
  transform: translateY(0);
}

.divider {
  margin: 24px 0;
  text-align: center;
  position: relative;
  color: #8f9bb3;
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, #e1e8ed, transparent);
}

.divider span {
  background: rgba(255, 255, 255, 0.95);
  padding: 0 16px;
  font-size: 13px;
  position: relative;
  z-index: 1;
}

.login-link {
  text-align: center;
  color: #8f9bb3;
  font-size: 14px;
  margin-top: 6px;
}

.login-link a {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  margin-left: 6px;
  position: relative;
  transition: all 0.3s ease;
}

.login-link a::after {
  content: '';
  position: absolute;
  width: 0;
  height: 2px;
  bottom: -2px;
  left: 50%;
  background: linear-gradient(90deg, #667eea, #764ba2);
  transition: all 0.3s ease;
  transform: translateX(-50%);
}

.login-link a:hover {
  color: #764ba2;
}

.login-link a:hover::after {
  width: 100%;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .register-card {
    width: 90vw;
    margin: 16px;
    border-radius: 18px;
  }
  
  .register-card :deep(.el-card__header) {
    padding: 24px 20px;
  }
  
  .register-card :deep(.el-card__body) {
    padding: 24px 20px;
  }
  
  .title {
    font-size: 22px;
  }
  
  .subtitle {
    font-size: 13px;
  }
  
  .floating-shape {
    display: none;
  }
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .register-card {
    background: rgba(30, 30, 30, 0.95);
  }
  
  .custom-input :deep(.el-input__wrapper) {
    background: rgba(255, 255, 255, 0.05);
    border-color: rgba(255, 255, 255, 0.1);
  }
  
  .custom-input :deep(.el-input__inner) {
    color: #e0e0e0;
  }
  
  .divider span {
    background: rgba(30, 30, 30, 0.95);
  }
}
</style>