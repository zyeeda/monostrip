import path from 'path'

import fs from 'fs-plus'
import Router from 'koa-router'

import config from '../../config'
import logger from '../../logger'

export default (app) => {
  logger.info('loading hook %s...', path.basename(__filename, '.js'))

  const router = new Router()
  const routerRootPath = path.join(config.get('appPath'), 'routers')
  const re = new RegExp(`^${routerRootPath}/(.*).js`)

  router.use(function* setResponseMimeType(next) {
    logger.info('set response MIME type to json')
    this.type = 'json'
    yield* next
  })

  fs
    .listTreeSync(routerRootPath)
    .filter(filePath => fs.isFileSync(filePath) && path.extname(filePath) === '.js')
    .map(filePath => filePath.match(re)[1])
    .forEach(name => {
      logger.debug(`mount router: /${name}`)
      router.use(`/${name}`, require(`${routerRootPath}/${name}`).default.routes())
    })

  app.use(router.routes())
  app.use(router.allowedMethods())
}
