import path from 'path'

import locale from 'koa-locale'
import i18n from 'koa-i18n'

export default (app) => {
  const options = {
    directory: path.join(__dirname, '..', '..', '..', 'locales'),
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
  app.use(i18n(app, options))
}
