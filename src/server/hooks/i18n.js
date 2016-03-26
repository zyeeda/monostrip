import path from 'path'

import merge from 'lodash.merge'
import locale from 'koa-locale'
import i18n from 'koa-i18n'

import config from '../../config'
import logger from '../../logger'

export default (app) => {
  logger.info('loading hook %s...', path.basename(__filename, '.js'))

  const options = {
    directory: path.join(config.get('appPath'), 'locales'),
  }

  locale(app)
  app.use(i18n(app, merge({}, options, config.get('hooks:i18n'))))
}
