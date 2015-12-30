import superagent from 'superagent'

import config from '../server/config'

class ApiAgent {
  constructor(req) {
    ['options', 'get', 'put', 'post', 'patch', 'del'].forEach((method) => {
      this[method] = (path, options = {}) => {
        return new Promise((resolve, reject) => {
          const request = superagent[method](this.formatUrl(path))

          if (options.params) request.query(options.params)
          if (!__CLIENT__ && req.get('cookie')) request.set('cookie', req.get('cookie'))
          if (options.data) request.send(options.data)
          request.end((err, res) => {
            if (err) {
              reject(err)
            } else {
              resolve(res.body)
            }
          })
        })
      }
    })
  }

  formatUrl(path) {
    if (path[0] !== '/') throw new Error('Request path must be started with /.')
  }
}

export class ClientAgent extends ApiAgent {
  constructor() {
    super()
  }

  formatUrl(path) {
    super.formatUrl(path)
    return config.web.apiServerPrefix + path
  }
}

export class ServerAgent extends ApiAgent {
  constructor(req) {
    super(req)
  }

  formatUrl(path) {
    super.formatUrl(path)
    return 'http://' + config.api.host + ':' + config.api.port + path
  }
}
