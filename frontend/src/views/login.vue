<template>
  <div class="login-container">
    <!-- 背景装饰元素 -->
    <div class="bg-decoration top-left"></div>
    <div class="bg-decoration bottom-right"></div>

    <el-form ref="loginRef" :model="loginForm" :rules="loginRules" class="login-card">
      <!-- 品牌区域 -->
      <div class="brand-area">
        <span
            style="
      font-size: 28px;
      font-weight: 600;
      letter-spacing: -0.5px;
      line-height: 1.2;
      text-rendering: optimizeLegibility;
      -webkit-font-smoothing: antialiased;
      background: linear-gradient(90deg, #4285f4, #34a853);
      -webkit-background-clip: text;
      background-clip: text;
      color: transparent;
      text-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
    "
        >
    {{ title }}
  </span>
      </div>

      <h3 class="login-title">登录您的账户</h3>

      <el-form-item prop="username">
        <el-input
            v-model="loginForm.username"
            type="text"
            size="large"
            auto-complete="off"
            placeholder="账号"
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
            auto-complete="off"
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
        <el-checkbox v-model="loginForm.rememberMe" class="remember-checkbox">
          记住密码
        </el-checkbox>

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

        <div v-if="register" class="register-link-container">
          <router-link class="register-link" :to="'/register'">
            立即注册
          </router-link>
        </div>
      </div>
    </el-form>

    <!-- 页脚信息 -->
    <footer class="page-footer">
      <div class="footer-links">
        <a href="#" class="footer-link">帮助</a>
        <a href="#" class="footer-link">隐私政策</a>
        <a href="#" class="footer-link">条款</a>
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

const title = import.meta.env.VITE_APP_TITLE
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
  username: [{ required: true, trigger: "blur", message: "请输入您的账号" }],
  password: [{ required: true, trigger: "blur", message: "请输入您的密码" }],
  code: [{ required: true, trigger: "change", message: "请输入验证码" }]
}

const codeUrl = ref("")
const loading = ref(false)
const captchaEnabled = ref(true)
const register = ref(false)
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
  codeUrl.value = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='120' height='40' viewBox='0 0 120 40'%3E%3Crect width='120' height='40' fill='%23f5f5f5'/%3E%3C/svg%3E"
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
// 基础变量
$primary-color: #4285f4;
$secondary-color: #34a853;
$accent-color: #fbbc05;
$danger-color: #ea4335;
$light-gray: #f5f5f5;
$mid-gray: #e0e0e0;
$dark-gray: #5f6368;
$text-color: #202124;

.login-container {
  min-height: 100vh;
  padding: 24px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f8f9fa 0%, #e8f0fe 100%);
  position: relative;
  overflow: hidden;

  // 背景装饰
  .bg-decoration {
    position: absolute;
    width: 600px;
    height: 600px;
    border-radius: 50%;
    opacity: 0.1;
    z-index: 0;

    &.top-left {
      top: -300px;
      left: -300px;
      background: radial-gradient(circle, $primary-color 0%, transparent 70%);
    }

    &.bottom-right {
      bottom: -300px;
      right: -300px;
      background: radial-gradient(circle, $secondary-color 0%, transparent 70%);
    }
  }
}

// 登录卡片
.login-card {
  width: 100%;
  max-width: 400px;
  padding: 32px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
  z-index: 1;
  transition: box-shadow 0.3s ease;

  &:hover {
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
  }

  .brand-area {
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 24px;

    .logo {
      margin-right: 12px;
    }

    .app-name {
      font-size: 22px;
      color: $text-color;
      margin: 0;
      font-weight: 500;
    }
  }

  .login-title {
    text-align: center;
    color: $text-color;
    font-size: 24px;
    font-weight: 500;
    margin: 0 0 32px 0;
  }

  .custom-input {
    height: 48px;
    margin-bottom: 16px;
    transition: all 0.2s ease;

    &:hover {
      border-color: $primary-color;
    }

    .el-input__inner {
      height: 48px;
      padding: 0 16px;
      border-radius: 8px;
      border-color: $mid-gray;
      font-size: 14px;

      &:focus {
        border-color: $primary-color;
        box-shadow: 0 0 0 2px rgba(66, 133, 244, 0.2);
      }
    }

    .input-icon {
      color: $dark-gray;
      width: 18px;
      height: 18px;
    }
  }

  .code-group {
    display: flex;
    gap: 12px;
    margin-bottom: 16px;

    .code-input {
      margin-bottom: 0;
      flex: 1;
    }

    .code-image-container {
      width: 120px;
      height: 48px;

      .code-image {
        width: 100%;
        height: 100%;
        object-fit: cover;
        border-radius: 8px;
        cursor: pointer;
        background-color: $light-gray;
        transition: transform 0.2s ease;

        &:hover {
          transform: scale(1.02);
        }
      }
    }
  }

  .form-actions {
    margin-top: 8px;

    .remember-checkbox {
      color: $dark-gray;
      font-size: 14px;
      margin-bottom: 20px;
      display: flex;
      align-items: center;

      .el-checkbox__label {
        padding-left: 8px;
      }

      .el-checkbox__input.is-checked .el-checkbox__inner {
        background-color: $primary-color;
        border-color: $primary-color;
      }
    }

    .login-button {
      width: 100%;
      height: 48px;
      font-size: 16px;
      font-weight: 500;
      border-radius: 8px;
      background-color: $primary-color;
      border-color: $primary-color;
      transition: all 0.2s ease;

      &:hover {
        background-color: #3367d6;
        border-color: #3367d6;
        transform: translateY(-1px);
      }

      &:active {
        transform: translateY(0);
      }

      &.is-loading {
        background-color: $primary-color;
        border-color: $primary-color;
      }
    }

    .register-link-container {
      text-align: center;
      margin-top: 24px;

      .register-link {
        color: $primary-color;
        text-decoration: none;
        font-size: 14px;
        transition: color 0.2s ease;

        &:hover {
          color: #3367d6;
          text-decoration: underline;
        }
      }
    }
  }
}

// 页脚样式
.page-footer {
  margin-top: 48px;
  text-align: center;
  color: $dark-gray;
  font-size: 12px;
  z-index: 1;

  .footer-links {
    margin-bottom: 8px;

    .footer-link {
      color: inherit;
      text-decoration: none;
      margin: 0 8px;

      &:hover {
        text-decoration: underline;
      }
    }
  }

  .copyright {
    margin: 0;
  }
}

// 响应式调整
@media (max-width: 480px) {
  .login-card {
    padding: 24px;
    box-shadow: none;
    background: transparent;
  }

  .bg-decoration {
    display: none;
  }
}
</style>
