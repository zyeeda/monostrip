/**
 * @fileOverview Middleware for transaction support.
 *
 * this installs a method 'tx' in the application object to config transaction attribute.
 */

var {TransactionTemplate,DefaultTransactionDefinition,TransactionCallback} = org.springframework.transaction.support;
var ctx = require('common/tx');

exports.middleware = function tx(next, app) {
    
    var config = {};
    
    app.tx = function(cfg) {
        config = cfg || {};
    };
    
    return function tx(request) {
        config.callback = function(status) {
            return next(request);
        };
        return ctx.execute(request,config);
    };
}