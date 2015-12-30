const INIT = '@@CDE.IO/COUNTER/INIT'
const INCREASE = '@@CDE.IO/COUNTER/INCREASE'
const DECREASE = '@@CDE.IO/COUNTER/DECREASE'

const initialState = {
  status: null,
  value: 0
}

export default function reducer(state = initialState, action = {}) {
  const {value} = state

  switch (action.type) {
    case `${INIT}_PENDING`:
      return {
        ...state,
        status: 'LOADING...'
      }
    case `${INIT}_FULFILLED`:
      return {
        status: 'LOADED',
        value: action.payload
      }
    case INCREASE:
      return {
        status: 'LOADED',
        value: value + action.payload
      }
    case DECREASE:
      return {
        status: 'LOADED',
        value: value - action.payload
      }
    default:
      return state
  }
}

export function init() {
  return {
    type: INIT,
    payload: (agent) => {
      return agent.get('/counter/init')
    }
  }
}

export function increase(step) {
  return {
    type: INCREASE,
    payload: step
  }
}

export function decrease(step) {
  return {
    type: DECREASE,
    payload: step
  }
}
