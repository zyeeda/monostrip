jest.setMock('nconf/lib/nconf/provider', require.requireActual('../__mocks__/nconf/lib/nconf/provider'))
jest.dontMock('..')

describe('config', () => {
  it('should fetch config from many sources', () => {
    const path = require.requireActual('path')

    const {Provider} = require('nconf/lib/nconf/provider')
    const config = require('..').default

    const appPath = process.cwd()

    expect(Provider.mock.instances[0]).toBe(config)

    expect(config.argv).toBeCalled()
    expect(config.env).toBeCalled()
    expect(config.file.mock.calls.length).toBe(4)
    expect(config.file.mock.calls[0]).toEqual(['app_env', {file: path.join(appPath, 'config', 'test.json')}])
    expect(config.file.mock.calls[1]).toEqual(['sys_env', {file: path.join(__dirname, '..', 'test.json')}])
    expect(config.file.mock.calls[2]).toEqual(['app_default', {file: path.join(appPath, 'config', 'default.json')}])
    expect(config.file.mock.calls[3]).toEqual(['sys_default', {file: path.join(__dirname, '..', 'default.json')}])

    expect(config.get('sysPath')).toBe(path.join(__dirname, '..', '..', '..'))
    expect(config.get('appPath')).toBe(config.get('sysPath'))
    expect(config.get('environment')).toBe('test')
  })
})
