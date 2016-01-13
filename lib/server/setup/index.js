import path from 'path'

export default (app, config) => {
  if (config && config.hooks) {
    config.hooks
      .map(hookName => path.resolve(__dirname, 'hooks', hookName))
      .map(hookFileName => require(hookFileName).default)
      .forEach(hook => hook(app))
  }
}
