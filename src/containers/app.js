import React from 'react'

export default class App extends React.Component {
  static propTypes = {
    children: React.PropTypes.object.isRequired
  }

  render() {
    return this.props.children
  }
}
