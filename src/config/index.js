import path from 'path'
import _ from 'underdash'

import fs from 'fs-plus'

const basename = path.basename(module.filename, '.js')

let [envType, dirPath, hookConfig, config, excludePath] = ['', '', {}, {}, []]

excludePath.push(path.join(__dirname, 'index.js'))

const environment = process.env.NODE_ENV || 'development'

if (environment === 'development') {
  envType = 'dev'
} else if (environment === 'production') {
  envType = 'prod'
} else if (environment === 'test') {
  envType = 'test'
}

fs
  .listTreeSync(path.join(__dirname, 'env', envType))
  .filter(filePath => fs.isFileSync(filePath))
  .filter(filePath => !_.contains(excludePath, filePath))
  .forEach(name => {
    dirPath = name.substr(0, (name.length - path.basename(name).length - 1))

    // hooks 目录
    if (dirPath === path.join(__dirname, 'env', envType, 'hooks')) {
      if ('index' === path.basename(name, '.js')) {
        config = _.extend({}, config, require(name).default)
      } else {
        hookConfig[path.basename(name, '.js')] = require(name).default

        config = _.extend({}, config, {hooksCfg: hookConfig})
      }
    } else {
      config = _.extend({}, config, require(name).default)
    }
  })

export default config
