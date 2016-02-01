import path from 'path'

jest.dontMock('../models')

// mock fs-plus
const _files = ['account.js', 'group.js', 'project/task.js', 'project/phase.js']
let _start = 0
const fsMock = {
  listTreeSync: jest.genMockFunction().mockImplementation(
    filePath => [`${filePath}/${_files[_start++]}`, `${filePath}/${_files[_start++]}`]),
  isFileSync: jest.genMockFunction().mockReturnValue(true)
}
jest.setMock('fs-plus', fsMock)

// mock sequelize
const SequelizeMock = jest.genMockFunction()
SequelizeMock.prototype.import = jest.genMockFunction().mockImplementation(filePath => ({
  name: path.basename(filePath, '.js'),
  associate: jest.genMockFunction()
}))
jest.setMock('sequelize', SequelizeMock)

// test case
describe('models', () => {
  it('should import dedicated models', () => {
    const fs = require('fs-plus')
    const sequelize = require('sequelize')
    const config = require('../../config').default

    config.set('db:database', 'test-database')
    config.set('db:username', 'test-user')
    config.set('db:password', 'test-password')
    config.set('db:options', {})

    const {sequelize: sequelizeInstance, models} = require('../models')

    expect(config.get.mock.calls.length).toBe(6)

    expect(fs.listTreeSync.mock.calls.length).toBe(2)
    expect(fs.listTreeSync.mock.calls[0][0]).toBe('/path/to/sys/lib/models')
    expect(fs.listTreeSync.mock.calls[1][0]).toBe('/path/to/app/models')
    expect(fs.isFileSync.mock.calls.length).toBe(4)
    expect(fs.isFileSync.mock.calls[0][0]).toBe('/path/to/sys/lib/models/account.js')
    expect(fs.isFileSync.mock.calls[1][0]).toBe('/path/to/sys/lib/models/group.js')
    expect(fs.isFileSync.mock.calls[2][0]).toBe('/path/to/app/models/project/task.js')
    expect(fs.isFileSync.mock.calls[3][0]).toBe('/path/to/app/models/project/phase.js')

    expect(sequelize).toBeCalledWith('test-database', 'test-user', 'test-password', {})
    expect(sequelize.mock.instances[0]).toBe(sequelizeInstance)
    expect(sequelizeInstance.import.mock.calls.length).toBe(4)

    const values = require('lodash.values')
    values(models).forEach(model => {
      expect(model.associate).toBeCalledWith(models)
    })
  })
})
