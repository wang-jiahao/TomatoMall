const bookCatalog = [
  {
    title: '活着',
    cover: '/covers/huozhe.jpg',
    fallback: '/covers/cover-1.svg',
    price: 39,
    stock: 120,
    rate: 9.1,
    author: '余华',
    publisher: '作家出版社',
    publishedAt: '2012-06',
    description: '一个普通农民在时代洪流中的坚韧与善良。',
    detail:
      '<p>福贵的人生起落沉浮，却始终以朴素的方式守住对生活的热爱。</p><p>这是关于苦难与希望、生命与尊严的动人书写。</p>'
  },
  {
    title: '三体Ⅱ：黑暗森林',
    cover: '/covers/santi2.jpg',
    fallback: '/covers/cover-2.svg',
    price: 58,
    stock: 86,
    rate: 9.3,
    author: '刘慈欣',
    publisher: '重庆出版社',
    publishedAt: '2008-05',
    description: '“黑暗森林”法则揭示宇宙文明的生存逻辑。',
    detail:
      '<p>人类文明在宇宙尺度上渺小而脆弱，威慑与信任成为关键。</p><p>科幻史诗进入更深层的哲学与文明博弈。</p>'
  },
  {
    title: '解忧杂货店',
    cover: '/covers/jieyou.jpg',
    fallback: '/covers/cover-3.svg',
    price: 45,
    stock: 108,
    rate: 8.8,
    author: '东野圭吾',
    publisher: '南海出版公司',
    publishedAt: '2014-06',
    description: '一封封跨越时空的来信，串起人与人的温情。',
    detail:
      '<p>在静静伫立的杂货店里，烦恼与希望被认真倾听。</p><p>善意与选择让人生有了意想不到的回响。</p>'
  },
  {
    title: '小王子',
    cover: '/covers/xiaowangzi.jpg',
    fallback: '/covers/cover-4.svg',
    price: 28,
    stock: 160,
    rate: 9.0,
    author: '安托万·德·圣-埃克苏佩里',
    publisher: '人民文学出版社',
    publishedAt: '2013-05',
    description: '献给成人的童话，提醒我们珍视纯真与爱。',
    detail:
      '<p>在宇宙的旅途中，小王子与飞行员分享关于爱与责任的故事。</p><p>每一次相遇都照见了成人世界的另一种可能。</p>'
  },
  {
    title: '白夜行',
    cover: '/covers/baiyexing.jpg',
    fallback: '/covers/cover-5.svg',
    price: 52,
    stock: 72,
    rate: 8.9,
    author: '东野圭吾',
    publisher: '南海出版公司',
    publishedAt: '2008-09',
    description: '两个孩子在漫长岁月里相互牵引、彼此守护。',
    detail:
      '<p>白夜与黑暗交织的命运里，真相与爱都显得格外隐忍。</p><p>一场跨越时空的追寻，见证人性的复杂与光亮。</p>'
  },
  {
    title: '人类简史',
    cover: '/covers/renleijianshi.jpg',
    fallback: '/covers/cover-6.svg',
    price: 68,
    stock: 64,
    rate: 9.2,
    author: '尤瓦尔·赫拉利',
    publisher: '中信出版社',
    publishedAt: '2014-11',
    description: '从认知革命到科技革命，重构人类发展图景。',
    detail:
      '<p>作者以宏大的历史视角讲述人类如何塑造世界。</p><p>用清晰的逻辑与故事，连接过去与未来的关键节点。</p>'
  },
  {
    title: '月亮与六便士',
    cover: '/covers/yueliang.jpg',
    fallback: '/covers/cover-7.svg',
    price: 42,
    stock: 94,
    rate: 8.7,
    author: '毛姆',
    publisher: '上海译文出版社',
    publishedAt: '2006-03',
    description: '一个人舍弃安稳，追逐灵魂深处的自由。',
    detail:
      '<p>理性与欲望、现实与理想在主人公身上激烈碰撞。</p><p>“月亮”象征梦想，“六便士”代表现实的诱惑。</p>'
  },
  {
    title: '平凡的世界',
    cover: '/covers/pingfan.jpg',
    fallback: '/covers/cover-8.svg',
    price: 88,
    stock: 58,
    rate: 9.0,
    author: '路遥',
    publisher: '人民文学出版社',
    publishedAt: '2005-01',
    description: '普通人在大时代中的奋斗与坚守。',
    detail:
      '<p>孙少安与孙少平在城乡之间寻找人生的出路。</p><p>平凡生活里有不平凡的梦想与力量。</p>'
  },
  {
    title: '三国演义',
    cover: '/covers/sanguo.jpg',
    fallback: '/covers/cover-2.svg',
    price: 79,
    stock: 80,
    rate: 8.6,
    author: '罗贯中',
    publisher: '人民文学出版社',
    publishedAt: '1998-09',
    description: '群雄逐鹿的历史风云与英雄传奇。',
    detail:
      '<p>从桃园结义到三分归晋，权谋与义气交织成史诗。</p><p>经典人物与战争场面，塑造了中国古典文学的高峰。</p>'
  }
]

const CATALOG_MAP_KEY = 'tomatoCatalogMapV1'

