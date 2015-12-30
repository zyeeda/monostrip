import Router from 'koa-router'
import Chance from 'chance'

const router = new Router()
const chance = new Chance()

router.get('/init', function* initCounter() {
  const thunk = (callback) => {
    setTimeout(() => {
      callback(null, chance.integer({min: 100, max: 200}))
    }, 1000)
  }

  this.body = yield thunk
})

export default router
