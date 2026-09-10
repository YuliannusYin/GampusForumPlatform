<template>
  <div class="wall-page">
    <section class="wall-hero">
      <div class="hero-stars" aria-hidden="true"></div>
      <div class="hero-copy">
        <p class="hero-kicker">Campus Night Board</p>
        <h1 class="hero-title">夜里说不出口的话，<br />可以贴在这里。</h1>
        <p class="hero-desc">
          发帖前台匿名，评论区仍是实名。想认真回应就留下名字；想倾诉，就当这面墙替你保密。
        </p>
        <div class="hero-actions">
          <el-button class="seal-btn" type="danger" round @click="goCreate">投一封匿名信</el-button>
          <span class="hero-count">墙上已有 {{ total }} 封</span>
        </div>
      </div>
      <div class="hero-note" aria-hidden="true">
        <span class="pin"></span>
        <p>评论实名 · 违规可举报 · 管理员可追溯</p>
      </div>
    </section>

    <div class="wall-toolbar">
      <button
        v-for="opt in sortOptions"
        :key="opt.value"
        class="wall-sort"
        :class="{ active: sort === opt.value }"
        @click="changeSort(opt.value)"
      >
        {{ opt.label }}
      </button>
    </div>

    <div v-loading="loading" class="wall-board">
      <article
        v-for="(post, index) in posts"
        :key="post.id"
        class="note-card"
        :class="'paper-' + (post.id % 4)"
        :style="{ '--tilt': tiltOf(post.id), animationDelay: index * 0.04 + 's' }"
        @click="goDetail(post.id)"
      >
        <span class="thumbtack"></span>
        <h2 class="note-title">{{ post.title }}</h2>
        <p class="note-body">{{ post.summary || '……' }}</p>
        <footer class="note-foot">
          <span class="note-author">{{ authorLabel(post) }}</span>
          <span class="note-meta">
            {{ post.commentCount || 0 }} 评 · {{ post.likeCount || 0 }} 赞
          </span>
        </footer>
      </article>
    </div>

    <el-empty v-if="!loading && posts.length === 0" description="墙还空着，要不要当第一个说出口的人" />

    <div v-if="total > 0" class="wall-pager">
      <el-pagination
        v-model:current-page="page"
        :page-size="size"
        :total="total"
        layout="prev, pager, next, total"
        background
        @current-change="loadPosts"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPosts, getSections } from '@/api/post'

const router = useRouter()
const loading = ref(false)
const posts = ref([])
const page = ref(1)
const size = 12
const total = ref(0)
const sort = ref('latest')
const sectionId = ref(null)

const sortOptions = [
  { value: 'latest', label: '最新' },
  { value: 'hot', label: '最热' }
]

const tiltOf = (id) => `${((id % 5) - 2) * 1.15}deg`

const authorLabel = (post) => {
  if (post.isAuthor) return '匿名（我）'
  return '匿名墙友'
}

const changeSort = (value) => {
  sort.value = value
  page.value = 1
  loadPosts()
}

const goCreate = () => {
  router.push({ path: '/post/create', query: { anonymous: '1' } })
}

const goDetail = (id) => {
  router.push(`/post/${id}`)
}

const resolveSection = async () => {
  const list = await getSections()
  const found = (list || []).find((item) => item.code === 'confession')
  sectionId.value = found ? found.id : null
}

const loadPosts = async () => {
  if (!sectionId.value) return
  loading.value = true
  try {
    const res = await getPosts({
      page: page.value,
      size,
      sectionId: sectionId.value,
      sort: sort.value
    })
    posts.value = res.records || []
    total.value = res.total || 0
  } catch (err) {
    posts.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    await resolveSection()
    await loadPosts()
  } catch (err) {
    loading.value = false
  }
})
</script>

<style scoped>
.wall-page {
  --wall-night: #1b1523;
  --wall-paper: #f6ead2;
  --wall-blush: #f3d5c6;
  --wall-gold: #efe0b0;
  --wall-mint: #e4ead6;
  --wall-ink: #2a2118;
  --wall-seal: #b4232c;
  --wall-font-display: "ZCOOL XiaoWei", "Noto Serif SC", serif;
  --wall-font-body: "Noto Serif SC", "Songti SC", serif;
  max-width: 1080px;
  margin: 0 auto;
  padding-bottom: 48px;
}

