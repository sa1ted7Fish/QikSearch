<template>
  <div class="app-container">
    <!-- 初始状态的搜索区域（样式不变） -->
    <div class="initial-search-area" :class="{ 'searched': hasSearched }">
      <!-- Logo（搜索后隐藏） -->
      <div class="logo-container" v-if="!hasSearched">
        <img
            src="@/assets/logo/qs.png"
            alt="QikSearch Logo"
            class="logo-img"
        >
      </div>

      <!-- 搜索框（搜索后上移，样式不变） -->
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

    <!-- 搜索结果区域（核心：通过容器结构适配移动端按钮位置） -->
    <div class="results-wrapper" v-if="hasSearched">
      <!-- 核心优化：自适应布局容器（网页端横向排列，移动端纵向排列且按钮在上） -->
      <div class="search-header-adapt" v-if="!loading">
        <!-- 移动端：先显示跳转按钮（在结果数上方） -->
        <div class="top-engine-entries mobile-first">
          <el-button
              type="default"
              @click="goToSearchEngine('baidu')"
              class="engine-btn baidu-btn"
              size="mini"
          >
            <i class="el-icon-search"></i> 去百度
          </el-button>
          <el-button
              type="default"
              @click="goToSearchEngine('bing')"
              class="engine-btn bing-btn"
              size="mini"
          >
            <i class="el-icon-search"></i> 去必应
          </el-button>
        </div>

        <!-- 原有搜索统计信息 -->
        <div class="search-stats" v-if="total > 0">
          <span>找到约 {{ total }} 条结果（用时 {{ responseTime }} 秒）</span>
        </div>

        <!-- 网页端：显示跳转按钮（在结果数右侧，与原位置一致） -->
        <div class="top-engine-entries desktop-only">
          <el-button
              type="default"
              @click="goToSearchEngine('baidu')"
              class="engine-btn baidu-btn"
              size="mini"
          >
            <i class="el-icon-search"></i> 去百度
          </el-button>
          <el-button
              type="default"
              @click="goToSearchEngine('bing')"
              class="engine-btn bing-btn"
              size="mini"
          >
            <i class="el-icon-search"></i> 去必应
          </el-button>
        </div>
      </div>

      <!-- 结果展示区域（样式不变） -->
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

      <!-- 分页控件（样式不变） -->
      <div class="pagination-container" v-if="total > 0 && !loading">
        <pagination
            :total="total"
            v-model:page="queryParams.pageNum"
            v-model:limit="queryParams.pageSize"
            @pagination="handleSearch"
        />
      </div>

      <!-- 无结果提示（样式不变） -->
      <el-empty
          v-if="hasSearched && total === 0 && !loading"
          description="没有找到匹配的内容"
      ></el-empty>

      <!-- 保留原有：底部跳转按钮 -->
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

    <!-- 加载状态（样式不变） -->
    <div class="loading-container" v-if="loading">
      <el-loading :text="'搜索中，请稍候...'" fullscreen></el-loading>
    </div>

    <!-- 回到顶部组件 -->
    <el-backtop
        :right="20"
        :bottom="100"
        :visibility-height="100"
    />
  </div>
</template>

<script>
import { getList } from "@/api/qiksearch-dev/search";

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

      if (currentKeyword !== this.lastKeyword) {
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
    goToSearchEngine(engine) {
      const keyword = this.queryParams.keyword.trim();
      if (!keyword) {
        this.$message.warning('请输入搜索关键词');
        return;
      }

      const encodedKeyword = encodeURIComponent(keyword);
      let url = '';
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
      window.open(url, '_blank');
    },
  }
};
</script>

<style scoped>
/* 基础样式保持不变（确保未搜索时布局正常） */
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
  height: 100px;
  width: auto;
  object-fit: contain;
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.1));
}

.search-container {
  width: 100%;
  text-align: center;
}

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
  margin-left: -1px;
}

/* 核心优化：自适应布局容器（控制网页端/移动端的按钮位置） */
.search-header-adapt {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-left: 8px;
  line-height: 32px;
}

/* 网页端：仅显示右侧按钮，隐藏移动端顶部按钮 */
.desktop-only {
  display: flex;
  gap: 12px; /* 按钮间距12px，与原样式一致 */
}
.mobile-first {
  display: none; /* 网页端隐藏移动端专用按钮容器 */
}

/* 顶部按钮样式（复用原样式，确保视觉统一） */
.top-engine-entries .engine-btn.baidu-btn,
.top-engine-entries .engine-btn.bing-btn {
  padding: 6px 18px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  color: #333;
  transition: all 0.2s ease;
  cursor: pointer;
}

.engine-btn.baidu-btn {
  background-color: #f8f8f8;
  border-color: #e5e5e5;
}

.engine-btn.baidu-btn:hover {
  background-color: #f0f0f0;
  border-color: #d0d0d0;
  color: #000;
  text-decoration: none;
}

.engine-btn.bing-btn {
  background-color: #f0f7ff;
  border-color: #d1e0fe;
}

.engine-btn.bing-btn:hover {
  background-color: #e1efff;
  border-color: #b3d1ff;
  color: #0066cc;
  text-decoration: none;
}

/* 结果区域其他样式保持不变 */
.results-wrapper {
  margin-top: 20px;
}

.search-stats {
  color: #666;
  font-size: 14px;
  margin: 0;
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

/* 底部跳转按钮样式（保持原有） */
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

/* 移动端适配：核心控制按钮位置（在结果数上方） */
@media (max-width: 768px) {
  .app-container {
    padding: 0 10px;
  }

  .logo-img {
    height: 60px;
  }

  .search-group {
    width: 100%;
  }

  .search-input {
    width: 100% !important;
    border-right: 1px solid #dcdfe6;
    border-radius: 4px;
  }

  .search-btn {
    display: none;
  }

  .initial-search-area {
    margin-top: 80px;
    min-height: 250px;
  }

  /* 移动端：自适应容器改为纵向排列，按钮在结果数上方 */
  .search-header-adapt {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
    line-height: normal;
    margin-bottom: 15px;
  }

  /* 移动端：显示顶部按钮容器（在结果数上方），隐藏右侧按钮容器 */
  .mobile-first {
    display: flex;
    width: 100%;
    box-sizing: border-box;
    padding-right: 8px;
    gap: 10px;
  }
  .desktop-only {
    display: none; /* 移动端隐藏网页端右侧按钮 */
  }

  /* 移动端按钮：宽度自适应，占满可用空间，易点击 */
  .mobile-first .engine-btn.baidu-btn,
  .mobile-first .engine-btn.bing-btn {
    flex: 1;
    text-align: center;
    padding: 8px 0;
  }

  .search-stats {
    padding-left: 0;
    width: 100%;
  }

  .search-other {
    flex-wrap: wrap;
    justify-content: flex-start;
    gap: 10px;
  }

  .result-title a {
    font-size: 18px;
  }
}
</style>
