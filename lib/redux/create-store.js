import {combineReducers, compose, applyMiddleware, createStore} from 'redux'
import agentMiddleware from './middleware/agent-middleware'
import promiseMiddleware from 'redux-promise-middleware'
import transitionMiddleware from './middleware/transition-middleware'

export default ({reduxReactRouter, getRoutes, createHistory, agent, initialState}) => {
  const middleware = [agentMiddleware(agent), promiseMiddleware(), transitionMiddleware]

  if (__DEVELOPMENT__ && __CLIENT__) {
    const createLogger = require('redux-logger')
    const loggerMiddleware = createLogger()
    middleware.push(loggerMiddleware)
  }

  let finalCreateStore

  if (__DEVELOPMENT__ && __CLIENT__ && __DEVTOOLS__) {
    const {persistState} = require('redux-devtools')
    const DevTools = require('../containers/dev-tools')
    finalCreateStore = compose(
      applyMiddleware(...middleware),
      window.devToolsExtension ? window.devToolsExtension() : DevTools.instrument(),
      persistState(window.location.href.match(/[?&]debug_session=([^&]+)\b/))
    )(createStore)
  } else {
    finalCreateStore = applyMiddleware(...middleware)(createStore)
  }

  finalCreateStore = reduxReactRouter({getRoutes, createHistory})(finalCreateStore)

  const ducks = require('./ducks')
  const rootReducer = combineReducers(ducks)
  const store = finalCreateStore(rootReducer, initialState)

  if (__DEVELOPMENT__ && module.hot) {
    module.hot.accept('./ducks', () => {
      store.replaceReducer(require('./ducks'))
    })
  }

  return store
}
