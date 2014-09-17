

{TransactionTemplate,DefaultTransactionDefinition,TransactionCallback} = org.springframework.transaction.support
{type} = require 'cdeio/util/type'
objects = require 'cdeio/util/objects'
{Context} = com.zyeeda.cdeio.web.SpringAwareJsgiServlet;

###
see org.springframework.transaction.TransactionDefinition
PROPAGATION_REQUIRED = 0;
PROPAGATION_SUPPORTS = 1;
PROPAGATION_MANDATORY = 2;
PROPAGATION_REQUIRES_NEW = 3;
PROPAGATION_NOT_SUPPORTED = 4;
PROPAGATION_NEVER = 5;
PROPAGATION_NESTED = 6;

ISOLATION_DEFAULT = -1;
ISOLATION_READ_UNCOMMITTED = 1;
ISOLATION_READ_COMMITTED = 2;
ISOLATION_REPEATABLE_READ = 4;
ISOLATION_SERIALIZABLE = 8;
###

defaults =
    readOnly: false,
    name: 'transaction thread'
    propagationBehavior: 0,
    isolationLevel: -1,
    timeout: -1

createTransactionDefinition = (option = {}) ->
    definition = new DefaultTransactionDefinition()
    c = objects.extend defaults, option
    definition[name] = value for name, value of c
    definition

exports.tx = (callback) ->
    context = Context.getInstance(module)
    cb = if type(callback) is 'object' then callback else {}
    callback = callback['callback'] if type(callback) is 'object'
    throw new Error('no callback function supplied') unless type(callback) is 'function'

    delete cb[key] for key of cb when key not of defaults

    tm = context.getBean 'transactionManager'
    definition = createTransactionDefinition cb
    template = new TransactionTemplate tm, definition

    template.execute new TransactionCallback
        doInTransaction: (status) ->
            return callback status
