const config = {
  name: 'CDE.IO API',
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
  },
  log: {
    level: 'error'
  }
}

export default config
