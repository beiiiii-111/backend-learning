import { defineConfig } from 'vitepress'

export default defineConfig({
  title: '訫的学习笔记',
  description: '前后端工程化学习笔记 · Vue 3 / Vite / Spring Boot',

  // 本地开发用根路径 http://localhost:5173/
  // GitHub Actions 上（部署到 <用户名>.github.io/backend-learning/）才加前缀
  base: process.env.GITHUB_ACTIONS ? '/backend-learning/' : '/',

  lang: 'zh-CN',

  lastUpdated: true,
  cleanUrls: false,

  head: [
    ['meta', { name: 'theme-color', content: '#2f6fed' }]
  ],

  themeConfig: {
    logo: null,

    nav: [
      { text: '首页', link: '/' },
      { text: '前端笔记', link: '/frontend/', activeMatch: '/frontend/' },
      { text: '后端笔记', link: '/backend/', activeMatch: '/backend/' },
      {
        text: 'GitHub',
        link: 'https://github.com/beiiiii-111/backend-learning'
      }
    ],

    sidebar: {
      '/frontend/': [
        {
          text: 'Vue 3 入门',
          collapsed: false,
          items: [
            { text: '起步', link: '/frontend/' },
            { text: '01 · 环境准备', link: '/frontend/01-env-setup' },
            { text: '02 · Vue 字面量入门', link: '/frontend/02-vue-literal' },
            { text: '03 · Vue 基础指令合集', link: '/frontend/03-vue-directives' }
          ]
        },
        {
          text: '组件实战',
          collapsed: false,
          items: [
            { text: '04 · 组件拆分与事件', link: '/frontend/04-component-event' },
            { text: '05 · 计算属性与动态样式', link: '/frontend/05-props-style' },
            { text: '06 · 插槽与主题切换', link: '/frontend/06-slot-theme' }
          ]
        },
        {
          text: '工程化',
          collapsed: false,
          items: [
            { text: '07 · 用 VitePress 搭建本笔记站', link: '/frontend/07-vitepress-site' }
          ]
        }
      ],

      '/backend/': [
        {
          text: 'Spring Boot',
          collapsed: false,
          items: [
            { text: '起步', link: '/backend/' },
            { text: '01 · 快速入门', link: '/backend/01-quickstart' },
            { text: '02 · 配置管理', link: '/backend/02-config' }
          ]
        }
      ]
    },

    outline: {
      level: [2, 3],
      label: '本页目录'
    },

    docFooter: {
      prev: '上一篇',
      next: '下一篇'
    },

    lastUpdated: {
      text: '最后更新于',
      formatOptions: { dateStyle: 'medium', timeStyle: 'short' }
    },

    socialLinks: [
      { icon: 'github', link: 'https://github.com/beiiiii-111/backend-learning' }
    ],

    footer: {
      message: '前后端工程化学习笔记',
      copyright: 'Copyright © 2026 · 基于 VitePress 构建'
    },

    returnToTopLabel: '回到顶部',
    sidebarMenuLabel: '目录',
    darkModeSwitchLabel: '主题',
    lightModeSwitchTitle: '切换到浅色模式',
    darkModeSwitchTitle: '切换到深色模式',
    skipToContentLabel: '跳到内容',

    editLink: {
      pattern: 'https://github.com/beiiiii-111/backend-learning/edit/main/docs/:path',
      text: '在 GitHub 上编辑此页'
    },

    search: {
      provider: 'local',
      options: {
        translations: {
          button: { buttonText: '搜索笔记', buttonAriaLabel: '搜索笔记' },
          modal: {
            noResultsText: '没有找到相关内容',
            resetButtonTitle: '清空查询',
            footer: {
              selectText: '选择',
              navigateText: '切换',
              closeText: '关闭'
            }
          }
        }
      }
    }
  }
})
