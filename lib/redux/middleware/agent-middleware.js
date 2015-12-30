export default (agent) => {
  return () => next => action => {
    const {type, payload, meta} = action

    // The payload is a function.
    if (typeof payload === 'function') {
      const promise = payload(agent)
      return next({
        type,
        payload: {promise},
        meta
      })
    }

    // The payload is an object, which contains withAgent method and data attribute.
    /* if (typeof payload === 'object') {
      const {withAgent, data} = payload
      if (typeof withAgent === 'function') {
        const promise = withAgent(agent)
        return next({
          type,
          payload: {promise, data},
          meta
        })
      }
    } */

    next(action)
  }
}
