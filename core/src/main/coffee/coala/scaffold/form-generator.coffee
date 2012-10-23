
###
tabs: [
    {title: 'tab title', groups: ['DEFAULT', 'Group1']}
]
groups:
    DEFAULT:
        label: null
        columns: 1
    groupName: 'Group Label'
fields: [
    'username'
    name: 'username', label: 'Username', colspan: 1, rowspan: 1, group: 'groupName', type: 'date|number|string|file|picker', pickerSource: 'string|key-value-pair'
]
###
{type, objects} = require 'coala/util'
{coala} = require 'coala/config'
{createValidator} = require 'coala/validation/validator'
{Add, Edit} = com.zyeeda.framework.validator.group

exports.generateForms = (meta, labels = {}, forms, formName) ->
    return null if not forms

    defaults = forms.defaults or {}
    form = forms[formName] or defaults

    generateForm form, meta, labels, formName

generateForm = (form, meta, labels, formName) ->
    groups = DEFAULT: {label: null, columns: 1}
    for name, value of form.groups or {}
        if value is null or type(value) is 'string'
            groups[name] = label: value, columns: 1
        else
            groups[name] = value

    result = {}
    result.groups = groups

    result.fields = []
    print 'meta', meta.type
    if meta.type is 'tree' or meta.type is 'treeTable'
        result.fields.push
            label: '父节点', name: 'parentName', value: 'parent.name', colspan: 2, rowspan: 1, group: 'DEFAULT', type: 'string', readOnly: true
        result.fields.push
            name: 'parent', value: 'parent.id', colspan: 1, rowspan: 1, group: 'DEFAULT', type: 'hidden'

    result.fields.push generateField(field, meta, labels) for field in form.fields
    result.tabs = form.tabs

    validateGroup = if formName == 'add' then Add else Edit
    result.validator = createValidator().buildValidateRules result.fields, meta.entityClass, validateGroup

    result

generateField = (config, meta, labels) ->
    field = config
    field = name: config if type(field) is 'string'

    defaults =
        label: labels[field.name], colspan: 1, rowspan: 1, group: 'DEFAULT'

    field = objects.extend defaults, field
    defineFieldType field, meta.getField(field.name), meta

    field

defineFieldType = (field, fieldMeta, entityMeta) ->
    if field.type is 'many-picker'
        if (fieldMeta.isManyToManyTarget() or fieldMeta.isOneToMany())
            field.pickerSource = fieldMeta.getPath()
            return

    return if field.type
    if fieldMeta.getType() is java.lang.Boolean
        field.type = 'picker'
        field.pickerSource = coala.booleanFieldPickerSource
        return
    if fieldMeta.getType() is java.util.Date
        field.type = 'date'
        return
    if fieldMeta.isEntity()
        field.type = 'picker'
        field.pickerSource = fieldMeta.getPath()
        return

    field.type = 'string'
