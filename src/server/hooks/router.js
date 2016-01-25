import path from 'path'

import fs from 'fs-plus'
import Router from 'koa-router'

import config from '../../config'

export default (app) => {
  const router = new Router(),
        routerRootPath = path.join(config.get('appPath'), 'routers'),
        re = new RegExp(`^${routerRootPath}/(.*).js`)

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
