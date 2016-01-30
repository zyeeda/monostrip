import path from 'path'

import isFunction from 'lodash.isfunction'
import values from 'lodash.values'
import fs from 'fs-plus'
import Sequelize from 'sequelize'

import config from '../config'

const sequelize = new Sequelize(
  config.get('db:database'),
  config.get('db:username'),
  config.get('db:password'),
  config.get('db:options')
)

const modelPaths = [
  path.join(config.get('sysPath'), 'lib', 'models'),
  path.join(config.get('appPath'), 'models')
]

const models = modelPaths
  .map(modelPath => fs.listTreeSync(modelPath))
  .reduce((prev, current) => prev.concat(current), [])
  .filter(filePath => fs.isFileSync(filePath) && path.extname(filePath) === '.js')
  .map(filePath => sequelize.import(filePath))
  .reduce((models, model) => {
    models[model.name] = model
    return models
  }, {})

values(models)
  .filter(model => isFunction(model.associate))
  .forEach(model => model.associate(models))

export {sequelize, models}
