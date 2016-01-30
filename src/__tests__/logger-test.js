jest.dontMock('../logger')

describe('logger', () => {
  it('should have stdout stream with debug level enabled in test mode', () => {
    const bunyan = require('bunyan')
    const config = require('../config').default

    config.set('name', 'CDE.IO [TEST]')
    config.set('log:level', 'debug')
    config.set('environment', 'test')

    require('../logger')

    expect(config.get.mock.calls.length).toBe(6)
    expect(config.get.mock.calls[0]).toEqual(['appPath'])
    expect(config.get.mock.calls[1]).toEqual(['name'])
    expect(config.get.mock.calls[2]).toEqual(['environment'])
    expect(config.get.mock.calls[3]).toEqual(['log:level'])
    expect(config.get.mock.calls[4]).toEqual(['environment'])
    expect(config.get.mock.calls[5]).toEqual(['name'])

    expect(bunyan.createLogger).toBeCalled()
    const options = bunyan.createLogger.mock.calls[0/*first call*/][0/* first argument*/]
    expect(options.src).toBe(true)
    expect(options.name).toBe('CDE.IO [TEST]')
    expect(options.streams[0].level).toBe('debug')
  })

  it('should have file and stdout streams with info level enabled in production model', () => {
    process.env.NODE_ENV = 'production'

    const bunyan = require('bunyan')
    const fs = require('fs-plus').default
    const config = require('../config').default

    config.set('name', 'CDE.IO [PROD]')
    config.set('log:level', 'info')
    config.set('environment', 'production')

    require('../logger')

    expect(config.get.mock.calls.length).toBe(7)
    expect(config.get.mock.calls[0]).toEqual(['appPath'])
    expect(config.get.mock.calls[1]).toEqual(['name'])
    expect(config.get.mock.calls[2]).toEqual(['environment'])
    expect(config.get.mock.calls[3]).toEqual(['log:level'])
    expect(config.get.mock.calls[4]).toEqual(['log:level'])
    expect(config.get.mock.calls[5]).toEqual(['environment'])
    expect(config.get.mock.calls[6]).toEqual(['name'])

    const logFile = '/path/to/app/logs/CDE.IO [PROD].log'

    expect(fs.existsSync).toBeCalledWith(logFile)
    expect(fs.writeFileSync).toBeCalledWith(logFile, '')

    expect(bunyan.createLogger).toBeCalled()
    const options = bunyan.createLogger.mock.calls[0][0]
    expect(options.src).toBe(false)
    expect(options.name).toBe('CDE.IO [PROD]')
    expect(options.streams[0].level).toBe('info')
    expect(options.streams[0].path).toBe(logFile)
    expect(options.streams[1].level).toBe('info')
  })
})
