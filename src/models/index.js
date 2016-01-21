import path from 'path'

import fs from 'fs-plus'
import R from 'ramda'
import Sequelize from 'sequelize'

import config from '../config'

const basename = path.basename(module.filename, '.js'),
      re = new RegExp(`^${__dirname}/(.*[^${basename}]).js$`),
      sequelize = new Sequelize(config.get('db:database'), config.get('db:username'), config.get('db:password'), config.get('db:options')),
      models = {}

fs
  .listTreeSync(__dirname)
  .filter(filePath => fs.isFileSync(filePath))
  .filter(filePath => re.test(filePath))
  .map(name => sequelize.import(name))
  .forEach(model => models[model.name] = model)

Object.keys(models).forEach(modelName => {
  const model = models[modelName]

  if (R.is(Function, model.associate)) model.associate(models)
})

models.sequelize = sequelize

export default models
