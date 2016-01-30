const config = {}

export default {
  set: jest.genMockFunction().mockImplementation((key, value) => config[key] = value),
  get: jest.genMockFunction().mockImplementation((key) => config[key])
}
