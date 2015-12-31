import httpProxy from 'http-proxy'
import config from '../config'

export default (options) => {
  const opts = typeof options === 'string' ? {prefix: options} : options
  if (!opts.prefix) opts.prefix = '/'

  const proxyServer = httpProxy.createProxyServer({
    target: {
      host: config.api.host,
      port: config.api.port
    }
  })

  return function* delegate(next) {
    // this.log.debug('request path = %s', this.path)

    if (!this.path.startsWith(opts.prefix)) return yield *next

    this.path = this.path.slice(opts.prefix.length)
    // this.log.debug('sliced request path = %s', this.path)

    yield new Promise((resolve, reject) => {
      proxyServer.web(this.req, this.res, (err) => {
        if (err) reject(err)
      })
    })
  }
}
