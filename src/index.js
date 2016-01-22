global.cdeio = {
  start: () => {
    require('./server/models')
    require('./server')
  }
}

module.exports = cdeio
