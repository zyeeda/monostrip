import bodyParser from 'koa-bodyparser'
import _ from 'underdash'

export default ({app}) => {
  app.use(bodyParser())
}
