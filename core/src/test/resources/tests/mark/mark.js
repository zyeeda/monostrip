var _ = require('underscore');
var {mark} = require('cdeio/mark');
var {ok} = require('assert');

exports.testMark = function() {
    ok(mark !== null);
};

exports.testUnderscore = function() {
    ok(_ !== null);
};