const hashString = (input) => {
  let hash = 0
  for (let i = 0; i < input.length; i += 1) {
    hash = (hash << 5) - hash + input.charCodeAt(i)
    hash |= 0
  }
  return hash
}

const getBookIndex = (product, fallbackIndex = 0) => {
  const rawId = product?.id ?? product?.productId ?? product?.cartItemId ?? fallbackIndex
  if (typeof rawId === 'number' && Number.isFinite(rawId)) {
    return Math.abs(rawId) % bookCatalog.length
  }
  if (typeof rawId === 'string' && rawId.trim()) {
    return Math.abs(hashString(rawId)) % bookCatalog.length
  }
  const safeIndex = Number.isFinite(Number(fallbackIndex)) ? Number(fallbackIndex) : 0
  return Math.abs(safeIndex) % bookCatalog.length
}

const getProductKey = (product, fallbackIndex = 0) =>
  product?.productId ?? product?.id ?? product?.cartItemId ?? fallbackIndex

const getCatalogMapping = () => {
  if (typeof localStorage === 'undefined') {
    return {}
  }
  try {
    const raw = localStorage.getItem(CATALOG_MAP_KEY)
    return raw ? JSON.parse(raw) : {}
  } catch (error) {
    return {}
  }
}

const setCatalogMapping = (products = [], options = {}) => {
  if (!Array.isArray(products) || products.length === 0 || typeof localStorage === 'undefined') {
    return {}
  }
  const mapping = options.overwrite ? {} : getCatalogMapping()
  const usedIndexes = new Set(
    Object.values(mapping)
      .map(value => Number(value))
      .filter(value => Number.isFinite(value))
  )
  let nextIndex = 0
  let changed = false

  products.forEach((product, index) => {
    const key = getProductKey(product, index)
    if (key === undefined || key === null || mapping[key] !== undefined) {
      return
    }
    while (usedIndexes.size < bookCatalog.length && usedIndexes.has(nextIndex)) {
      nextIndex = (nextIndex + 1) % bookCatalog.length
    }
    mapping[key] = nextIndex
    usedIndexes.add(nextIndex)
    changed = true
    nextIndex = (nextIndex + 1) % bookCatalog.length
  })

  if (options.overwrite || changed) {
    localStorage.setItem(CATALOG_MAP_KEY, JSON.stringify(mapping))
  }
  return mapping
}

const getCatalogIndex = (product, index = 0) => {
  const directIndex = Number(product?.catalogIndex)
  if (Number.isFinite(directIndex)) {
    return directIndex
  }
  const key = getProductKey(product, index)
  const mapping = getCatalogMapping()
  const mappedIndex = mapping?.[key]
  if (Number.isFinite(Number(mappedIndex))) {
    return Number(mappedIndex)
  }
  return getBookIndex(product, index)
}

const getCatalogEntry = (product, index = 0) => bookCatalog[getCatalogIndex(product, index)]
const getCatalogEntryByIndex = (index = 0) => bookCatalog[Number(index)] || null

const setCatalogMappingEntry = (productId, catalogIndex) => {
  if (typeof localStorage === 'undefined' || !productId) {
    return
  }
  const mapping = getCatalogMapping()
  mapping[productId] = Number(catalogIndex)
  localStorage.setItem(CATALOG_MAP_KEY, JSON.stringify(mapping))
}

const getDisplayTitle = (product, index = 0) => {
  const entry = getCatalogEntry(product, index)
  return entry?.title || '图书'
}

const getDisplayCover = (product, index = 0) => {
  const entry = getCatalogEntry(product, index)
  return entry?.cover || entry?.fallback || '/covers/cover-1.svg'
}

const getFallbackCover = (product, index = 0) => {
  const entry = getCatalogEntry(product, index)
  return entry?.fallback || '/covers/cover-1.svg'
}

const getDisplayPrice = (product, index = 0) => {
  const entry = getCatalogEntry(product, index)
  return Number(entry?.price || 0)
}

const getDisplayStock = (product, index = 0) => {
  const entry = getCatalogEntry(product, index)
  return Number(entry?.stock || 0)
}

const getDisplayRate = (product, index = 0) => {
  const entry = getCatalogEntry(product, index)
  return Number(entry?.rate || product?.rate || 0)
}

const getDisplayDescription = (product, index = 0) => {
  const entry = getCatalogEntry(product, index)
  return entry?.description || ''
}

const getDisplayDetail = (product, index = 0) => {
  const entry = getCatalogEntry(product, index)
  return entry?.detail || ''
}

const getDisplaySpecs = (product, index = 0) => {
  const entry = getCatalogEntry(product, index)
  if (!entry) {
    return []
  }
  return [
    { item: '作者', value: entry.author || '—' },
    { item: '出版社', value: entry.publisher || '—' },
    { item: '出版日期', value: entry.publishedAt || '—' }
  ]
}

export {
  bookCatalog,
  getCatalogIndex,
  getCatalogEntryByIndex,
  setCatalogMappingEntry,
  setCatalogMapping,
  getDisplayTitle,
  getDisplayCover,
  getFallbackCover,
  getDisplayPrice,
  getDisplayStock,
  getDisplayRate,
  getDisplayDescription,
  getDisplayDetail,
  getDisplaySpecs
}
