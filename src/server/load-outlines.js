import path from 'path'

import fs from 'fs-plus'
import inflection from 'inflection'
import keys from 'lodash.keys'
import Router from 'koa-router'

import logger from '../logger'
import models from './load-models'
import sketches from './sketches'

const generateDefaultRouter = () => {
  const router = new Router()

  router.get('/', function* list() {
    this.body = [{
      id: '1',
      username: 'tom',
      name: 'Tom',
      age: '19',
      gender: 'M'
    }, {
      id: '2',
      username: 'mary',
      name: 'Many',
      age: '21',
      gender: 'F'
    }]
  })

  router.get('/:id', function* getById() {
    this.body = {
      id: '1',
      username: 'tom',
      name: 'Tom',
      age: '19',
      gender: 'M'
    }
  })

  router.post('/', function* add() {
    this.body = {
      id: '3',
      username: 'peter',
      name: 'Peter',
      age: '25',
      gender: 'M'
    }
  })

  router.post('/:id', function* updateById() {
    this.body = {
      id: '3',
      username: 'peter',
      name: 'Peter',
      age: '25',
      gender: 'M'
    }
  })

  router.delete('/:id', function* removeById() {
    this.body = {
      id: '3',
      username: 'peter',
      name: 'Peter',
      age: '25',
      gender: 'M'
    }
  })

  router.get('/:id', function* getById() {
    this.body = {
      id: '1',
      username: 'tom',
      name: 'Tom',
      age: '19',
      gender: 'M'
    }
  })

  return router
}

export default (app) => {
  const outlines = {}

  keys(sketches)
    .map(sketchPath => path.join(sketchPath, (sketches[sketchPath]['directories.outlines']) || 'lib/outlines'))
    .map(outlinePath => fs.listTreeSync(outlinePath))
    .reduce((prev, current) => prev.concat(current), [])
    .filter(filePath => fs.isFileSync(filePath) && path.extname(filePath) === '.js')
    .forEach(filePath => {
      const sketchName = path.basename(filePath, '.js')
      const outline = require(filePath).default
      outlines[sketchName] = outline

      const outlineRouters = new Router()

      logger.debug(`mount router: /${sketchName}`)
      outlineRouters.use(`/${inflection.pluralize(sketchName)}`,
        generateDefaultRouter(models[outline.modelName]).routes())

      app.use(outlineRouters.routes())
      app.use(outlineRouters.allowedMethods())
    })

  return outlines
}
