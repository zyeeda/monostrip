const config = {
  appPath: '@@/path/to/app',
  name: `CDE.IO [${process.env.NODE_ENV === 'production' ? 'PROD' : 'TEST'}]`,
  'log:level': process.env.NODE_ENV === 'production' ? 'info' : 'debug',
  environment: process.env.NODE_ENV
}

export default {
  get: jest.genMockFunction().mockImplementation((key) => config[key])
}
