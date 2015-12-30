import React, {Component, PropTypes} from 'react'

export default class Counter extends Component {
  static propTypes = {
    onIncrease: PropTypes.func.isRequired,
    onDecrease: PropTypes.func.isRequired,
    onInit: PropTypes.func.isRequired
  }

  static contextTypes = {
    status: PropTypes.string.isRequired,
    counter: PropTypes.number.isRequired
  }

  render() {
    return (
      <div>
        <p>{this.context.status}</p>
        <p>{this.context.counter || 0}</p>
        <button className="btn btn-primary" onClick={this.props.onIncrease}>增加</button>
        <button className="btn btn-danger" onClick={this.props.onDecrease}>减少</button>
        <button className="btn btn-primary" onClick={this.props.onInit}>初始化</button>
      </div>
    )
  }
}
