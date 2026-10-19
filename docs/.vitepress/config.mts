import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { defineConfig } from 'vitepress'

// docs/.vitepress/config.mts → docs/
const docsDir = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const frontendDir = path.join(docsDir, 'frontend')

const SKIP_DIRS = new Set(['.vitepress', 'node_modules'])
// 站点里没有源码仓库，这些路径在同步脚本里已经降级成行内代码了

function isMd(name) {
  return name.endsWith('.md')
}

function sortByName(entries) {
  return [...entries].sort((a, b) =>
    a.name.localeCompare(b.name, 'zh-Hans-CN', { numeric: true })
  )
}

/** 目录 / 文件在站上的显示名：去掉编号前缀里的连字符，改成中位点 */
function prettyName(name) {
  const base = name.replace(/\.md$/, '')
  return base.replace(/^(\d+)-/, '$1 · ')
}

/** 用文件里的一级标题做标题，取不到就退回文件名 */
function titleOf(file, fallback) {
  try {
    const text = fs.readFileSync(file, 'utf-8')
    const m = text.match(/^#\s+(.+?)\s*$/m)
    if (m) return m[1]
  } catch {
    /* 读不到就用文件名 */
  }
  return fallback
}

function linkOf(relPosix) {
  const noExt = relPosix.replace(/\.md$/, '')
  return noExt === 'README' ? '/frontend/' : `/frontend/${noExt}`
}

/** 递归生成侧边栏条目：先本层文件，再子目录（子目录折叠） */
function walk(dir, relBase) {
  const items = []
  if (!fs.existsSync(dir)) return items

  const entries = sortByName(fs.readdirSync(dir, { withFileTypes: true }))

  for (const entry of entries) {
    if (entry.isFile() && isMd(entry.name)) {
      if (entry.name === 'README.md' || entry.name === 'index.md') continue
      const rel = relBase ? `${relBase}/${entry.name}` : entry.name
      items.push({
        text: titleOf(path.join(dir, entry.name), prettyName(entry.name)),
        link: linkOf(rel)
      })
    }
  }

  for (const entry of entries) {
    if (!entry.isDirectory() || SKIP_DIRS.has(entry.name)) continue
    const rel = relBase ? `${relBase}/${entry.name}` : entry.name
    const childDir = path.join(dir, entry.name)
    const childItems = walk(childDir, rel)
    if (childItems.length === 0) continue
    items.push({
      text: prettyName(entry.name),
      collapsed: true,
      items: childItems
    })
  }

  return items
}

/** 前端教程侧边栏：顶部放索引与清单，下面按 10 个部分分组 */
function frontendSidebar() {
  const rootFiles = fs
    .readdirSync(frontendDir, { withFileTypes: true })
    .filter((e) => e.isFile() && isMd(e.name) && e.name !== 'README.md')

  const indexItems = sortByName(rootFiles).map((entry) => ({
    text: titleOf(path.join(frontendDir, entry.name), prettyName(entry.name)),
    link: linkOf(entry.name)
  }))

  const groups = [
    {
      text: '索引与清单',
      collapsed: false,
      items: [
        { text: '教程首页', link: '/frontend/' },
        ...indexItems
      ]
    }
  ]

  const dirs = sortByName(
    fs
      .readdirSync(frontendDir, { withFileTypes: true })
      .filter((e) => e.isDirectory() && !SKIP_DIRS.has(e.name))
  )

  for (const dir of dirs) {
    const dirPath = path.join(frontendDir, dir.name)
    const items = walk(dirPath, dir.name)
    if (items.length === 0) continue
    const group: Record<string, unknown> = {
      text: prettyName(dir.name),
      collapsed: true,
      items
    }
    // 有索引页的部分可以整块点进去
    if (fs.existsSync(path.join(dirPath, 'index.md'))) {
      group.link = `/frontend/${dir.name}/`
    }
    groups.push(group)
  }

  return groups
}

export default defineConfig({
  title: '訫的学习笔记',
  description: '前后端工程化学习笔记 · Vue 3 / Vite / Spring Boot',

  // 本地开发用根路径 http://localhost:5173/
  // GitHub Actions 上（部署到 <用户名>.github.io/backend-learning/）才加前缀
  base: process.env.GITHUB_ACTIONS ? '/backend-learning/' : '/',

  lang: 'zh-CN',

  lastUpdated: true,
  cleanUrls: false,

  // 教程里大量出现 {{ }}，不处理会被 Vue 当插值执行，示例直接消失。
  // 代码块统一加 v-pre，行内代码在同步脚本里包成 <code v-pre>。
  markdown: {
    codeTransformers: [
      {
        name: 'v-pre-for-code-blocks',
        pre(node) {
          node.properties['v-pre'] = ''
        }
      }
    ]
  },

  // 1) 教程里带了一份独立的 Git 地图 HTML，VitePress 不把它当页面
  // 2) 教程正文里的 http://localhost:8080 只是示例地址，不是真链接
  // 注意：这里匹配到的是百分号编码后的路径
  ignoreDeadLinks: [
    /%E4%BB%93%E5%BA%93Git%E5%9C%B0%E5%9B%BE/,
    /^https?:\/\/localhost/
  ],

  head: [['meta', { name: 'theme-color', content: '#2f6fed' }]],

  themeConfig: {
    logo: null,

    nav: [
      { text: '首页', link: '/' },
      { text: '前端教程', link: '/frontend/', activeMatch: '/frontend/' },
      { text: '后端笔记', link: '/backend/', activeMatch: '/backend/' },
      { text: '本站怎么做', link: '/site/07-vitepress-site', activeMatch: '/site/' },
      {
        text: 'GitHub',
        link: 'https://github.com/beiiiii-111/backend-learning'
      }
    ],

    sidebar: {
      '/frontend/': frontendSidebar(),

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
      {
        icon: 'github',
        link: 'https://github.com/beiiiii-111/backend-learning'
      }
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
      pattern:
        'https://github.com/beiiiii-111/backend-learning/edit/main/docs/:path',
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
