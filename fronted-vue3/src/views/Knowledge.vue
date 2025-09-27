<template>
  <div class="knowledge-container">
    <div class="background-decoration">
      <div class="floating-shape shape-1"></div>
      <div class="floating-shape shape-2"></div>
      <div class="floating-shape shape-3"></div>
      <div class="floating-shape shape-4"></div>
    </div>
    
    <el-card class="knowledge-card">
      <template #header>
        <div class="header">
          <div class="logo-container">
            <div class="logo-icon">
              <el-icon><Collection /></el-icon>
            </div>
          </div>
          <h2 class="title">知识库管理</h2>
          <p class="subtitle">管理和维护系统知识库内容</p>
          <div class="header-actions" v-if="isAdmin">
            <el-button 
              @click="addKnowledge" 
              type="primary" 
              :icon="Plus"
              class="add-button"
            >
              新增问答
            </el-button>
          </div>
        </div>
      </template>

      <div class="content-area">
        <!-- 搜索区域 -->
        <div class="search-area">
          <div class="search-controls">
            <el-input
              v-model="searchQuery"
              placeholder="搜索问题或答案..."
              style="width: 300px"
              clearable
              @input="handleSearch"
              @clear="loadKnowledgeList"
              class="custom-search-input"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button 
              v-if="searchQuery" 
              @click="performSearch" 
              type="primary" 
              style="margin-left: 10px"
              class="search-button"
            >
              搜索
            </el-button>
          </div>
          
          <!-- 权限提示 -->
          <div class="permission-info">
            <el-tag :type="isAdmin ? 'success' : 'info'" size="small" class="permission-tag">
              {{ isAdmin ? '管理员权限' : '只读权限' }}
            </el-tag>
            <el-text size="small" type="info" style="margin-left: 8px" class="permission-text">
              {{ isAdmin ? '可以新增、编辑、删除知识条目' : '仅可查看知识条目' }}
            </el-text>
          </div>
        </div>

        <!-- 知识库表格 -->
        <div class="table-container">
          <el-table
            :data="knowledgeList"
            v-loading="loading"
            stripe
            style="width: 100%"
            empty-text="暂无数据"
            element-loading-text="加载中..."
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
            
            <el-table-column prop="question" label="问题" min-width="200">
              <template #default="scope">
                <el-text class="question-text">{{ scope.row.question || scope.row.title }}</el-text>
              </template>
            </el-table-column>
            <el-table-column prop="answer" label="答案" min-width="300">
              <template #default="scope">
                <el-text 
                  class="answer-text" 
                  :title="scope.row.answer || scope.row.content"
                  truncated
                >
                  {{ scope.row.answer || scope.row.content }}
                </el-text>
              </template>
            </el-table-column>
            
            <el-table-column prop="createTime" label="创建时间" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="updateTime" label="更新时间" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.updateTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="160" fixed="right" v-if="isAdmin">
              <template #default="scope">
                <el-button
                  type="primary"
                  size="small"
                  @click="editKnowledge(scope.row)"
                  :icon="Edit"
                  link
                  class="action-button"
                >
                  编辑
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  @click="deleteKnowledge(scope.row)"
                  :icon="Delete"
                  link
                  class="action-button delete-button"
                >
                  删除
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
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialogMode === 'add' ? '新增问答' : '编辑问答'"
      v-model="showAddDialog"
      width="600px"
      :close-on-click-modal="false"
      class="custom-dialog"
    >
      <el-form
        :model="currentKnowledge"
        :rules="formRules"
        ref="formRef"
        label-width="80px"
        class="custom-form"
      >
        <el-form-item label="问题" prop="question">
          <el-input
            v-model="currentKnowledge.question"
            type="textarea"
            :rows="3"
            placeholder="请输入问题"
            maxlength="500"
            show-word-limit
            class="custom-textarea"
          />
        </el-form-item>
        <el-form-item label="答案" prop="answer">
          <el-input
            v-model="currentKnowledge.answer"
            type="textarea"
            :rows="5"
            placeholder="请输入答案"
            maxlength="2000"
            show-word-limit
            class="custom-textarea"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelDialog" class="cancel-button">取消</el-button>
          <el-button 
            type="primary" 
            @click="saveKnowledge"
            :loading="saving"
            class="save-button"
          >
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, getCurrentInstance } from 'vue'
import { ElMessage, ElMessageBox, ElTag, ElText } from 'element-plus'
import { 
  Collection, 
  Plus, 
  Search, 
  Edit, 
  Delete 
} from '@element-plus/icons-vue'

