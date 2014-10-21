exports.testHello = require('hello/hello');
exports.testMark  = require('mark/mark');

if (require.main == module.id) {
    require('test').run(exports);
}
