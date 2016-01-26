import path from 'path'

import fs from 'fs-plus'
import Router from 'koa-router'

import config from '../../config'
import logger from '../../logger'

export default (app) => {
  logger.info(`Setup ${path.basename(__filename, '.js')} hook.`)

  const router = new Router()
  const routerRootPath = path.join(config.get('appPath'), 'routers')
  const re = new RegExp(`^${routerRootPath}/(.*).js`)

  router.use(function* setDefaultResponseMimeType(next) {
    this.type = 'json'
    yield next
  })

  fs
    .listTreeSync(routerRootPath)
    .filter(filePath => fs.isFileSync(filePath))
    .filter(filePath => path.extname(filePath) === '.js')
    .map(filePath => filePath.match(re)[1])
    .forEach(name => router.use(`/${name}`, require(`${routerRootPath}/${name}`).default.routes()))

  app.use(router.routes())
}
