<template>
  <div class="login-container">
    <!-- 背景装饰元素 -->
    <div class="bg-gradient"></div>

    <el-form ref="loginRef" :model="loginForm" :rules="loginRules" class="login-card">
      <!-- 品牌区域 -->
      <div class="brand-area">
        <span class="google-logo">Q</span>
        <span class="app-name">{{ title }}</span>
      </div>

      <h1 class="login-title">登录您的账户</h1>

      <el-form-item prop="username">
        <el-input
            v-model="loginForm.username"
            type="text"
            size="large"
            auto-complete="username"
            placeholder="电子邮件或电话号码"
            class="custom-input"
        >
          <template #prefix><svg-icon icon-class="user" class="input-icon" /></template>
        </el-input>
      </el-form-item>

      <el-form-item prop="password">
        <el-input
            v-model="loginForm.password"
            type="password"
            size="large"
            auto-complete="current-password"
            placeholder="密码"
            @keyup.enter="handleLogin"
            class="custom-input"
        >
          <template #prefix><svg-icon icon-class="password" class="input-icon" /></template>
        </el-input>
      </el-form-item>

      <el-form-item prop="code" v-if="captchaEnabled" class="code-group">
        <el-input
            v-model="loginForm.code"
            size="large"
            auto-complete="off"
            placeholder="验证码"
            class="custom-input code-input"
            @keyup.enter="handleLogin"
        >
          <template #prefix><svg-icon icon-class="validCode" class="input-icon" /></template>
        </el-input>
        <div class="code-image-container">
          <img :src="codeUrl" @click="getCode" class="code-image" :alt="refreshCodeText"/>
        </div>
      </el-form-item>

      <div class="form-actions">
        <div class="form-options">
          <el-checkbox v-model="loginForm.rememberMe" class="remember-checkbox">
            记住密码
          </el-checkbox>
        </div>

        <el-button
            :loading="loading"
            size="large"
            type="primary"
            class="login-button"
            @click.prevent="handleLogin"
        >
          <span v-if="!loading">登录</span>
          <span v-else>登录中...</span>
        </el-button>

      </div>
    </el-form>

    <!-- 页脚信息 -->
    <footer class="page-footer">
      <div class="footer-links">
        <a href="#" class="footer-link">帮助</a>
        <a href="#" class="footer-link">隐私政策</a>
        <a href="#" class="footer-link">服务条款</a>
      </div>
      <p class="copyright">{{ new Date().getFullYear() }} © {{ title }}</p>
    </footer>
  </div>
</template>

<script setup>
import { getCodeImg } from "@/api/login"
import Cookies from "js-cookie"
import { encrypt, decrypt } from "@/utils/jsencrypt"
import useUserStore from '@/store/modules/user'
import { useRoute, useRouter } from 'vue-router'
import { getCurrentInstance, ref, watch } from 'vue'

const title = import.meta.env.VITE_APP_TITLE || 'Google 账户'
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()

const loginForm = ref({
  username: "",
  password: "",
  rememberMe: false,
  code: "",
  uuid: ""
})

const loginRules = {
  username: [{ required: true, trigger: "blur", message: "请输入用户名" }],
  password: [{ required: true, trigger: "blur", message: "请输入密码" }],
  code: [{ required: true, trigger: "change", message: "请输入验证码" }]
}

const codeUrl = ref("")
const loading = ref(false)
const captchaEnabled = ref(true)
const redirect = ref(undefined)
const refreshCodeText = ref("点击刷新验证码")

watch(route, (newRoute) => {
  redirect.value = newRoute.query && newRoute.query.redirect
}, { immediate: true })

function handleLogin() {
  proxy.$refs.loginRef.validate(valid => {
    if (valid) {
      loading.value = true
      if (loginForm.value.rememberMe) {
        Cookies.set("username", loginForm.value.username, { expires: 30 })
        Cookies.set("password", encrypt(loginForm.value.password), { expires: 30 })
        Cookies.set("rememberMe", loginForm.value.rememberMe, { expires: 30 })
      } else {
        Cookies.remove("username")
        Cookies.remove("password")
        Cookies.remove("rememberMe")
      }

      userStore.login(loginForm.value).then(() => {
        const query = route.query
        const otherQueryParams = Object.keys(query).reduce((acc, cur) => {
          if (cur !== "redirect") {
            acc[cur] = query[cur]
          }
          return acc
        }, {})
        router.push({ path: redirect.value || "/", query: otherQueryParams })
      }).catch(() => {
        loading.value = false
        if (captchaEnabled.value) {
          getCode()
        }
      })
    }
  })
}

