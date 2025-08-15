<template>
  <div class="app-container">
    <!-- 初始状态的搜索区域（包含logo和搜索框） -->
    <div class="initial-search-area" :class="{ 'searched': hasSearched }">
      <!-- Logo（搜索后隐藏） -->
      <div class="logo-container" v-if="!hasSearched">
        <img
            src="@/assets/logo/qs.png"
            alt="QikSearch Logo"
            class="logo-img"
        >
      </div>

      <!-- 搜索框（搜索后上移） -->
      <div class="search-container">
        <div class="search-group">
          <el-input
              class="search-input"
              v-model="queryParams.keyword"
              clearable
              @keyup.enter="handleSearch"
              size="large"
              placeholder="请输入搜索关键词..."
          ></el-input>
          <el-button
              type="primary"
              @click="handleSearch"
              size="large"
              class="search-btn"
          >搜索</el-button>
        </div>
      </div>
    </div>

    <!-- 搜索结果区域 -->
    <div class="results-wrapper" v-if="hasSearched">
      <!-- 搜索统计信息 -->
      <div class="search-stats" v-if="total > 0 && !loading">
        <span>找到约 {{ total }} 条结果（用时 {{ responseTime }} 秒）</span>
      </div>

      <!-- 结果展示区域 -->
      <div class="results-container" v-if="total > 0 && !loading">
        <div class="result-card" v-for="(item, index) in tableData" :key="index">

          <div class="result-title">
            <a href="javascript:void(0)" @click="handleResultClick(item)">{{ item.title }}</a>
          </div>

          <div class="result-snippet" v-html="item.highlightContent"></div>

          <!-- 结果预览信息 -->
          <div class="result-actions" v-if="item.htmlPath">
            <a href="javascript:void(0)" @click="handleResultClick(item)" class="preview-link">查看详情</a>
          </div>
        </div>
      </div>

      <!-- 分页控件 -->
      <div class="pagination-container" v-if="total > 0 && !loading">
        <pagination
            :total="total"
            v-model:page="queryParams.pageNum"
            v-model:limit="queryParams.pageSize"
            @pagination="handleSearch"
        />
      </div>

      <!-- 无结果提示 -->
      <el-empty
          v-if="hasSearched && total === 0 && !loading"
          description="没有找到匹配的内容"
      ></el-empty>

      <!-- 结果不满意跳转其他搜索引擎 -->
      <div class="search-other" v-if="hasSearched && !loading">
        <span>结果不满意？</span>
        <el-button
            type="text"
            @click="goToSearchEngine('baidu')"
            class="engine-btn"
        >
          <i class="el-icon-search"></i> 去百度搜索
        </el-button>
        <el-button
            type="text"
            @click="goToSearchEngine('bing')"
            class="engine-btn"
        >
          <i class="el-icon-search"></i> 去必应搜索
        </el-button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div class="loading-container" v-if="loading">
      <el-loading :text="'搜索中，请稍候...'" fullscreen></el-loading>
    </div>
  </div>
</template>

<script>
import { getList } from "@/api/qiksearch/search";

