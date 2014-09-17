# support mark 'beans',
# attributes should be a single element or a collection of elements
# each element should be a bean name or a type(class)
# ex. :
# mark('beans', ['beanName', SomeService]).on(function(beanUnderNameBeanName, instanceOfSomeService){})

{type} = require 'cdeio/util/type'

exports.handler = (context, attributes, fn, args) ->
    beans = []
    beans.push((if type(name) == 'string' then context.getBean else context.getBeanByClass).call(context, name)) for name in attributes
    args = beans.concat args
    fn.apply null, args
