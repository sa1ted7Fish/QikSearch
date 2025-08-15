<template>
  <div class="app-container home">
    <!-- 系统标题区域 -->
    <header class="system-header">
      <div class="header-content">
        <h1 class="system-title">QikSearch</h1>
        <p class="system-slogan">让答题搜索更简单高效</p>
      </div>
    </header>

    <!-- 主要内容区域 -->
    <main class="content-wrapper">
      <!-- 系统简介 -->
      <section class="intro-section">
        <div class="intro-card">
          <p class="intro-text">
            QikSearch是一款专注于答题资源的智能搜索系统，整合海量题库资源，
            通过先进的检索技术和用户友好的界面，帮助你快速找到所需的答题内容。
            无论是学习备考还是知识拓展，都能为你提供精准、高效的搜索体验。
          </p>
        </div>
      </section>

      <!-- 核心功能 -->
      <section class="features-section">
        <h2 class="section-title">为什么选择QikSearch？</h2>
        <div class="features-grid">
          <div class="feature-item">
            <div class="feature-icon">
              <i class="search-icon"></i>
            </div>
            <h3 class="feature-title">精准匹配</h3>
            <p class="feature-desc">智能算法理解你的搜索意图，精准匹配最相关的答题资源，节省筛选时间</p>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <i class="speed-icon"></i>
            </div>
            <h3 class="feature-title">极速响应</h3>
            <p class="feature-desc">毫秒级搜索响应，海量题库瞬间呈现，无需等待</p>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <i class="update-icon"></i>
            </div>
            <h3 class="feature-title">实时更新</h3>
            <p class="feature-desc">题库资源每日更新，确保你获取的是最新、最优质的答题内容</p>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <i class="easy-icon"></i>
            </div>
            <h3 class="feature-title">简单易用</h3>
            <p class="feature-desc">简洁直观的操作界面，无需学习成本，上手即用</p>
          </div>
        </div>
      </section>

      <!-- 更新日志 -->
      <section class="changelog-section">
        <h2 class="section-title">更新日志</h2>
        <div class="changelog-timeline">
          <!-- 最新版本 -->
          <div class="changelog-item latest">
            <div class="version-badge">v1.2.0</div>
            <div class="changelog-content">
              <div class="update-date">2025-08-15</div>
              <h3 class="update-title">搜索体验优化</h3>
              <ul class="update-details">
                <li>新增搜索（测试版），搜索（测试版）过滤了相关性较低的结果，如果结果为空自动跳转到百度搜索</li>
              </ul>
            </div>
          </div>

          <div class="changelog-item latest">
            <div class="version-badge">v1.1.0</div>
            <div class="changelog-content">
              <div class="update-date">2025-08-14</div>
              <h3 class="update-title">搜索体验优化</h3>
              <ul class="update-details">
                <li>优化结果排序，较新的文章排得更靠前</li>
              </ul>
            </div>
          </div>

          <div class="changelog-item">
            <div class="version-badge">v1.0.0</div>
            <div class="changelog-content">
              <div class="update-date">2025-08-13</div>
              <h3 class="update-title">系统正式发布</h3>
              <ul class="update-details">
                <li>基础搜索功能上线</li>
              </ul>
            </div>
          </div>
        </div>
      </section>

      <!-- 问题反馈 -->
      <section class="feedback-section">
        <div class="feedback-card">
          <h2 class="feedback-title">遇到问题？告诉我们</h2>
          <p class="feedback-text">
            在使用过程中有任何建议或问题，欢迎通过微信联系我们，你的反馈是我们进步的动力
          </p>
          <div class="contact-way">
            <span class="wechat-label">微信：</span>
            <span class="wechat-id">15756303357</span>
          </div>
        </div>
      </section>

      <!-- 行动按钮 -->
      <section class="action-section">
        <button class="start-button" @click="$router.push('/s')">
          立即开始搜索
        </button>
      </section>
    </main>
  </div>
</template>

<script setup name="Index">
// 互联网风格的QikSearch首页逻辑
</script>

