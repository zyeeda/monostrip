{mark} = require 'coala/mark'
objects = require 'coala/util/objects'
{Date, Locale} = java.util
{Integer, Boolean, Double, Float} = java.lang
{Default} = javax.validation.groups
{FieldUtils} = org.apache.commons.lang.reflect
{ArrayUtils} = org.apache.commons.lang
{AnnotationUtils} = org.springframework.core.annotation

exports.createValidator = ->
    validate: mark('beans', 'validatorFactory').on (validatorFactory, validationContext, entity, group) ->
        validator = validatorFactory.getValidator()
        constraintViolations = if group? then validator.validate entity, Default, group else validator.validate entity, Default
        it = constraintViolations.iterator()
        while it.hasNext()
            v = it.next()
            properties = v.propertyPath.toString()
            properties = v.getConstraintDescriptor().getAttributes().get('bindingProperties') if properties is ''

            validationContext.addViolation properties: properties

exports.buildValidateRules = (fields, entityClass, group) ->
        rules = {}
        return rules: rules unless fields
        for f in fields
            name = f; annos = []
            if f instanceof Object
                name = f.name
                rules[name] = objects.extend {}, f.validations.rules if f.validations and f.validations.rules
            rules[name] = {} unless rules[name]
            field = FieldUtils.getField entityClass, name, true
            continue if not field
            if field.type == Date
                rules[name].date = true
            else if field.type == Integer or 'int'.equals field.type
                rules[name].digits = true
            else if field.type == Double or 'double'.equals field.type or field.type == Float or 'float'.equals field.type
                rules[name].number= true
            annos = field.annotations
            upperCaseName = name.substring(0, 1).toUpperCase() + name.substring 1, name.length
            try
                m = entityClass.getMethod 'get' + upperCaseName
            catch e
                try
                    m = entityClass.getMethod 'is' + upperCaseName
                catch ex
                    continue
                    throw new Error "property #{name} is not found"
            annos2 = m.annotations
            newAnnos = annos.concat annos2
            for a in newAnnos
                map = AnnotationUtils.getAnnotationAttributes a
                groups = map.get 'groups'
                continue unless groups
                if groups.length == 0 or ArrayUtils.contains groups, group
                    if a instanceof javax.validation.constraints.NotNull or a instanceof org.hibernate.validator.constraints.NotBlank
                        rules[name].required = true
                        f.required = true
                    else if a instanceof java.validator.constraints.Digits
                        rules[name].digits = true
                    else if a instanceof org.hibernate.validator.constraints.Email
                        rules[name].email = true
                    else if a instanceof org.hibernate.validator.constraints.URL
                        rules[name].url = true
                    else if a instanceof org.hibernate.validator.constraints.CreditCardNumber
                        rules[name].creditcard = true
                    else if a instanceof org.hibernate.validator.constraints.Range
                        rules[name].range = [map.get('min'), map.get('max')]
                    else if a instanceof com.zyeeda.coala.validation.constraint.NullableSize
                        rules[name].rangelength = [map.get('min'), map.get('max')]
                    else if a instanceof javax.validation.constraints.DecimalMax or a instanceof javax.validation.constraints.constraints.Max
                        rules[name].max = map.get 'value'
                    else if a instanceof javax.validation.constraints.DecimalMin or a instanceof javax.validation.constraints.Min
                        rules[name].min = map.get 'value'
                    else if a instanceof com.zyeeda.coala.validation.constraint.Matches
                        rules[name].equalTo = a.target()

            classAnnos = entityClass.annotations
            for anno in classAnnos
                if anno instanceof com.zyeeda.coala.validation.constraint.Matches
                        name = anno.target()
                        rules[name] = {} unless rules[name]
                        rules[name].equalTo = anno.source()
        rules: rules
