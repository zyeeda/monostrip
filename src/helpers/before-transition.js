import React, {Component} from 'react'

export default (hook) => {
  return (WrappedComponent) => {
    class BeforeTransitionDecorator extends Component {
      render() {
        return <WrappedComponent {...this.props} />
      }
    }

    BeforeTransitionDecorator.__beforeTransition = hook

    return BeforeTransitionDecorator
  }
}
