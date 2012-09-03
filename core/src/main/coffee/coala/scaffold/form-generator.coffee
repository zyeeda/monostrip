
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

exports.generateForms = (meta, labels = {}, forms) ->
    return null if not forms

    defaults = forms.defaults or {}
    add = forms.add or defaults
    edit = forms.edit or defaults

    add: generateForm add, meta, labels
    edit: generateForm edit, meta, labels

generateForm = (form, meta, labels) ->
    groups = DEFAULT: {label: null, columns: 1}
    for name, value of form.groups or {}
        if value is null or type(value) is 'string'
            groups[name] = label: value, columns: 1
        else
            groups[name] = value

    result = {}
    result.groups = groups

    result.fields = []
    result.fields.push generateField(field, meta, labels) for field in form.fields
    result.tabs = form.tabs
    
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
    print JSON.stringify field
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
