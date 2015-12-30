import {ROUTER_DID_CHANGE} from 'redux-router/lib/constants'
import {executeBeforeTransitionHook, executeAfterTransitionHook} from '../../helpers/execute-transition-hook'

const locationsAreEqual = (location1, location2) => {
  return (location1.pathname === location2.pathname) && (location1.search === location2.search)
}

export default ({getState, dispatch}) => next => action => {
  if (action.type !== ROUTER_DID_CHANGE) return next(action)

  if (getState().router && locationsAreEqual(action.payload.location, getState().router.location)) {
    return next(action)
  }

  const {components, location, params} = action.payload
  const promise = new Promise((resolve, reject) => {
    const options = {
      components,
      getState,
      dispatch,
      location,
      params
    }

    const doTransition = () => {
      next(action)
      Promise.all(executeAfterTransitionHook(options))
        .then(resolve)
        .catch(err => {
          console.warn('Error in after transition hook', err) // eslint-disable-line no-console
          reject(err)
        })
    }

    Promise.all(executeBeforeTransitionHook(options))
      .then(doTransition)
      .catch(err => {
        console.warn('Error in before transition hook', err) // eslint-disable-line no-console
        return doTransition()
      })
  })

  if (!__CLIENT__) getState().router = promise

  return promise
}