export default {
  name: 'KnowledgeBase',
  setup() {
    const { proxy } = getCurrentInstance()
    
    const loading = ref(false)
    const saving = ref(false)
    const knowledgeList = ref([])
    const searchQuery = ref('')
    const showAddDialog = ref(false)
    const dialogMode = ref('add') // 'add' or 'edit'
    const currentPage = ref(1)
    const pageSize = ref(20)
    const total = ref(0)
    const isSearchMode = ref(false)
    
    // 获取当前用户信息和权限
    const currentUser = ref(null)
    const isAdmin = ref(false)
    
    // 初始化用户权限
    const initUserPermissions = () => {
      const userStr = localStorage.getItem('user')
      if (userStr) {
        currentUser.value = JSON.parse(userStr)
        // 检查是否为管理员 (is_admin 字段为 1 或者 isAdmin 字段为 true)
        isAdmin.value = currentUser.value.is_admin === 1 || currentUser.value.isAdmin === true || currentUser.value.is_admin === true
      }
    }
    
    const currentKnowledge = ref({
      id: null,
      question: '',
      answer: '',
      category: ''
    })
    
    const formRef = ref()
    const formRules = {
      question: [
        { required: true, message: '请输入问题', trigger: 'blur' },
        { min: 5, max: 500, message: '问题长度在 5 到 500 个字符', trigger: 'blur' }
      ],
      answer: [
        { required: true, message: '请输入答案', trigger: 'blur' },
        { min: 5, max: 2000, message: '答案长度在 5 到 2000 个字符', trigger: 'blur' }
      ]
    }
    
    // 计算表格序号
    const getTableIndex = (index) => {
      return (currentPage.value - 1) * pageSize.value + index + 1
    }
    
    // 加载知识库列表
    const loadKnowledgeList = async () => {
      loading.value = true
      try {
        const response = await proxy.$axios.get('/knowledge/all', {
          params: {
            page: currentPage.value,
            size: pageSize.value
          }
        })
        
        if (response.data.code === 200) {
          // 后端返回分页结果
          const result = response.data.data
          if (result && result.data) {
            knowledgeList.value = result.data
            total.value = result.total || 0
          } else {
            // 兼容旧版本API，直接返回数组
            knowledgeList.value = response.data.data || []
            total.value = knowledgeList.value.length
          }
        } else {
          ElMessage.error(response.data.message || '加载失败')
        }
      } catch (error) {
        console.error('加载知识库列表失败:', error)
        ElMessage.error('加载知识库失败')
      } finally {
        loading.value = false
        isSearchMode.value = false
      }
    }
    
    // 搜索处理
    const handleSearch = () => {
      // 实时搜索逻辑可以在这里实现，现在先不做处理
    }
    
    // 执行搜索
    const performSearch = async () => {
      if (!searchQuery.value.trim()) {
        loadKnowledgeList()
        return
      }
      
      loading.value = true
      try {
        const response = await proxy.$axios.get('/knowledge/search', {
          params: { query: searchQuery.value.trim() }
        })
        
        if (response.data.code === 200) {
          knowledgeList.value = response.data.data || []
          total.value = knowledgeList.value.length
          isSearchMode.value = true
          currentPage.value = 1 // 搜索时重置到第一页
        } else {
          ElMessage.error(response.data.message || '搜索失败')
        }
      } catch (error) {
        console.error('搜索知识库失败:', error)
        ElMessage.error('搜索失败')
      } finally {
        loading.value = false
      }
    }
    
    // 新增知识条目
    const addKnowledge = () => {
      dialogMode.value = 'add'
      currentKnowledge.value = {
        id: null,
        question: '',
        answer: '',
        category: ''
      }
      showAddDialog.value = true
    }
    
    // 编辑知识条目
    const editKnowledge = (row) => {
      dialogMode.value = 'edit'
      currentKnowledge.value = {
        id: row.id,
        question: row.question || row.title || '',
        answer: row.answer || row.content || '',
        category: row.category || ''
      }
      showAddDialog.value = true
    }
    
    // 删除知识条目
    const deleteKnowledge = async (row) => {
      const question = row.question || row.title || `ID:${row.id}`
      try {
        await ElMessageBox.confirm(
          `确定要删除问题"${question}"吗？`,
          '确认删除',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        
        const response = await proxy.$axios.delete(`/knowledge/${row.id}`)
        if (response.data.code === 200) {
          ElMessage.success('删除成功')
          if (isSearchMode.value && searchQuery.value) {
            performSearch() // 如果在搜索模式，重新搜索
          } else {
            loadKnowledgeList() // 否则重新加载列表
          }
        } else {
          ElMessage.error(response.data.message || '删除失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除知识库条目失败:', error)
          ElMessage.error('删除失败')
        }
      }
    }
    
    // 保存知识条目
    const saveKnowledge = async () => {
      if (!formRef.value) return
      
      try {
        await formRef.value.validate()
        
        saving.value = true
        const isEdit = dialogMode.value === 'edit'
        
        // 构建请求数据 - 适配后端字段
        const data = {
          question: currentKnowledge.value.question,
          answer: currentKnowledge.value.answer,
          category: currentKnowledge.value.category
        }
        
        let response
        if (isEdit) {
          response = await proxy.$axios.put(`/knowledge/${currentKnowledge.value.id}`, data)
        } else {
          response = await proxy.$axios.post('/knowledge', data)
        }
        
        if (response.data.code === 200) {
          ElMessage.success(isEdit ? '更新成功' : '添加成功')
          showAddDialog.value = false
          if (isSearchMode.value && searchQuery.value) {
            performSearch()
          } else {
            loadKnowledgeList()
          }
        } else {
          ElMessage.error(response.data.message || '保存失败')
        }
      } catch (error) {
        console.error('保存知识库条目失败:', error)
        if (error.message) {
          ElMessage.error('保存失败')
        }
      } finally {
        saving.value = false
      }
    }
    
    // 取消对话框
    const cancelDialog = () => {
      showAddDialog.value = false
      if (formRef.value) {
        formRef.value.resetFields()
      }
    }
    
    // 分页处理
    const handleSizeChange = (newSize) => {
      pageSize.value = newSize
      currentPage.value = 1
      if (isSearchMode.value && searchQuery.value) {
        performSearch()
      } else {
        loadKnowledgeList()
      }
    }
    
    const handleCurrentChange = (newPage) => {
      currentPage.value = newPage
      if (isSearchMode.value && searchQuery.value) {
        performSearch()
      } else {
        loadKnowledgeList()
      }
    }
    
    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return '-'
      return new Date(dateString).toLocaleString('zh-CN')
    }
    
    onMounted(() => {
      initUserPermissions()
      loadKnowledgeList()
    })
    
    return {
      loading,
      saving,
      knowledgeList,
      searchQuery,
      showAddDialog,
      dialogMode,
      currentPage,
      pageSize,
      total,
      currentKnowledge,
      formRef,
      formRules,
      currentUser,
      isAdmin,
      getTableIndex,
      loadKnowledgeList,
      handleSearch,
      performSearch,
      addKnowledge,
      editKnowledge,
      deleteKnowledge,
      saveKnowledge,
      cancelDialog,
      handleSizeChange,
      handleCurrentChange,
      formatDate,
      Collection,
      Plus,
      Search,
      Edit,
      Delete
    }
  }
}
</script>

