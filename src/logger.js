import path from 'path'

import bunyan from 'bunyan'
import fs from 'fs-plus'

import config from './config'

let [environment, streams] = [process.env.NODE_ENV || 'development', []]

const rootDir = path.join(__dirname, '..')
const logFile = path.join(rootDir, 'logs', `${config.name}.log`)

if (environment === 'development' || environment === 'test') {
  streams.push({
    level: 'trace',
    stream: process.stdout
  }, {
    level: config.log.level,
    stream: process.stderr
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
