const { getDefaultConfig } = require('expo/metro-config')
const path = require('path')

// Direktori root monorepo (2 level di atas apps/student)
const projectRoot = __dirname
const monorepoRoot = path.resolve(projectRoot, '../..')

const config = getDefaultConfig(projectRoot)

// ─── Monorepo Support ────────────────────────────────────────────────────────
// Beri tahu Metro untuk mencari module di root monorepo juga
config.watchFolders = [monorepoRoot]

// Prioritaskan node_modules di dalam apps/student dulu,
// baru fallback ke root node_modules
config.resolver.nodeModulesPaths = [
  path.resolve(projectRoot, 'node_modules'),
  path.resolve(monorepoRoot, 'node_modules'),
]

module.exports = config
