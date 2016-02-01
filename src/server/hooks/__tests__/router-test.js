import path from 'path'

jest.dontMock('../router')

jest.setMock('fs-plus', {
  listTreeSync: jest.genMockFunction().mockImplementation(filePath => [
    `${filePath}/hello.js`,
    `${filePath}/project/list`,
    `${filePath}/task/create.js`,
    `${filePath}/.DS_Store`
  ]),
  isFileSync: jest.genMockFunction().mockImplementation(filePath => path.extname(filePath) !== '')
})


describe('hooks', () => {
  describe('router', () => {
    it('should load routes from app path', () => {
      const fs = require('fs-plus')
      const Router = require('koa-router')
      const config = require('../../../config').default
      const hook = require('../router').default
      const helloRouter = require('../__mocks__/routers/hello').default
      const taskRouter = require('../__mocks__/routers/task/create').default

      config.set('appPath', path.join(__dirname, '..', '__mocks__'))

      const app = {
        use: jest.genMockFunction()
      }
      hook(app)

      expect(Router.mock.instances.length).toBe(1)
      const router = Router.mock.instances[0]
      expect(router.use.mock.calls.length).toBe(3)
      expect(router.use.mock.calls[1][0]).toBe('/hello')
      expect(router.use.mock.calls[2][0]).toBe('/task/create')
      expect(router.routes).toBeCalled()

      expect(fs.listTreeSync).toBeCalled()
      expect(helloRouter.routes).toBeCalled()
      expect(taskRouter.routes).toBeCalled()

      expect(app.use).toBeCalled()
    })
  })
})
