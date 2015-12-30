const baseConfig = {
  web: {
    host: process.env.HOST || 'localhost',
    port: parseInt(process.env.PORT, 10) || 4000,
    apiServerPrefix: '/api'
  },
  api: {
    host: process.env.HOST || 'localhost',
    port: parseInt(process.env.PORT, 10) + 1 || 4100,
    db: {
      database: 'cdeio-samples',
      username: 'root',
      password: 'mysecretpassword',
      options: {
        host: 'mysql',
        dialect: 'mysql',
        pool: {
          port: 3306,
          max: 15,
          min: 0,
          idle: 10000
        }
      }
    }
  },
  log: {
    level: 'debug'
  }
}

const specific = {
  development: {
    ...baseConfig,
    web: {
      ...baseConfig.web,
      name: 'CDE.IO Web [DEV]'
    },
    api: {
      ...baseConfig.api,
      name: 'CDE.IO API [DEV]'
    }
  },

  production: {
    ...baseConfig,
    web: {
      ...baseConfig.web,
      name: 'CDE.IO Web'
    },
    api: {
      ...baseConfig.api,
      name: 'CDE.IO API'
    },
    log: {
      level: 'debug'
    }
  }
}

export default specific[__DEVELOPMENT__ ? 'development' : 'production']
