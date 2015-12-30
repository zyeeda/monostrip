import isPromise from 'redux-promise-middleware/dist/isPromise'

const executeTransitionHook = ({components, getState, dispatch, location, params, methodName}) => {
  return components
    .filter(component => component && component[methodName])
    .map(component => component[methodName])
    .map(method => method({getState, dispatch, location, params}))
    .filter(action => isPromise(action.payload))
    .map(action => action.payload.promise)
}

export function executeBeforeTransitionHook(options) {
  return executeTransitionHook({
    ...options,
    methodName: '__beforeTransition'
  })
}

export function executeAfterTransitionHook(options) {
  return executeTransitionHook({
    ...options,
    methodName: '__afterTransition'
  })
}