<style scoped>
.knowledge-container {
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

.knowledge-card {
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

.knowledge-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 35px 70px rgba(0, 0, 0, 0.2), 
              0 15px 50px rgba(0, 0, 0, 0.15);
}

.knowledge-card :deep(.el-card__header) {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  padding: 20px 24px;
  border-bottom: none;
}

.knowledge-card :deep(.el-card__body) {
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

.header-actions {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
}

.add-button {
  background: rgba(255, 255, 255, 0.2) !important;
  border: 1px solid rgba(255, 255, 255, 0.3) !important;
  color: white !important;
  transition: all 0.3s ease;
}

.add-button:hover {
  background: rgba(255, 255, 255, 0.3) !important;
  border-color: rgba(255, 255, 255, 0.5) !important;
  transform: scale(1.05);
}

.content-area {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.search-area {
  margin-bottom: 24px;
}

.search-controls {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.custom-search-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  border: 2px solid rgba(102, 126, 234, 0.2);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.1);
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.95);
}

.custom-search-input :deep(.el-input__wrapper:hover) {
  border-color: #667eea;
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.2);
}

.custom-search-input :deep(.el-input__wrapper.is-focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.search-button {
  background: linear-gradient(135deg, #667eea, #764ba2) !important;
  border: none !important;
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.search-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4) !important;
}

.permission-info {
  display: flex;
  align-items: center;
}

.permission-tag {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(102, 126, 234, 0.3);
  color: #667eea;
  font-weight: 600;
}

.permission-text {
  color: #606266;
  font-weight: 500;
}

.table-container {
  background: rgba(255, 255, 255, 0.98);
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
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

.delete-button {
  background: linear-gradient(135deg, #f56c6c, #e6a23c);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
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
  padding: 24px;
  background: rgba(255, 255, 255, 0.98);
}

.custom-form {
  margin-top: 10px;
}

.custom-textarea :deep(.el-textarea__inner) {
  border-radius: 8px;
  border: 2px solid rgba(102, 126, 234, 0.2);
  transition: all 0.3s ease;
}

.custom-textarea :deep(.el-textarea__inner:hover) {
  border-color: #667eea;
}

.custom-textarea :deep(.el-textarea__inner:focus) {
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.dialog-footer {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.cancel-button {
  border: 1px solid rgba(102, 126, 234, 0.3);
  color: #667eea;
  transition: all 0.3s ease;
}

.cancel-button:hover {
  background: rgba(102, 126, 234, 0.1);
  border-color: #667eea;
}

.save-button {
  background: linear-gradient(135deg, #667eea, #764ba2) !important;
  border: none !important;
  font-weight: 600;
  transition: all 0.3s ease;
}

.save-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4) !important;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .knowledge-container {
    padding: 5px;
  }
  
  .knowledge-card {
    margin-top: 5px;
    border-radius: 16px;
  }
  
  .knowledge-card :deep(.el-card__header) {
    padding: 16px 20px;
  }
  
  .knowledge-card :deep(.el-card__body) {
    padding: 16px;
  }
  
  .header {
    text-align: center;
  }
  
  .header-actions {
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
  
  .content-area {
    padding: 16px;
    border-radius: 12px;
  }
  
  .search-controls {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .custom-search-input {
    width: 100% !important;
  }
  
  .permission-info {
    margin-top: 10px;
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }
  
  .table-container {
    padding: 12px;
  }
  
  .floating-shape {
    display: none;
  }
  
  .custom-dialog {
    width: 90% !important;
  }
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .knowledge-card {
    background: rgba(30, 30, 30, 0.95);
  }
  
  .knowledge-card :deep(.el-card__body) {
    background: rgba(40, 40, 40, 0.98);
  }
  
  .content-area {
    background: rgba(50, 50, 50, 0.95);
  }
  
  .table-container {
    background: rgba(60, 60, 60, 0.98);
  }
  
  .custom-table :deep(.el-table__header) {
    background: linear-gradient(135deg, #2c2c2c, #3a3a3a);
  }
  
  .custom-table :deep(.el-table__header th) {
    color: #e0e0e0;
  }
  
  .custom-table :deep(.el-table__row--striped) {
    background: rgba(70, 70, 70, 0.5);
  }
  
  .permission-text {
    color: #d0d0d0;
  }
  
  .custom-dialog :deep(.el-dialog__body) {
    background: rgba(40, 40, 40, 0.98);
  }
  
  .custom-textarea :deep(.el-textarea__inner) {
    background: rgba(60, 60, 60, 0.8);
    color: #e0e0e0;
  }
}
</style>