<style scoped lang="scss">
// 基础样式 - 互联网风格：简洁、现代、友好
.home {
  width: 100%;
  min-height: 100vh;
  background-color: #f5f7fa;
  font-family: 'Helvetica Neue', Arial, sans-serif;
  color: #333;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

// 内容容器
.content-wrapper {
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 0 20px;
  flex-grow: 1;
}

// 头部区域
.system-header {
  background: linear-gradient(135deg, #4285f4 0%, #34a853 100%);
  padding: 60px 20px;
  text-align: center;
  color: white;

  .header-content {
    max-width: 800px;
    margin: 0 auto;
  }

  .system-title {
    font-size: 42px;
    margin: 0 0 15px;
    font-weight: 700;
    letter-spacing: -0.5px;
  }

  .system-slogan {
    font-size: 20px;
    margin: 0;
    opacity: 0.9;
    font-weight: 300;
  }
}

// 简介区域
.intro-section {
  padding: 50px 0;

  .intro-card {
    background-color: white;
    border-radius: 10px;
    padding: 30px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  }

  .intro-text {
    font-size: 16px;
    line-height: 1.8;
    color: #555;
    margin: 0;
    text-align: center;
    max-width: 800px;
    margin: 0 auto;
  }
}

// 功能特性区域
.features-section {
  padding: 20px 0 60px;

  .section-title {
    font-size: 28px;
    text-align: center;
    margin: 0 0 40px;
    color: #333;
    font-weight: 600;
  }

  .features-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 30px;
  }

  .feature-item {
    background-color: white;
    border-radius: 10px;
    padding: 30px 20px;
    text-align: center;
    box-shadow: 0 3px 15px rgba(0, 0, 0, 0.05);
    transition: transform 0.3s ease, box-shadow 0.3s ease;

    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
    }
  }

  .feature-icon {
    width: 70px;
    height: 70px;
    margin: 0 auto 20px;
    background-color: #f0f7ff;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #4285f4;
    font-size: 30px;
  }

  // 自定义图标
  .search-icon::before { content: "🔍"; }
  .speed-icon::before { content: "⚡"; }
  .update-icon::before { content: "🔄"; }
  .easy-icon::before { content: "✓"; }

  .feature-title {
    font-size: 18px;
    margin: 0 0 15px;
    color: #333;
    font-weight: 600;
  }

  .feature-desc {
    font-size: 14px;
    color: #666;
    line-height: 1.6;
    margin: 0;
  }
}

// 更新日志区域
.changelog-section {
  padding: 20px 0 60px;
  position: relative;

  .changelog-timeline {
    position: relative;
    max-width: 800px;
    margin: 0 auto;
    padding-left: 20px;

    // 时间线竖线
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      width: 4px;
      background-color: #e0e0e0;
      border-radius: 2px;
    }
  }

  .changelog-item {
    position: relative;
    padding: 20px 0 20px 30px;
    margin-bottom: 10px;

    // 时间点
    &::before {
      content: '';
      position: absolute;
      left: -22px;
      top: 24px;
      width: 16px;
      height: 16px;
      border-radius: 50%;
      background-color: #4285f4;
      border: 4px solid #e8f0fe;
      z-index: 1;
    }

    // 最新版本高亮
    &.latest {
      &::before {
        background-color: #34a853;
        transform: scale(1.2);
      }
    }

    .version-badge {
      position: absolute;
      left: -100px;
      top: 20px;
      font-size: 14px;
      font-weight: 600;
      color: #4285f4;
      padding: 3px 10px;
      background-color: #e8f0fe;
      border-radius: 12px;
      white-space: nowrap;

      @media (max-width: 768px) {
        position: relative;
        left: 0;
        top: 0;
        margin-bottom: 10px;
        display: inline-block;
      }
    }

    .changelog-content {
      background-color: white;
      border-radius: 8px;
      padding: 20px;
      box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);

      .update-date {
        font-size: 13px;
        color: #888;
        margin: 0 0 10px;
      }

      .update-title {
        font-size: 18px;
        color: #333;
        margin: 0 0 15px;
        font-weight: 600;
      }

      .update-details {
        margin: 0;
        padding-left: 20px;
        list-style-type: disc;
        color: #666;
        font-size: 14px;
        line-height: 1.8;

        li {
          margin-bottom: 6px;
        }
      }
    }
  }
}

// 问题反馈区域
.feedback-section {
  padding: 30px 0 60px;

  .feedback-card {
    background-color: white;
    border-radius: 10px;
    padding: 35px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
    max-width: 700px;
    margin: 0 auto;
    text-align: center;
  }

  .feedback-title {
    font-size: 22px;
    margin: 0 0 15px;
    color: #333;
    font-weight: 600;
  }

  .feedback-text {
    font-size: 15px;
    color: #555;
    line-height: 1.6;
    margin: 0 0 20px;
  }

  .contact-way {
    display: inline-flex;
    align-items: center;
    font-size: 16px;

    .wechat-label {
      color: #333;
      font-weight: 500;
      margin-right: 8px;
    }

    .wechat-id {
      color: #4285f4;
      font-weight: 600;
    }
  }
}

// 行动按钮区域
.action-section {
  padding: 20px 0 60px;
  text-align: center;

  .start-button {
    background-color: #4285f4;
    color: white;
    border: none;
    border-radius: 5px;
    padding: 14px 36px;
    font-size: 16px;
    font-weight: 600;
    cursor: pointer;
    transition: background-color 0.3s ease, transform 0.2s ease;

    &:hover {
      background-color: #3367d6;
      transform: translateY(-2px);
    }

    &:active {
      transform: translateY(0);
    }
  }
}

// 响应式调整
@media (max-width: 768px) {
  .system-title {
    font-size: 32px !important;
  }

  .system-slogan {
    font-size: 18px !important;
  }

  .section-title {
    font-size: 24px !important;
  }

  .features-grid {
    grid-template-columns: 1fr;
  }

  .start-button {
    width: 100%;
    padding: 14px 20px !important;
  }

  // 调整更新日志在移动端显示
  .changelog-timeline {
    padding-left: 10px;
  }

  .changelog-item {
    padding-left: 20px;
  }
}
</style>