export default {
  name: "Search",
  data() {
    return {
      queryParams: {
        keyword: "",
        pageNum: 1,
        pageSize: 5,
      },
      tableData: [],
      total: 0,
      loading: false,
      responseTime: 0,
      hasSearched: false,
      lastKeyword: "" // 记录上一次的搜索关键词
    };
  },
  methods: {
    handleSearch() {
      this.hasSearched = true;
      const currentKeyword = this.queryParams.keyword.trim();

      // 判断是否为新关键词（与上一次不同）
      if (currentKeyword !== this.lastKeyword) {
        // 新关键词搜索：重置为第1页，并更新记录的关键词
        this.queryParams.pageNum = 1;
        this.lastKeyword = currentKeyword;
      }

      if (!currentKeyword) {
        this.total = 0;
        this.tableData = [];
        return;
      }

      this.loading = true;
      const startTime = Date.now();

      getList(this.queryParams).then(response => {
        this.responseTime = ((Date.now() - startTime) / 1000).toFixed(2);
        this.tableData = response.rows;
        this.total = response.total;
        this.loading = false;
      }).catch(() => {
        this.loading = false;
        this.$message.error('搜索失败，请稍后重试');
      });
    },
    handleResultClick(item) {
      if (item.htmlPath) {
        window.open(item.htmlPath, '_blank');
      } else {
        console.warn('该结果没有对应的HTML路径', item);
        this.$message.warning('未找到对应的内容页面');
      }
    },
    // 跳转到其他搜索引擎
    goToSearchEngine(engine) {
      const keyword = this.queryParams.keyword.trim();
      if (!keyword) {
        this.$message.warning('请输入搜索关键词');
        return;
      }

      // 编码关键词（处理中文等特殊字符）
      const encodedKeyword = encodeURIComponent(keyword);
      let url = '';

      // 生成对应搜索引擎的搜索URL
      switch(engine) {
        case 'baidu':
          url = `https://www.baidu.com/s?wd=${encodedKeyword}`;
          break;
        case 'bing':
          url = `https://www.bing.com/search?q=${encodedKeyword}`;
          break;
        default:
          return;
      }

      // 打开新窗口跳转
      window.open(url, '_blank');
    },
    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`;
    }
  }
};
</script>

<style scoped>
/* 基础样式 - 网页端保持原样 */
.app-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 40px;
}

.initial-search-area {
  transition: all 0.5s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  min-height: 380px;
  padding: 20px 0;
  margin-top: 200px;
}

.initial-search-area.searched {
  min-height: auto;
  padding: 20px 0 10px;
  margin-top: 20px;
}

.logo-container {
  margin-bottom: 30px;
  transition: all 0.3s ease;
}

.logo-img {
  height: 100px; /* 网页端logo尺寸 */
  width: auto;
  object-fit: contain;
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.1));
}

.search-container {
  width: 100%;
  text-align: center;
}

/* 搜索框与按钮组合 - 网页端样式 */
.search-group {
  display: inline-flex;
  align-items: stretch;
  width: auto;
}

.search-input {
  width: 650px;
  height: 54px;
  font-size: 17px;
  border-right: none;
  border-radius: 4px 0 0 4px;
}

.search-btn {
  height: 54px;
  padding: 0 28px;
  font-size: 17px;
  border-radius: 0 4px 4px 0;
  margin-left: -1px; /* 消除间隙 */
}

/* 结果区域样式 */
.results-wrapper {
  margin-top: 20px;
}

.search-stats {
  color: #666;
  font-size: 14px;
  margin-bottom: 20px;
  padding-left: 8px;
}

.results-container {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.result-card {
  padding: 20px;
  border-radius: 8px;
  transition: all 0.3s ease;
  border: 1px solid transparent;
}

.result-card:hover {
  background-color: #f8f9fa;
  border-color: #e9ecef;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.result-title a {
  color: #2440b3;
  font-size: 20px;
  text-decoration: none;
  line-height: 1.4;
  font-weight: 500;
  transition: all 0.25s ease;
  letter-spacing: 0.2px;
}

.result-title a:hover {
  color: #0d6efd;
  text-decoration: underline;
  text-underline-offset: 4px;
  text-decoration-thickness: 2px;
  text-decoration-color: rgba(36, 64, 179, 0.6);
}

.result-snippet {
  color: #343a40;
  font-size: 15px;
  line-height: 1.6;
  word-wrap: break-word;
  margin-bottom: 10px;
}

:deep(em) {
  font-style: normal;
  color: #d93025;
  font-weight: 600;
  background-color: rgba(255, 234, 234, 0.3);
  border-radius: 2px;
}

.result-actions {
  padding-top: 5px;
}

.preview-link {
  color: #6c757d;
  font-size: 14px;
  text-decoration: none;
  transition: color 0.2s;
}

.preview-link:hover {
  color: #0d6efd;
  text-decoration: underline;
}

.pagination-container {
  margin: 40px 0;
  text-align: center;
}

.loading-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(255, 255, 255, 0.7);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
}

.search-other {
  margin: 30px 0;
  padding: 15px;
  border-top: 1px solid #eee;
  display: flex;
  align-items: center;
  gap: 15px;
  color: #666;
  font-size: 14px;
}

.engine-btn {
  color: #1a73e8;
  padding: 0 5px;
}

.engine-btn:hover {
  color: #174ea6;
  text-decoration: underline;
}

/* 移动端适配 - 仅修改移动端样式 */
@media (max-width: 768px) {
  .app-container {
    padding: 0 10px; /* 最小化内边距，最大化可用空间 */
  }

  /* 移动端logo缩小 */
  .logo-img {
    height: 60px; /* 相比网页端缩小40% */
  }

  /* 搜索框占满屏幕宽度 */
  .search-group {
    width: 100%; /* 搜索组占满容器宽度 */
  }

  .search-input {
    width: 100% !important; /* 强制与屏幕等宽 */
    border-right: 1px solid #dcdfe6; /* 恢复完整边框 */
    border-radius: 4px; /* 恢复四角圆角 */
  }

  /* 移动端隐藏搜索按钮 */
  .search-btn {
    display: none;
  }

  /* 调整初始区域位置，适应小屏幕 */
  .initial-search-area {
    margin-top: 80px;
    min-height: 250px;
  }

  .search-other {
    flex-wrap: wrap;
  }

  .result-title a {
    font-size: 18px;
  }
}
</style>
