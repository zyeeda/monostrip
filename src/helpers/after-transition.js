import React, {Component} from 'react'

export default (hook) => {
  return (WrappedComponent) => {
    class AfterTransitionDecorator extends Component {
      render() {
        return <WrappedComponent {...this.props} />
      }
    }

    AfterTransitionDecorator.__afterTransition = hook

    return AfterTransitionDecorator
  }
}
