import path from 'path'

import fs from 'fs-plus'
import R from 'ramda'
import Sequelize from 'sequelize'

import config from '../config'

const sequelize = new Sequelize(config.get('db:database'), config.get('db:username'), config.get('db:password'), config.get('db:options')),
      modelPaths = [
        path.join(config.get('sysPath'), 'lib', 'models'),
        path.join(config.get('appPath'), 'models')
      ],
      models = {}

const importModels = (modelPath) => {
  fs
    .listTreeSync(modelPath)
    .filter(filePath => fs.isFileSync(filePath))
    .map(name => sequelize.import(name))
    .forEach(model => models[model.name] = model)
}

modelPaths.forEach((modelPath) => importModels(modelPath))

R
  .values(models)
  .filter((model) => R.is(Function, model.associate))
  .forEach((model) => model.associate(models))

models.sequelize = sequelize

export default cdeio.models = models
