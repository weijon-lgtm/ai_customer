<template>
  <div id="app">
    <div class="background-decoration">
      <div class="floating-shape shape-1"></div>
      <div class="floating-shape shape-2"></div>
      <div class="floating-shape shape-3"></div>
      <div class="floating-shape shape-4"></div>
    </div>
    
    <el-container v-if="loggedIn">
      <el-header class="app-header">
        <div class="header-content">
          <div class="logo-section">
            <div class="logo-icon">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
              </svg>
            </div>
            <span class="logo-text">AI客服系统</span>
          </div>
          
          <el-menu
            mode="horizontal"
            :default-active="activeIndex"
            @select="handleSelect"
            :router="true"
            class="custom-menu"
          >
            <el-menu-item index="/chat" class="menu-item">
              <el-icon><ChatDotRound /></el-icon>
              <span>聊天</span>
            </el-menu-item>
            <el-menu-item index="/history" class="menu-item">
              <el-icon><Clock /></el-icon>
              <span>历史记录</span>
            </el-menu-item>
            <el-menu-item index="/knowledge" class="menu-item">
              <el-icon><Collection /></el-icon>
              <span>知识库</span>
            </el-menu-item>
          </el-menu>
          
          <div class="user-section">
            <div class="user-info">
              <el-icon class="user-avatar"><User /></el-icon>
              <span class="username">{{ username }}</span>
            </div>
            <el-button 
              @click="logout" 
              class="logout-button"
              type="primary"
              :icon="SwitchButton"
            >
              退出
            </el-button>
          </div>
        </div>
      </el-header>
      
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
    
    <div v-else class="login-wrapper">
      <router-view />
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ChatDotRound, Clock, Collection, User, SwitchButton } from '@element-plus/icons-vue'

export default {
  name: 'App',
  setup() {
    const route = useRoute()
    const router = useRouter()
    
    const activeIndex = ref('/chat')
    // 使用响应式数据而不是computed读取localStorage
    const userInfo = ref(null)
    
    // 计算属性基于响应式数据
    const loggedIn = computed(() => !!userInfo.value)
    const username = computed(() => userInfo.value?.username || '')
    const isAdmin = computed(() => userInfo.value?.isAdmin || false)
    
    // 检查登录状态
    const checkLoginStatus = () => {
      const user = localStorage.getItem('user')
      userInfo.value = user ? JSON.parse(user) : null
    }
    
    const handleSelect = (key) => {
      if (key !== 'logout' && key !== 'user') {
        activeIndex.value = key
      }
    }
    
    const logout = () => {
      localStorage.removeItem('user')
      userInfo.value = null // 立即更新响应式数据
      router.push('/login')
    }
    
    // 监听路由变化
    watch(() => route.path, (newPath) => {
      activeIndex.value = newPath
      
      // 如果导航到登录或注册页面，清除登录状态
      if (newPath === '/login' || newPath === '/register') {
        localStorage.removeItem('user')
        userInfo.value = null
      } else {
        // 其他页面检查登录状态
        checkLoginStatus()
      }
    })
    
    // 监听storage事件（其他标签页的localStorage变化）
    const handleStorageChange = (e) => {
      if (e.key === 'user') {
        checkLoginStatus()
      }
    }
    
    onMounted(() => {
      activeIndex.value = route.path
      checkLoginStatus()
      // 监听storage事件
      window.addEventListener('storage', handleStorageChange)
    })
    
    // 组件销毁时清理事件监听
    onUnmounted(() => {
      window.removeEventListener('storage', handleStorageChange)
    })
    
    return {
      activeIndex,
      loggedIn,
      username,
      isAdmin,
      handleSelect,
      logout,
      ChatDotRound,
      Clock,
      Collection,
      User,
      SwitchButton
    }
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;
}

#app {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB',
    'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  height: 100vh;
  position: relative;
  margin: 0;
  padding: 0;
  width: 100%;
}

/* 背景装饰元素 */
.background-decoration {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: -1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
}

.floating-shape {
  position: absolute;
  border-radius: 50%;
  opacity: 0.06;
  animation: float 10s ease-in-out infinite;
}

