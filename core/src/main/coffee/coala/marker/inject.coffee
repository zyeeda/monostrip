# support mark 'inject',
# attributes should be a single element or a collection of elements
# each element should be a bean name or a type(class)
# ex. :
# mark('inject', ['beanName', SomeService]).on(function(beanUnderNameBeanName, instanceOfSomeService){})

{type} = require 'coala/util'

exports.handler = (context, attributes, fn, args) ->
   if attributes
       attr = if type(attributes) == 'array' then attributes else [attributes]
       beans = []
       beans.push((if type(name) == 'string' then context.getBean else context.getBeanByClass)(name)) for name in attr
       args = beans.concat args
   fn.apply null, args
