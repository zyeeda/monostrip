import Router from 'koa-router'

const router = new Router()

router.get('/', function* sayHello() {
  this.body = {
    message: this.i18n.__('hello_world')
  }
})

export default router
