import path from 'path'

import keys from 'lodash.keys'
import fs from 'fs-plus'

import sketches from './sketches'

export default () => {
  const outlines = {}
  keys(sketches)
    .map(sketchPath => path.join(sketchPath, (sketches[sketchPath]['directories.outlines']) || 'lib/outlines'))
    .map(outlinePath => fs.listTreeSync(outlinePath))
    .reduce((prev, current) => prev.concat(current), [])
    .filter(filePath => fs.isFileSync(filePath) && path.extname(filePath) === '.js')
    .forEach(filePath => outlines[path.basename(filePath, '.js')] = require(filePath).default)

  return outlines
}
