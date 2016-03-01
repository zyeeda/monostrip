import path from 'path'

import fs from 'fs-plus'
import values from 'lodash.values'

import config from '../config'
import logger from '../logger'

const sketches = {}
const npmSketches = config.get('npmSketches') || []

if (npmSketches.length > 0) {
  // 扫描读取 npmSketches 所配置 sketch 目录中的 sketch.json 内容
  logger.info('npmSketches config is defined, lodding npmSketches...')

  npmSketches
    .map(sketchName => path.join(config.get('appPath'), 'node_modules', sketchName, 'sketch.json'))
    .forEach(sketchConfigPath => sketches[path.dirname(sketchConfigPath)] = require(sketchConfigPath))
} else {
  // 未配置 npmSketches，检索 appPath/node_modules 中带 sketch.json 的目录并读取 sketch.json 内容
  logger.info('npmSketches config is not defined, scanning appPath/node_modules with sketch.json file...')

  fs.listSync(path.join(config.get('appPath'), 'node_modules'))
    .filter(filePath => fs.isDirectorySync(filePath) && fs.existsSync(path.join(filePath, 'sketch.json')))
    .map(filePath => path.join(filePath, 'sketch.json'))
    .reduce((prev, current) => prev.concat(current), [])
    .forEach(sketchConfigPath => {console.log('projects.path = ', sketchConfigPath, ' content = ', require(sketchConfigPath));sketches[path.dirname(sketchConfigPath)] = require(sketchConfigPath)})
}

// 扫描 app 内部自定义 sketches 目录，并读取各 sketch 目录下 sketch.json 内容
logger.info('scanning appPath/sketches with sketch.json file...')

fs.listSync(path.join(config.get('appPath'), 'sketches'))
  .filter(filePath => fs.isDirectorySync(filePath) && fs.existsSync(path.join(filePath, 'sketch.json')))
  .map(filePath => path.join(filePath, 'sketch.json'))
  .reduce((prev, current) => prev.concat(current), [])
  .forEach(sketchConfigPath => {console.log('projects.path = ', sketchConfigPath, ' content = ', require(sketchConfigPath));sketches[path.dirname(sketchConfigPath)] = require(sketchConfigPath)})

export default sketches
