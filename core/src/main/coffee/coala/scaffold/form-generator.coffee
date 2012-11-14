
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

exports.generateForms = (meta, labels = {}, forms, groups, formName, options) ->
    return null if not groups

    if not forms or not forms.defaults
        defaults = if groups['DEFAULT'] then groups: ['DEFAULT'] else groups: (groupName for groupName of groups)
    else
        defaults = forms.defaults
    if not forms or not forms[formName]
        form = if groups[formName] then groups: [formName] else defaults
    else
        form = forms[formName]

    generateForm form, meta, labels, groups, formName, options

generateForm = (form, meta, labels, fieldGroups, formName, options) ->
    groups = {}
    for value in form.groups or []
        if type(value) is 'string'
            groups[value] = label: null, columns: 1, readOnly: false
        else
            groups[value.name] = value

    result = {}
    result.groups = groups

    result.fields = []
    if options.style is 'tree' or options.style is 'treeTable'
        result.fields.push
            label: '父节点', name: 'parentName', value: 'parent.name', colspan: 2, rowspan: 1, group: 'DEFAULT', type: 'string', readOnly: true
        result.fields.push
            name: 'parent', value: 'parent.id', colspan: 1, rowspan: 1, group: 'DEFAULT', type: 'hidden'

    for groupName, group of groups
        result.fields.push generateField(field, meta, labels, groupName, group) for field in fieldGroups[groupName] or []
    result.tabs = form.tabs

    validateGroup = if formName == 'add' then Add else Edit
    result.validator = createValidator().buildValidateRules result.fields, meta.entityClass, validateGroup

    result.entityLabel = labels.entity if labels.entity

    result

generateField = (config, meta, labels, groupName, group) ->
    field = config
    field = name: config if type(field) is 'string'

    defaults =
        label: labels[field.name], colspan: 1, rowspan: 1, group: 'DEFAULT', readOnly: !!group.readOnly

    field = objects.extend defaults, field
    field.group = groupName
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
