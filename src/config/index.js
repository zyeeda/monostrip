import path from 'path'

import {Provider} from 'nconf/lib/nconf/provider'

const environment = process.env.NODE_ENV || 'development',
      appPath = process.cwd(),
      nconf = new Provider()

nconf
  .env()
  .argv()
  .file('app_env', { file: path.join(appPath, 'config', environment + '.json')})
  .file('sys_env', { file: path.join(__dirname, environment + '.json')})
  .file('app_default', { file: path.join(appPath, 'config', 'default.json')})
  .file('sys_default', { file: path.join(__dirname, 'default.json')})

nconf.set('sysPath', path.join(__dirname, '..', '..'))
nconf.set('appPath', appPath)
nconf.set('environment', environment)

cdeio.config = nconf

export default nconf
