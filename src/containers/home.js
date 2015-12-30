import React, {Component} from 'react'
import {connect} from 'react-redux'

import beforeTransition from '../helpers/before-transition'

import {init, increase, decrease} from '../redux/ducks/counter'
import Counter from '../components/counter'

@beforeTransition(({getState, dispatch}) => { // eslint-disable-line no-unused-vars
  return dispatch(init())
})
@connect(
  state => ({
    status: state.counter.status,
    counter: state.counter.value
  }),
  {init, increase, decrease}
)
export default class Home extends Component {
   static propTypes = {
     status: React.PropTypes.string.isRequired,
     counter: React.PropTypes.number.isRequired,
     init: React.PropTypes.func.isRequired,
     increase: React.PropTypes.func.isRequired,
     decrease: React.PropTypes.func.isRequired
   }

   static childContextTypes = {
     status: React.PropTypes.string.isRequired,
     counter: React.PropTypes.number.isRequired
   }

   getChildContext() {
     return {
       status: this.props.status,
       counter: this.props.counter
     }
   }

   handleInit(event) {
     event.preventDefault()
     this.props.init()
   }

   handleIncrease(event) {
     event.preventDefault()
     this.props.increase(1)
   }

   handleDecrease(event) {
     event.preventDefault()
     this.props.decrease(1)
   }

   render() {
     return (
       <div>
         <Counter
           onInit={this.handleInit.bind(this)}
           onIncrease={this.handleIncrease.bind(this)}
           onDecrease={this.handleDecrease.bind(this)}
         />
       </div>
     )
   }
}
