
###
 Value               Class      Type
-------------------------------------
"foo"               String     string
new String("foo")   String     object
1.2                 Number     number
new Number(1.2)     Number     object
true                Boolean    boolean
new Boolean(true)   Boolean    object
new Date()          Date       object
new Error()         Error      object
[1,2,3]             Array      object
new Array(1, 2, 3)  Array      object
new Function("")    Function   function
/abc/g              RegExp     object
new RegExp("meow")  RegExp     object
{}                  Object     object
new Object()        Object     object

type("")         # "string"
type(new String) # "string"
type([])         # "array"
type(/\d/)       # "regexp"
type(new Date)   # "date"
type(true)       # "boolean"
type(null)       # "null"
type({})         # "object"

###

classToType = {}
for name in "Boolean Number String Function Array Date RegExp Undefined Null".split(" ")
    classToType["[object " + name + "]"] = name.toLowerCase()

exports.type = (obj) ->
    strType = Object::toString.call(obj)
    classToType[strType] or "object"

exports.objects =
    extend: (target = {}, mixins...) ->
        for mixin in mixins
            target[key] = value for key, value of mixin
        target
