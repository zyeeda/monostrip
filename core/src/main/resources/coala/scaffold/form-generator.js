// Generated by CoffeeScript 1.3.3

/*
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
*/


(function() {
  var coala, defineFieldType, generateField, generateForm, objects, type, _ref;

  _ref = require('coala/util'), type = _ref.type, objects = _ref.objects;

  coala = require('coala/config').coala;

  exports.generateForms = function(meta, labels, forms) {
    var add, defaults, edit;
    if (labels == null) {
      labels = {};
    }
    if (!forms) {
      return null;
    }
    defaults = forms.defaults || {};
    add = forms.add || defaults;
    edit = forms.edit || defaults;
    return {
      add: generateForm(add, meta, labels),
      edit: generateForm(edit, meta, labels)
    };
  };

  generateForm = function(form, meta, labels) {
    var field, groups, name, result, value, _i, _len, _ref1, _ref2;
    groups = {
      DEFAULT: {
        label: null,
        columns: 1
      }
    };
    _ref1 = form.groups || {};
    for (name in _ref1) {
      value = _ref1[name];
      if (value === null || type(value) === 'string') {
        groups[name] = {
          label: value,
          columns: 1
        };
      } else {
        groups[name] = value;
      }
    }
    result = {};
    result.groups = groups;
    result.fields = [];
    _ref2 = form.fields;
    for (_i = 0, _len = _ref2.length; _i < _len; _i++) {
      field = _ref2[_i];
      result.fields.push(generateField(field, meta, labels));
    }
    return result;
  };

  generateField = function(config, meta, labels) {
    var defaults, field;
    field = config;
    if (type(field) === 'string') {
      field = {
        name: config
      };
    }
    defaults = {
      label: labels[field.name],
      colspan: 1,
      rowspan: 1,
      group: 'DEFAULT'
    };
    field = objects.extend(defaults, field);
    defineFieldType(field, meta.getField(field.name), meta);
    return field;
  };

  defineFieldType = function(field, fieldMeta, entityMeta) {
    print(JSON.stringify(field));
    if (field.type) {
      return;
    }
    if (fieldMeta.getType() === java.lang.Boolean) {
      field.type = 'picker';
      field.pickerSource = coala.booleanFieldPickerSource;
      return;
    }
    if (fieldMeta.getType() === java.util.Date) {
      field.type = 'date';
      return;
    }
    if (fieldMeta.isEntity()) {
      field.type = 'picker';
      field.pickerSource = fieldMeta.getPath();
      return;
    }
    return field.type = 'string';
  };

}).call(this);
