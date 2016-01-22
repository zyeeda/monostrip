import path from 'path'

import bunyan from 'bunyan'
import fs from 'fs-plus'

import config from './config'

const streams = []
const logFile = path.join(config.get('appPath'), 'logs', `${config.get('name')}.log`)

let logger

if (config.get('environment') === 'production') {
  if (!fs.existsSync(logFile)) fs.writeFileSync(logFile, '')

  streams.push({
    path: logFile,
    level: config.get('log:level'),
  }, {
    level: config.get('log:level'),
    stream: process.stdout
  })
} else {
  streams.push({
    level: config.get('log:level'),
    stream: process.stdout
  })
}

const options = {
  src: config.get('environment')  === 'production' ? false : true,
  name: config.get('name'),
  streams,
  serializers: bunyan.stdSerializers
}

export default logger = cdeio.logger = bunyan.createLogger(options)
