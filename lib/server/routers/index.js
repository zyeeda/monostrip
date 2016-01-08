import path from 'path'

import fs from 'fs-plus'
import Router from 'koa-router'

const router = new Router()

const basename = path.basename(module.filename, '.js')

router.use(function* setDefaultResponseMimeType(next) {
  this.type = 'json'
  yield next
})

const re = new RegExp(`^${__dirname}/(.*[^${basename}]).js$`)

fs
  .listTreeSync(__dirname)
  .filter(filePath => fs.isFileSync(filePath))
  .filter(filePath => re.test(filePath))
  .map(filePath => filePath.match(re)[1])
  .forEach(name => router.use(`/${name}`, require(`./${name}`).routes()))

export default router
