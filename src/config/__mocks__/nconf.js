const nconfMock = jest.genMockFromModule('nconf')
const config = {}

nconfMock.argv.mockImplementation().mockReturnThis()
nconfMock.env.mockImplementation().mockReturnThis()
nconfMock.file.mockImplementation().mockReturnThis()

nconfMock.set.mockImplementation((key, value) => {
  config[key] = value
})

nconfMock.get.mockImplementation((key) => {
  return config[key]
})

module.exports = nconfMock