.wall-hero {
  position: relative;
  overflow: hidden;
  border-radius: 28px;
  padding: 48px 40px 36px;
  background:
    radial-gradient(circle at 12% 20%, rgba(244, 214, 176, 0.18), transparent 36%),
    radial-gradient(circle at 88% 10%, rgba(180, 35, 44, 0.22), transparent 32%),
    linear-gradient(160deg, #241c2c 0%, #1b1523 55%, #121018 100%);
  color: #f7efe3;
  margin-bottom: 28px;
  min-height: 280px;
}

.hero-stars {
  position: absolute;
  inset: 0;
  background-image:
    radial-gradient(#f6ead2 1px, transparent 1px),
    radial-gradient(#f6ead2 1px, transparent 1px);
  background-size: 72px 72px, 48px 48px;
  background-position: 0 0, 24px 36px;
  opacity: 0.18;
  pointer-events: none;
}

.hero-copy {
  position: relative;
  max-width: 640px;
  z-index: 1;
}

.hero-kicker {
  letter-spacing: 0.22em;
  text-transform: uppercase;
  font-size: 12px;
  color: #d4b483;
  margin: 0 0 12px;
}

.hero-title {
  font-family: var(--wall-font-display);
  font-size: 42px;
  font-weight: 400;
  line-height: 1.28;
  margin: 0 0 16px;
}

.hero-desc {
  font-family: var(--wall-font-body);
  font-size: 15px;
  line-height: 1.8;
  color: rgba(247, 239, 227, 0.78);
  margin: 0 0 24px;
  max-width: 520px;
}

.hero-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.seal-btn {
  --el-button-bg-color: var(--wall-seal);
  --el-button-border-color: var(--wall-seal);
  font-family: var(--wall-font-body);
  letter-spacing: 0.08em;
  padding: 12px 22px;
  height: auto;
}

.hero-count {
  font-size: 13px;
  color: #d4b483;
}

.hero-note {
  position: absolute;
  right: 36px;
  bottom: 28px;
  width: 210px;
  background: var(--wall-paper);
  color: var(--wall-ink);
  padding: 18px 16px 14px;
  transform: rotate(4deg);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.28);
  font-family: var(--wall-font-body);
  font-size: 13px;
  line-height: 1.6;
}

.pin {
  position: absolute;
  top: -8px;
  left: 50%;
  width: 14px;
  height: 14px;
  margin-left: -7px;
  border-radius: 50%;
  background: radial-gradient(circle at 35% 35%, #fff6, #b4232c 55%, #7a1218);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.wall-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
}

.wall-sort {
  border: 1px solid #e4d3b4;
  background: transparent;
  color: #6b5744;
  border-radius: 999px;
  padding: 6px 16px;
  cursor: pointer;
  font-family: var(--wall-font-body);
}

.wall-sort.active {
  background: #1b1523;
  border-color: #1b1523;
  color: #f6ead2;
}

.wall-board {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 22px 18px;
  min-height: 160px;
}

.note-card {
  position: relative;
  padding: 28px 20px 18px;
  min-height: 210px;
  cursor: pointer;
  transform: rotate(var(--tilt));
  animation: note-in 0.5s ease both;
  box-shadow: 3px 8px 18px rgba(42, 33, 24, 0.12);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.note-card:hover {
  transform: rotate(0deg) translateY(-4px);
  box-shadow: 6px 16px 28px rgba(42, 33, 24, 0.18);
  z-index: 2;
}

.paper-0 { background: var(--wall-paper); }
.paper-1 { background: var(--wall-blush); }
.paper-2 { background: var(--wall-gold); }
.paper-3 { background: var(--wall-mint); }

.thumbtack {
  position: absolute;
  top: 10px;
  left: 50%;
  width: 12px;
  height: 12px;
  margin-left: -6px;
  border-radius: 50%;
  background: radial-gradient(circle at 30% 30%, #fff8, #c43c3c 60%, #7a1218);
}

.note-title {
  font-family: var(--wall-font-display);
  font-size: 22px;
  font-weight: 400;
  color: var(--wall-ink);
  line-height: 1.35;
  margin: 8px 0 12px;
}

.note-body {
  font-family: var(--wall-font-body);
  font-size: 14px;
  color: #5b4a3a;
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 88px;
}

.note-foot {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  margin-top: 16px;
  font-size: 12px;
  color: #8a7460;
  font-family: var(--wall-font-body);
}

.note-author {
  letter-spacing: 0.06em;
}

.wall-pager {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}

@keyframes note-in {
  from {
    opacity: 0;
    transform: rotate(var(--tilt)) translateY(12px);
  }
  to {
    opacity: 1;
    transform: rotate(var(--tilt));
  }
}

@media (max-width: 768px) {
  .wall-hero {
    padding: 32px 20px 24px;
  }

  .hero-title {
    font-size: 30px;
  }

  .hero-note {
    position: static;
    width: auto;
    margin-top: 24px;
    transform: none;
  }
}
</style>
