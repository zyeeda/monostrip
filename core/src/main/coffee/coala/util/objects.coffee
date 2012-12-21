exports.extend = (target = {}, mixins...) ->
    for mixin in mixins
        target[key] = value for key, value of mixin
    target
