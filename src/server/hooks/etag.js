import fresh from 'koa-fresh'
import etag from 'koa-etag'

export default (app) => {
  app.use(fresh())
  app.use(etag())
}
