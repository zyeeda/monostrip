import bunyan from 'bunyan'
import path from 'path'
import fs from 'fs-plus'

import config from './config'

const {name: defaultName} = require('../../package.json')

export default (name = defaultName) => {
  const streams = []

  const rootDir = path.join(__dirname, '../..')
  const logFile = path.join(rootDir, 'logs', `${name}.log`)

  if (__DEVELOPMENT__) {
    streams.push({
      stream: process.stdout,
      level: config.log.level
    })
  } else {
    if (!fs.existsSync(logFile)) fs.writeFileSync(logFile)

    streams.push({
      type: 'rotating-file',
      path: logFile,
      period: '1d', // daily rotation
      count: 15,
      level: config.log.level
    })
  }

  const options = {
    src: __DEVELOPMENT__ ? true : false,
    name,
    streams,
    serializers: bunyan.stdSerializers
  }

  return bunyan.createLogger(options)
}
