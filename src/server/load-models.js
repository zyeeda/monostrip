import path from 'path'

import isFunction from 'lodash.isfunction'
import keys from 'lodash.keys'
import values from 'lodash.values'
import fs from 'fs-plus'
import Sequelize from 'sequelize'

import config from '../config'
import sketches from './sketches'

const sequelize = new Sequelize(
  config.get('db:database'),
  config.get('db:username'),
  config.get('db:password'),
  config.get('db:options')
)

export default () => {
  const models = {sequelize}
  keys(sketches)
    .map(sketchPath => path.join(sketchPath, (sketches[sketchPath]['directories.models']) || 'lib/models'))
    .map(modelPath => fs.listTreeSync(modelPath))
    .reduce((prev, current) => prev.concat(current), [])
    .filter(filePath => fs.isFileSync(filePath) && path.extname(filePath) === '.js')
    .map(filePath => sequelize.import(filePath))
    .forEach(model => models[model.name] = model)

  values(models)
    .filter(model => isFunction(model.associate))
    .forEach(model => model.associate(models))

  return models
}
