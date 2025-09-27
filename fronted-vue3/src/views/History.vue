<template>
  <div class="history-container">
    <div class="background-decoration">
      <div class="floating-shape shape-1"></div>
      <div class="floating-shape shape-2"></div>
      <div class="floating-shape shape-3"></div>
      <div class="floating-shape shape-4"></div>
    </div>
    
    <el-card class="history-card">
      <template #header>
        <div class="header">
          <div class="logo-container">
            <div class="logo-icon">
              <el-icon><ChatLineRound /></el-icon>
            </div>
          </div>
          <h2 class="title">聊天记录</h2>
          <p class="subtitle">查看您的对话历史记录</p>
          <div class="refresh-container">
            <el-button 
              type="primary" 
              :icon="Refresh" 
              @click="fetchHistory" 
              circle 
              class="refresh-button"
              :loading="loading"
            />
          </div>
        </div>
      </template>
      
      <div class="table-container">
        <el-table
          v-loading="loading"
          :data="historyData"
          style="width: 100%"
          empty-text="暂无记录"
          stripe
          class="custom-table"
        >
          <!-- 序号列 -->
          <el-table-column 
            label="序号" 
            width="80" 
            align="center"
            type="index"
            :index="getTableIndex"
          />
          
          <el-table-column
            prop="question"
            label="问题"
            min-width="200"
            show-overflow-tooltip
          >
            <template #default="scope">
              <el-text class="question-text">{{ scope.row.question }}</el-text>
            </template>
          </el-table-column>
          
          <el-table-column
            prop="answer"
            label="回答"
            min-width="300"
            show-overflow-tooltip
          >
            <template #default="scope">
              <el-text 
                class="answer-text" 
                :title="scope.row.answer"
                truncated
              >
                {{ scope.row.answer }}
              </el-text>
            </template>
          </el-table-column>
          
          <el-table-column
            prop="createTime"
            label="时间"
            width="180"
            :formatter="formatDate"
          />
          
          <el-table-column
            fixed="right"
            label="操作"
            width="120"
          >
            <template #default="scope">
              <el-button 
                @click="viewDetail(scope.row)" 
                type="primary" 
                link
                :icon="View"
                class="action-button"
              >
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            class="custom-pagination"
          />
        </div>
      </div>
    </el-card>
      
    <!-- Detail Dialog -->
    <el-dialog
      v-model="dialogVisible"
      title="对话详情"
      width="60%"
      class="custom-dialog"
    >
      <div class="detail-content">
        <div class="detail-item">
          <h4>问题:</h4>
          <p>{{ currentDetail.question }}</p>
        </div>
        <div class="detail-item">
          <h4>回答:</h4>
          <p v-html="formatMessage(currentDetail.answer)"></p>
        </div>
        <div class="detail-item">
          <h4>时间:</h4>
          <p>{{ formatDate(null, null, currentDetail.createTime) }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, getCurrentInstance } from 'vue'
import { ElMessage } from 'element-plus'
import { View, Refresh, ChatLineRound } from '@element-plus/icons-vue'

export default {
  name: 'History',
  setup() {
    const { proxy } = getCurrentInstance()
    
    const historyData = ref([])
    const loading = ref(false)
    const dialogVisible = ref(false)
    const currentDetail = ref({})
    
    // 分页相关状态
    const currentPage = ref(1)
    const pageSize = ref(20)
    const total = ref(0)
    
    // 计算表格序号
    const getTableIndex = (index) => {
      return (currentPage.value - 1) * pageSize.value + index + 1
    }
    
    const fetchHistory = async () => {
      loading.value = true
      const user = JSON.parse(localStorage.getItem('user'))
      
      try {
        const response = await proxy.$axios.get(`/history/user/${user.id}`, {
          params: {
            page: currentPage.value,
            size: pageSize.value
          }
        })
        
        if (response.data.code === 200) {
          const result = response.data.data
          
          // 检查是否为分页结果
          if (result && typeof result === 'object' && result.data) {
            historyData.value = result.data || []
            total.value = result.total || 0
          } else {
            // 兼容旧版本API，直接返回数组
            historyData.value = result || []
            total.value = historyData.value.length
          }
        } else {
          ElMessage.error(response.data.message || 'Failed to fetch history')
        }
      } catch (error) {
        console.error('Failed to fetch history:', error)
        ElMessage.error('Failed to fetch history')
      } finally {
        loading.value = false
      }
    }
    
    const viewDetail = (row) => {
      currentDetail.value = row
      dialogVisible.value = true
    }
    
    const formatMessage = (message) => {
      return message ? message.replace(/\n/g, '<br>') : ''
    }
    
    const formatDate = (row, column, cellValue) => {
      if (!cellValue) return ''
      const date = new Date(cellValue)
      return date.toLocaleString()
    }
    
    // 分页处理
    const handleSizeChange = (newSize) => {
      pageSize.value = newSize
      currentPage.value = 1
      fetchHistory()
    }
    
    const handleCurrentChange = (newPage) => {
      currentPage.value = newPage
      fetchHistory()
    }
    
    onMounted(() => {
      fetchHistory()
    })
    
    return {
      historyData,
      loading,
      dialogVisible,
      currentDetail,
      currentPage,
      pageSize,
      total,
      getTableIndex,
      fetchHistory,
      viewDetail,
      formatMessage,
      formatDate,
      handleSizeChange,
      handleCurrentChange,
      View,
      Refresh,
      ChatLineRound
    }
  }
}
</script>

<style scoped>
.history-container {
  position: relative;
  width: 100%;
  min-height: calc(100vh - 80px);
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  overflow-x: hidden;
  box-sizing: border-box;
}

