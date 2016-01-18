import path from 'path'

import _ from 'underdash'
import staticServe from 'koa-static'

export default ({app, config}) => {
  const options = {
    path: path.join(__dirname, '..', '..', '..', 'static')
  }

  app.use(staticServe(_.extend({}, config.hooksCfg['static']), options))
}
