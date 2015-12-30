// Enable ES6/7 features.

var fs = require('fs')

var config = {}
try {
  config = JSON.parse(fs.readFileSync('./.babelrc'))
} catch (err) {
  console.error('Error parsing .babelrc file.')
  console.error(err)
}

require('babel-core/register')(config)
