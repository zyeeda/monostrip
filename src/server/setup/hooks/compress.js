import compress from 'koa-compress'

export default (app) => {
  app.use(compress())
}