function getCode() {
  // 加载时显示占位状态
  codeUrl.value = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='120' height='40' viewBox='0 0 120 40'%3E%3Crect width='120' height='40' fill='%23f8f9fa'/%3E%3C/svg%3E"
  refreshCodeText.value = "加载中..."

  getCodeImg().then(res => {
    captchaEnabled.value = res.captchaEnabled === undefined ? true : res.captchaEnabled
    if (captchaEnabled.value) {
      codeUrl.value = "data:image/gif;base64," + res.img
      loginForm.value.uuid = res.uuid
      refreshCodeText.value = "点击刷新验证码"
    }
  }).catch(() => {
    refreshCodeText.value = "加载失败，点击重试"
  })
}

function getCookie() {
  const username = Cookies.get("username")
  const password = Cookies.get("password")
  const rememberMe = Cookies.get("rememberMe")
  loginForm.value = {
    username: username === undefined ? loginForm.value.username : username,
    password: password === undefined ? loginForm.value.password : decrypt(password),
    rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
  }
}

// 初始化
getCode()
getCookie()
</script>

<style lang='scss' scoped>
// 谷歌风格变量
$google-blue: #165DFF;
$google-red: #ea4335;
$google-yellow: #fbbc05;
$google-green: #34a853;
$text-primary: #202124;
$text-secondary: #5f6368;
$text-link: #1967d2;
$bg-light: #f8f9fa;
$border-light: #dadce0;
$shadow-light: 0 1px 2px rgba(0, 0, 0, 0.1);
$shadow-hover: 0 2px 8px rgba(0, 0, 0, 0.15);

.login-container {
  min-height: 100vh;
  padding: 24px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #fff;
  position: relative;

  // 背景渐变装饰
  .bg-gradient {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100vh;
    background: linear-gradient(180deg,
        rgba(241, 243, 244, 0.95) 0%,  /* 谷歌标志性浅灰 */
        rgba(255, 255, 255, 0.8) 50%,
        rgba(255, 255, 255, 0) 100%
    );
    z-index: 0;
    pointer-events: none;
  }
}

