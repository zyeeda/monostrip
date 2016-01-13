module.exports = function configKarma(config) {
  config.set({

    browsers: ['PhantomJS'],

    singleRun: !!process.env.CONTINUOUS_INTEGRATION,

    frameworks: ['jasmine'],

    files: [
      './node_modules/phantomjs-polyfill/bind-polyfill.js',
      'src/**/__tests__/**/*-test.js'
    ],

    reporters: ['mocha'],

    plugins: [
      require('karma-jasmine'),
      require('karma-jasmine-ajax'),
      require('karma-phantomjs-launcher'),
      require('karma-mocha-reporter')
    ]
  })
}
