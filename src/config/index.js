import path from 'path'

import {Provider} from 'nconf/lib/nconf/provider'

const environment = process.env.NODE_ENV || 'development'
const appPath = process.cwd()
const nconf = new Provider()

nconf
  .argv()
  .env()
  .file('app_env', {file: path.join(appPath, 'config', `${environment}.json`)})
  .file('sys_env', {file: path.join(__dirname, `${environment}.json`)})
  .file('app_default', {file: path.join(appPath, 'config', 'default.json')})
  .file('sys_default', {file: path.join(__dirname, 'default.json')})

nconf.set('sysPath', path.join(__dirname, '..', '..'))
nconf.set('appPath', appPath)
nconf.set('environment', environment)

export default nconf
