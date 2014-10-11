// Run complete RingoJS test suite.

exports.testFoo   = require('test-foo');
exports.testHello = require('hello/test-hello');


//start the test runner if we're called directly from command line
if (require.main == module.id) {
    require('test').run(exports);
    // require('system').exit(require('test').run(exports));
}
