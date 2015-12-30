import 'babel-core/polyfill'

import React from 'react'
import ReactDOM from 'react-dom'
import createHistory from 'history/lib/createBrowserHistory'
import {Provider} from 'react-redux'
import {reduxReactRouter, ReduxRouter} from 'redux-router'

import {ClientAgent} from './helpers/api-agent'
import createStore from './redux/create-store'
import getRoutes from './get-routes'

const agent = new ClientAgent()

const store = createStore({
  reduxReactRouter,
  getRoutes,
  createHistory,
  agent,
  initialState: __INITIAL_STATE__ // eslint-disable-line no-undef
})

const component = (
  <ReduxRouter routes = {getRoutes(store)} />
)

const viewport = document.querySelector('#viewport')
ReactDOM.render((
  <Provider store={store} key="provider">
    {component}
  </Provider>
), viewport)

if (__DEVELOPMENT__) {
  window.React = React // enable debugger

  if (!viewport || !viewport.firstChild || !viewport.firstChild.attributes || !viewport.firstChild.attributes['data-react-checksum']) {
    console.error('Server-side React render was discarded. Make sure that your initial render does not contain any client-side code.') // eslint-disable-line no-console
  }
}

if (__DEVTOOLS__ && !window.devToolsExtension) {
  const DevTools = require('./containers/dev-tools')
  ReactDOM.render((
    <Provider store={store} key="provider">
      <div>
        {component}
        <DevTools />
      </div>
    </Provider>
  ), viewport)
}