.shape-1 {
  width: 120px;
  height: 120px;
  background: linear-gradient(45deg, #fff, #f0f0f0);
  top: 10%;
  left: 5%;
  animation-delay: 0s;
}

.shape-2 {
  width: 160px;
  height: 160px;
  background: linear-gradient(45deg, #fff, #e0e0e0);
  top: 60%;
  right: 8%;
  animation-delay: -4s;
}

.shape-3 {
  width: 90px;
  height: 90px;
  background: linear-gradient(45deg, #fff, #f5f5f5);
  bottom: 20%;
  left: 12%;
  animation-delay: -7s;
}

.shape-4 {
  width: 140px;
  height: 140px;
  background: linear-gradient(45deg, #fff, #ebebeb);
  top: 5%;
  right: 20%;
  animation-delay: -2s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-30px) rotate(180deg); }
}

.el-container {
  position: relative;
  z-index: 10;
  height: 100vh;
  background: transparent;
  margin: 0 !important;
  padding: 0 !important;
  width: 100% !important;
}

.el-header {
  padding: 0 !important;
  margin: 0 !important;
  width: 100% !important;
}

.app-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(102, 126, 234, 0.1);
  padding: 0;
  margin: 0;
  height: 70px !important;
  line-height: 70px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 1000;
  width: 100%;
}

.header-content {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 0 24px;
}

.logo-section {
  display: flex;
  align-items: center;
  margin-right: 40px;
  min-width: 180px;
}

.logo-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 50%;
  margin-right: 12px;
}

.logo-icon svg {
  width: 20px;
  height: 20px;
  color: white;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.custom-menu {
  flex: 1;
  border-bottom: none !important;
  background: transparent !important;
}

.custom-menu .el-menu-item {
  color: #606266;
  font-weight: 500;
  font-size: 15px;
  margin: 0 8px;
  border-radius: 8px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  border-bottom: none !important;
}

.custom-menu .el-menu-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(102, 126, 234, 0.1), transparent);
  transition: left 0.5s ease;
}

.custom-menu .el-menu-item:hover::before {
  left: 100%;
}

.custom-menu .el-menu-item:hover {
  background: rgba(102, 126, 234, 0.08);
  color: #667eea;
  transform: translateY(-2px);
}

.custom-menu .el-menu-item.is-active {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
  color: #667eea;
  font-weight: 600;
}

.custom-menu .el-menu-item .el-icon {
  margin-right: 6px;
  font-size: 16px;
}

.user-section {
  display: flex;
  align-items: center;
  gap: 16px;
  min-width: 200px;
  justify-content: flex-end;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(102, 126, 234, 0.05);
  border-radius: 20px;
  border: 1px solid rgba(102, 126, 234, 0.1);
}

.user-avatar {
  width: 24px;
  height: 24px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 50%;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #2c3e50;
}

.logout-button {
  background: linear-gradient(135deg, #667eea, #764ba2) !important;
  border: none !important;
  border-radius: 20px;
  font-weight: 500;
  padding: 8px 16px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.logout-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s ease;
}

.logout-button:hover::before {
  left: 100%;
}

.logout-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4) !important;
}

.logout-button:active {
  transform: translateY(0);
}

.app-main {
  background: transparent;
  padding: 0;
  position: relative;
  z-index: 1;
}

.login-wrapper {
  position: relative;
  z-index: 1;
  width: 100%;
  height: 100vh;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    padding: 0 16px;
  }
  
  .logo-section {
    margin-right: 16px;
    min-width: auto;
  }
  
  .logo-text {
    display: none;
  }
  
  .custom-menu .el-menu-item {
    margin: 0 4px;
    font-size: 14px;
    padding: 0 12px;
  }
  
  .custom-menu .el-menu-item span {
    display: none;
  }
  
  .user-section {
    min-width: auto;
    gap: 8px;
  }
  
  .user-info {
    padding: 6px 12px;
  }
  
  .username {
    display: none;
  }
  
  .logout-button {
    padding: 6px 12px;
  }
  
  .logout-button span {
    display: none;
  }
  
  .floating-shape {
    display: none;
  }
}

@media (max-width: 480px) {
  .app-header {
    height: 60px !important;
    line-height: 60px;
  }
  
  .header-content {
    padding: 0 12px;
  }
  
  .logo-section {
    margin-right: 8px;
  }
  
  .custom-menu .el-menu-item {
    margin: 0 2px;
    padding: 0 8px;
  }
  
  .user-section {
    gap: 4px;
  }
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .app-header {
    background: rgba(30, 30, 30, 0.95);
  }
  
  .user-info {
    background: rgba(102, 126, 234, 0.1);
    border-color: rgba(102, 126, 234, 0.2);
  }
  
  .username {
    color: #e0e0e0;
  }
  
  .custom-menu .el-menu-item {
    color: #d0d0d0;
  }
  
  .custom-menu .el-menu-item:hover {
    background: rgba(102, 126, 234, 0.15);
    color: #8bb5ff;
  }
  
  .custom-menu .el-menu-item.is-active {
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.2), rgba(118, 75, 162, 0.2));
    color: #8bb5ff;
  }
}
</style>