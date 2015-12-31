import React from 'react'
import ReactDOM from 'react-dom/server'
import {Provider} from 'react-redux'
import {ReduxRouter} from 'redux-router'
import {reduxReactRouter, match} from 'redux-router/server'
import createHistory from 'history/lib/createMemoryHistory'
import koa from 'koa'
import PrettyError from 'pretty-error'
import qs from 'query-string'

import {ServerAgent} from '../../helpers/api-agent'
import getStatusFromRoutes from '../../helpers/get-status-from-routes'
import Html from '../../helpers/html'
import createStore from '../../redux/create-store'
import getRoutes from '../../get-routes'
import setup from '../setup'
import createLogger from '../logger'
import config from '../config'
import createProxy from './proxy'

const pretty = new PrettyError()

const app = koa()
setup(app, require('./config.json'))

const logger = app.logger = createLogger(config.web.key)

app.use(createProxy(config.web.apiServerPrefix))

const hydrate = (store, component) => {
  return ('<!doctype html>\n' +
    ReactDOM.renderToString(
      <Html
        assets={webpackIsomorphicTools.assets()}
        component={component}
        store={store}
      />
    )
  )
}

app.use(function* middleware() {
  if (__DEVELOPMENT__) {
    webpackIsomorphicTools.refresh()
  }

  const agent = new ServerAgent(this.request)
  const store = createStore({reduxReactRouter, getRoutes, createHistory, agent})

  logger.debug('__DISABLE_SSR__ = %s', __DISABLE_SSR__)
  if (__DISABLE_SSR__) {
    // no server-side rendering
    this.body = hydrate(store)
    return
  }

  logger.debug('original url = %s', this.originalUrl)
  logger.debug({state: store.getState()}, 'store.getState()')

  const matchThunk = (url) => (callback) => {
    store.dispatch(match(url, (err, redirectLocation, routerState) => {
      callback(null, {err, redirectLocation, routerState})
    }))
  }

  const {err, redirectLocation, routerState} = yield matchThunk(this.originalUrl)

  logger.debug({redirectLocation})
  logger.debug({routerState})

  if (err) {
    logger.error(pretty.render(err))
    this.status = 500
    this.body = hydrate(store)
    return
  }

  if (redirectLocation) {
    this.redirect(redirectLocation.pathname + redirectLocation.search)
    return
  }

  if (!routerState) {
    this.status = 500
    this.body = hydrate(store)
    return
  }

  if (routerState.location.search && !routerState.location.query) {
    routerState.location.query = qs.parse(routerState.location.search)
  }

  logger.debug({state: store.getState()}, 'store.getState() before yield')
  yield store.getState().router
  logger.debug({state: store.getState()}, 'store.getState() after yield')

  const component = (
    <Provider store={store} key="provider">
      <ReduxRouter />
    </Provider>
  )

  const status = getStatusFromRoutes(routerState.routes)
  logger.debug('redux-router matched status = %s', status)
  if (status) this.status = status
  this.body = hydrate(store, component)

  /* store.dispatch(match(this.originalUrl, (err, redirectLocation, routerState) => {
    if (err) {
      logger.error(err)
      this.status = 500
      this.body = hydrate(store)
      return
    }

    if (redirectLocation) {
      this.redirect(redirectLocation.pathname + redirectLocation.search)
      return
    }

    if (!routerState) {
      this.status = 500
      this.body = hydrate(store)
      return
    }

    // Workaround redux-router query string issue:
    // https://github.com/rackt/redux-router/issues/106
    if (routerState.location.search && !routerState.location.query) {
      routerState.location.query = qs.parse(routerState.location.search);
    }

    logger.debug({state: store.getState()}, 'store.getState()')
    yield (store.getState().router)
    store.getState().router.then(() => {
      logger.debug({state: store.getState()}, 'after store.getState().router resolved')
      const component = (
        <Provider store={store} key="provider">
          <ReduxRouter />
        </Provider>
      )

      const status = getStatusFromRoutes(routerState.routes)
      logger.debug('redux-router matched status = %s', status)
      if (status) this.status = status
      this.body = hydrate(store, component)
    }).catch((err) => {
      console.error('Error in before transition hook', pretty.render(err))
      this.status = 500
      this.body = hydrate(store)
    })
    console.log('*****')
  })) */
})

app.listen(config.web.port, (err) => {
  if (err) {
    logger.error(pretty.render(err))
    return
  }

  logger.info('%s server is listening on port %d...', config.web.name, config.web.port)
})
