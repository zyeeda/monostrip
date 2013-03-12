{type} = require 'coala/util/type'
objects = require 'coala/util/objects'
{coala} = require 'coala/config'
{createValidator} = require 'coala/validation/validator'

{Add, Edit} = com.zyeeda.coala.validator.group
{TreeNode} = com.zyeeda.coala.commons.base.data
{ClassUtils} = org.springframework.util

exports.generateForms = (meta, labels = {}, forms, groups, formName, options) ->
    return null if not groups

    if not forms or not forms.defaults
        defaults = if groups['defaults'] then groups: ['defaults'] else groups: (groupName for groupName of groups)
    else
        defaults = forms.defaults
    if not forms or not forms[formName]
        form = if groups[formName] then groups: [formName] else defaults
    else
        form = forms[formName]

    generateForm form, meta, labels, groups, formName, options

generateForm = (form, meta, labels, fieldGroups, formName, options) ->
    groups = []
    for value in form.groups or []
        g = if type(value) is 'string' then name: value else value
        g.readOnly = true if formName is 'show'
        groups.push g

    result = {}
    result.labelOnTop = form.labelOnTop
    result.size = form.size
    result.groups = groups

    fg = result.fieldGroups = {}
    allFields = []
    for group in groups
        fields = []
        fields.push generateField(field, meta, labels, group.name, group) for field in fieldGroups[group.name] or []
        allFields = allFields.concat fields
        fg[group.name] = fields

    # if options.style is 'tree' or options.style is 'treeTable'
    #     fg['TREE_GROUP'] = [
    #         label: '父节点', name: 'parentName', value: 'parent.name', readOnly: true, type: 'text'
    #     ,
    #         name: 'parent', value: 'parent.id', type: 'hidden'
    #     ]
    #     result.groups.unshift 'TREE_GROUP'

    result.tabs = form.tabs

    validateGroup = if formName is 'add' then Add else Edit

    result.validation = createValidator().buildValidateRules allFields, meta.entityClass, validateGroup

    result.entityLabel = labels.entity if labels.entity

    result

generateField = (config, meta, labels, groupName, group) ->
    field = config
    field = name: config if type(field) is 'string'

    defaults =
        label: labels[field.name]

    field = objects.extend defaults, field
    defineFieldType field, meta.getField(field.name), meta

    field

defineFieldType = (field, fieldMeta, entityMeta) ->
    if field.type is 'many-picker'
        if (fieldMeta.isManyToManyTarget() or fieldMeta.isOneToMany())
            field.source = fieldMeta.getPath()
            return

    return if field.type
    return field.type = 'text' if not fieldMeta

    if fieldMeta.getType() is java.lang.Boolean
        field.type = 'dropdown'
        field.source = coala.booleanFieldPickerSource
        return
    if fieldMeta.getType() is java.util.Date
        field.type = 'datepicker'
        return
    if fieldMeta.isEntity()
        field.type = if ClassUtils.isAssignable(TreeNode, fieldMeta.getType()) then 'tree-picker' else 'grid-picker'
        field.source = fieldMeta.getPath()
        return

    field.type = 'text'
