{mark} = require 'coala/mark'
{objects} = require 'coala/util'
{Date, Locale} = java.util
{Integer, Boolean, Double, Float} = java.lang
{Default} = javax.validation.groups
{FieldUtils} = org.apache.commons.lang.reflect
{ArrayUtils} = org.apache.commons.lang
{AnnotationUtils} = org.springframework.core.annotation

exports.createValidator = ->
    validate: mark('beans', 'validatorFactory').on (validatorFactory, validationContext, entity, group) ->
        validator = validatorFactory.getValidator()
        constraintViolations = if group? then validator.validate entity, Default, group else validator.validator entity, Default
        it = constraintViolations.iterator()
        while it.hasNext()
            v = it.next()
            properties = v.propertyPath.toString()
            properties = v.getConstraintDescriptor().getAttributes().get('bindingProperties') if properties is ''
            message = v.getMessage()

            validationContext.addViolation properties: properties, message: message

    buildValidateRules: mark('beans', 'messageSource').on (messageSource, fields, entityClass, group) ->
        rules = {}; messages = {}
        return rules: rules, messages: messages unless fields
        for f in fields
            name = f; annos = []
            if f instanceof Object
                name = f.name
                rules[name] = objects.extend {}, f.rules if f.rules
                messages[name] = objects.extend {}, f.messages if f.messages
            rules[name] = {} unless rules[name]
            messages[name] = {} unless messages[name]
            field = FieldUtils.getField entityClass, name, true
            continue if not field
            if field.type == Date
                rules[name].date = true
                messages[name].date = messageSource.getMessage 'com.zyeeda.framework.validator.constraints.Date.message', null, Locale.default
            else if field.type == Integer or 'int'.equals field.type
                rules[name].digits = true
                messages[name].digits = messageSource.getMessage 'com.zyeeda.framework.validator.constraints.Digits.message', null, Locale.default
            else if field.type == Double or 'double'.equals field.type or field.type == Float or 'float'.equals field.type
                rules[name].number= true
                messages[name].number = messageSource.getMessage 'com.zyeeda.framework.validator.constraints.Number.message', null, Locale.default
            annos = field.annotations
            upperCaseName = 'get' + name.substring(0, 1).toUpperCase() + name.substring 1, name.length
            m = entityClass.getMethod upperCaseName
            annos2 = m.annotations
            newAnnos = annos.concat annos2
            for a in newAnnos
                map = AnnotationUtils.getAnnotationAttributes a
                groups = map.get 'groups'
                continue unless groups
                messageExp = map.get 'message'
                messageKey = messageExp.substring(1, messageExp.length - 1)
                message = messageSource.getMessage messageKey, null, Locale.default
                if groups.length == 0 or ArrayUtils.contains groups, group
                    if a instanceof javax.validation.constraints.NotNull or a instanceof org.hibernate.validator.constraints.NotBlank
                        rules[name].required = true
                        messages[name].required = message
                    else if a instanceof java.validator.constraints.Digits
                        rules[name].digits = true
                        messages[name].digits = message
                    else if a instanceof org.hibernate.validator.constraints.Email
                        rules[name].email = true
                        messages[name].email = message
                    else if a instanceof org.hibernate.validator.constraints.URL
                        rules[name].url = true
                        messages[name].url = message
                    else if a instanceof org.hibernate.validator.constraints.CreditCardNumber
                        rules[name].creditcard = true
                        messages[name].creditcard = message
                    else if a instanceof org.hibernate.validator.constraints.Range
                        rules[name].range = [map.get('min'), map.get('max')]
                        message = message.replace '{min}', map.get('min')
                        message = message.replace '{max}', map.get('max')
                        messages[name].range = message
                    else if a instanceof org.hibernate.validator.constraints.Length
                        rules[name].rangelength = [map.get('min'), map.get('max')]
                        message = message.replace '{min}', map.get('min')
                        message = message.replace '{max}', map.get('max')
                        messages[name].rangelength = message
                    else if a instanceof javax.validation.constraints.DecimalMax or a instanceof javax.validation.constraints.constraints.Max
                        rules[name].max = map.get 'value'
                        message = message.replace '{value}', map.get('value')
                        messages[name].max = message
                    else if a instanceof javax.validation.constraints.DecimalMin or a instanceof javax.validation.constraints.Min
                        rules[name].min = map.get 'value'
                        message = message.replace '{value}', map.get('value')
                        messages[name].min = message
        rules: rules, messages: messages
