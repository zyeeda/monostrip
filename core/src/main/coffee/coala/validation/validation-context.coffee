
exports.createValidationContext = ->

    violations: []

    isBeanValidationSkipped: false

    addViolation: (args...) ->
        @violations.push arg for arg in args
        this

    hasViolations: ->
        @violations.length > 0

    collectViolations: ->
        temp = @violations
        @violations = []
        temp

    skipBeanValidation: ->
        @isBeanValidationSkipped = true

