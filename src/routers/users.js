import Router from 'koa-router'

const router = new Router()

router.get('/', function* getAllUsers() {
  this.body = [{
    id: '1',
    username: 'tom',
    name: 'Tom',
    age: '19',
    gender: 'M'
  }, {
    id: '2',
    username: 'mary',
    name: 'Many',
    age: '21',
    gender: 'F'
  }]
})

export default router