// 登录卡片 - 缩小版本
.login-card {
  width: 100%;
  max-width: 360px; /* 从450px缩小到360px */
  padding: 28px; /* 从40px缩小到28px */
  background: #fff;
  border-radius: 8px;
  border: 1px solid $border-light;
  box-shadow: $shadow-light;
  z-index: 1;
  transition: box-shadow 0.3s ease;

  &:hover {
    box-shadow: $shadow-hover;
  }

  .brand-area {
    display: flex;
    align-items: center;
    margin-bottom: 24px; /* 从32px缩小到24px */

    .google-logo {
      font-size: 28px; /* 从32px缩小到28px */
      font-weight: bold;
      color: $google-blue;
      margin-right: 10px; /* 从12px缩小到10px */
      text-shadow: 0 1px 2px rgba(0,0,0,0.1);
    }

    .app-name {
      font-size: 20px; /* 从22px缩小到20px */
      color: $text-primary;
      font-weight: 500;
    }
  }

  .login-title {
    text-align: left;
    color: $text-primary;
    font-size: 22px; /* 从24px缩小到22px */
    font-weight: 400;
    margin: 0 0 6px 0; /* 从8px缩小到6px */
  }

  .custom-input {
    height: 48px; /* 从52px缩小到48px */
    margin-bottom: 18px; /* 从24px缩小到18px */
    transition: all 0.2s ease;

    .el-input__inner {
      height: 48px; /* 同步输入框高度 */
      padding: 0 14px; /* 从16px缩小到14px */
      border-radius: 4px;
      border: 1px solid $border-light;
      font-size: 15px; /* 从16px缩小到15px */
      color: $text-primary;
      background-color: #fff;
      transition: all 0.2s ease;

      &:focus {
        border-color: $google-blue;
        box-shadow: 0 0 0 2px rgba(22, 93, 255, 0.2);
        outline: none;
      }

      &::placeholder {
        color: $text-secondary;
        opacity: 0.8;
      }
    }

    .input-icon {
      color: $text-secondary;
      width: 18px; /* 从20px缩小到18px */
      height: 18px; /* 同步图标大小 */
    }
  }

  // 验证码区域
  .code-group {
    display: flex;
    gap: 12px; /* 从16px缩小到12px */
    margin-bottom: 18px; /* 从24px缩小到18px */

    .code-input {
      margin-bottom: 0;
      flex: 1;
    }

    .code-image-container {
      width: 100px; /* 从120px缩小到100px */
      height: 48px; /* 从52px缩小到48px */
      margin-left: 8px; /* 从12px缩小到8px */

      .code-image {
        width: 100%;
        height: 100%;
        object-fit: cover;
        border-radius: 4px;
        cursor: pointer;
        background-color: $bg-light;
        border: 1px solid $border-light;
        transition: all 0.2s ease;

        &:hover {
          border-color: $google-blue;
        }
      }
    }
  }

  .form-actions {
    margin-top: 6px; /* 从8px缩小到6px */

    .form-options {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 18px; /* 从24px缩小到18px */
    }

    .remember-checkbox {
      color: $text-primary;
      font-size: 13px; /* 从14px缩小到13px */
      display: flex;
      align-items: center;

      .el-checkbox__label {
        padding-left: 6px; /* 从8px缩小到6px */
      }

      .el-checkbox__input.is-checked .el-checkbox__inner {
        background-color: $google-blue;
        border-color: $google-blue;
      }
    }

    .login-button {
      width: 100%;
      height: 48px; /* 从52px缩小到48px */
      font-size: 15px; /* 从16px缩小到15px */
      font-weight: 500;
      border-radius: 4px;
      background-color: $google-blue;
      border-color: $google-blue;
      transition: all 0.2s ease;

      &:hover {
        background-color: #0d47a1;
        border-color: #0d47a1;
      }

      &.is-loading {
        background-color: $google-blue;
        border-color: $google-blue;
      }
    }

    .register-container {
      text-align: center;
      margin-top: 24px; /* 从32px缩小到24px */

      .register-text {
        color: $text-secondary;
        font-size: 13px; /* 从14px缩小到13px */
      }

      .register-link {
        color: $text-link;
        text-decoration: none;
        font-size: 13px; /* 从14px缩小到13px */
        font-weight: 500;
        margin-left: 3px; /* 从4px缩小到3px */
        transition: color 0.2s ease;

        &:hover {
          color: #1557b8;
          text-decoration: underline;
        }
      }
    }
  }
}

// 页脚样式 - 略微缩小
.page-footer {
  margin-top: 32px; /* 从40px缩小到32px */
  text-align: center;
  color: $text-secondary;
  font-size: 11px; /* 从12px缩小到11px */
  z-index: 1;

  .footer-links {
    margin-bottom: 10px; /* 从12px缩小到10px */

    .footer-link {
      color: inherit;
      text-decoration: none;
      margin: 0 10px; /* 从12px缩小到10px */
      transition: color 0.2s ease;

      &:hover {
        color: $text-primary;
        text-decoration: underline;
      }
    }
  }

  .copyright {
    margin: 0;
  }
}

// 响应式调整 - 适配更小屏幕
@media (max-width: 500px) {
  .bg-gradient {
    background: #fff !important;
  }

  .login-card {
    padding: 20px; /* 从24px缩小到20px */
    box-shadow: none !important;
    border: none !important;
    max-width: 100%;
    margin-top: 20px; /* 距顶部50px */
    margin-bottom: auto; /* 抵消flex布局的居中效果 */
  }

  .brand-area {
    margin-bottom: 20px; /* 从24px缩小到20px */
  }

  .login-title {
    font-size: 20px; /* 从22px缩小到20px */
  }

  .code-group {
    gap: 10px; /* 从12px缩小到10px */
  }

  .code-image-container {
    width: 90px !important; /* 从100px缩小到90px */
  }
}
</style>
