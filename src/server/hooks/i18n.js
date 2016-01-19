import path from 'path'

import _ from 'underdash'
import locale from 'koa-locale'
import i18n from 'koa-i18n'

export default ({app, config}) => {
  const options = {
    directory: path.join(process.cwd(), 'locales'),
    locales: ['en', 'zh-CN'],
    extension: '.json',
    modes: [
      'header',
      'tld',
      'subdomain',
      'url',
      'cookie',
      'query'
    ]
  }

  locale(app)
  app.use(i18n(app, _.extend({}, config.hooksCfg['i18n'], options)))
}
