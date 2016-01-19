import path from 'path'

import bunyan from 'bunyan'
import fs from 'fs-plus'

import config from './config'

const [environment, rootDir] = [path.join(__dirname, '..'), process.env.NODE_ENV || 'development']
const logFile = path.join(rootDir, 'logs', `${config.name}.log`)

let streams = []

if (environment === 'development' || environment === 'test') {
  streams.push({
    level: config.log.level,
    stream: process.stdout
  })
} else {
  if (!fs.existsSync(logFile)) fs.writeFileSync(logFile, '')

  streams.push({
    path: logFile,
    level: config.log.level,
  }, {
    level: config.log.level,
    stream: process.stdout
  })
}

const options = {
  src: environment === 'development' || environment === 'test' ? true : false,
  name: config.name,
  streams,
  serializers: bunyan.stdSerializers
}

export default bunyan.createLogger(options)