/* 背景装饰元素 */
.background-decoration {
  position: fixed;
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

.history-card {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 1200px;
  min-height: calc(100vh - 100px);
  border-radius: 20px;
  border: none;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.15), 
              0 10px 40px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(15px);
  background: rgba(255, 255, 255, 0.95);
  overflow: hidden;
  transition: all 0.3s ease;
  margin-top: 10px;
}

.history-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 35px 70px rgba(0, 0, 0, 0.2), 
              0 15px 50px rgba(0, 0, 0, 0.15);
}

.history-card :deep(.el-card__header) {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  padding: 20px 24px;
  border-bottom: none;
}

.history-card :deep(.el-card__body) {
  padding: 24px;
  background: rgba(255, 255, 255, 0.98);
}

.header {
  text-align: center;
  position: relative;
}

.logo-container {
  margin-bottom: 12px;
}

.logo-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 45px;
  height: 45px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  backdrop-filter: blur(10px);
  margin: 0 auto 12px;
  font-size: 24px;
}

.title {
  margin: 0 0 6px 0;
  font-size: 22px;
  font-weight: 600;
  color: white;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.subtitle {
  margin: 0 0 12px 0;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.85);
  font-weight: 300;
}

.refresh-container {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
}

.refresh-button {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  transition: all 0.3s ease;
}

.refresh-button:hover {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.5);
  transform: scale(1.1);
}

.table-container {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.custom-table {
  border-radius: 12px;
  overflow: hidden;
}

.custom-table :deep(.el-table__header) {
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
}

.custom-table :deep(.el-table__header th) {
  background: transparent;
  color: #2c3e50;
  font-weight: 600;
  border-bottom: 2px solid rgba(102, 126, 234, 0.1);
}

.custom-table :deep(.el-table__row) {
  transition: all 0.3s ease;
}

.custom-table :deep(.el-table__row:hover) {
  background: rgba(102, 126, 234, 0.05);
  transform: translateY(-1px);
}

.custom-table :deep(.el-table__row--striped) {
  background: rgba(248, 249, 250, 0.5);
}

.question-text {
  font-weight: 600;
  color: #2c3e50;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.answer-text {
  color: #606266;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  max-width: 100%;
}

.action-button {
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-weight: 600;
  transition: all 0.3s ease;
}

.action-button:hover {
  transform: translateY(-1px);
  text-shadow: 0 2px 4px rgba(102, 126, 234, 0.3);
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid rgba(102, 126, 234, 0.1);
}

.custom-pagination :deep(.el-pagination__jump) {
  color: #667eea;
}

.custom-pagination :deep(.el-pager li.is-active) {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border-radius: 8px;
}

.custom-pagination :deep(.btn-next),
.custom-pagination :deep(.btn-prev) {
  background: rgba(102, 126, 234, 0.1);
  border: 1px solid rgba(102, 126, 234, 0.2);
  border-radius: 8px;
  color: #667eea;
  transition: all 0.3s ease;
}

.custom-pagination :deep(.btn-next:hover),
.custom-pagination :deep(.btn-prev:hover) {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

.custom-dialog :deep(.el-dialog) {
  border-radius: 16px;
  overflow: hidden;
}

.custom-dialog :deep(.el-dialog__header) {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  text-align: center;
  margin: 0;
  padding: 20px;
}

.custom-dialog :deep(.el-dialog__title) {
  color: white;
  font-weight: 600;
}

.custom-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: white;
  font-size: 18px;
}

.custom-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.detail-content {
  padding: 24px;
  background: rgba(255, 255, 255, 0.98);
}

.detail-item {
  margin-bottom: 20px;
  padding: 20px;
  background: linear-gradient(145deg, #f8f9fa, #e9ecef);
  border-radius: 12px;
  border-left: 4px solid;
  border-image: linear-gradient(135deg, #667eea, #764ba2) 1;
  transition: all 0.3s ease;
}

.detail-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.15);
}

.detail-item h4 {
  margin: 0 0 12px 0;
  color: #2c3e50;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.detail-item p {
  margin: 0;
  line-height: 1.6;
  color: #606266;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .history-container {
    padding: 5px;
  }
  
  .history-card {
    margin-top: 5px;
    border-radius: 16px;
  }
  
  .history-card :deep(.el-card__header) {
    padding: 16px 20px;
  }
  
  .history-card :deep(.el-card__body) {
    padding: 16px;
  }
  
  .header {
    text-align: center;
  }
  
  .refresh-container {
    position: static;
    transform: none;
    margin-top: 12px;
  }
  
  .title {
    font-size: 20px;
  }
  
  .subtitle {
    font-size: 12px;
  }
  
  .table-container {
    padding: 16px;
    border-radius: 12px;
  }
  
  .floating-shape {
    display: none;
  }
  
  .custom-dialog {
    width: 90% !important;
  }
  
  .detail-item {
    padding: 16px;
  }
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .history-card {
    background: rgba(30, 30, 30, 0.95);
  }
  
  .history-card :deep(.el-card__body) {
    background: rgba(40, 40, 40, 0.98);
  }
  
  .table-container {
    background: rgba(50, 50, 50, 0.95);
  }
  
  .custom-table :deep(.el-table__header) {
    background: linear-gradient(135deg, #2c2c2c, #3a3a3a);
  }
  
  .custom-table :deep(.el-table__header th) {
    color: #e0e0e0;
  }
  
  .custom-table :deep(.el-table__row--striped) {
    background: rgba(60, 60, 60, 0.5);
  }
  
  .detail-content {
    background: rgba(40, 40, 40, 0.98);
  }
  
  .detail-item {
    background: linear-gradient(145deg, #2c2c2c, #3a3a3a);
    color: #e0e0e0;
  }
  
  .detail-item h4 {
    color: #e0e0e0;
  }
  
  .detail-item p {
    color: #d0d0d0;
  }
}
</style>