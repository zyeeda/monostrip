const ProviderMock = jest.genMockFunction()

const config = {}

ProviderMock.prototype.argv = jest.genMockFunction().mockReturnThis()
ProviderMock.prototype.env = jest.genMockFunction().mockReturnThis()
ProviderMock.prototype.file = jest.genMockFunction().mockReturnThis()

ProviderMock.prototype.set =
  jest.genMockFunction().mockImplementation((key, value) => config[key] = value)

ProviderMock.prototype.get =
  jest.genMockFunction().mockImplementation((key) => config[key])

export {ProviderMock as Provider}
