var gulp = require('gulp')
var babel = require('gulp-babel')
var del = require('del')

gulp.task('copy', function copy() {
  return gulp.src('./src/**/*.json')
    .pipe(gulp.dest('./lib'))
})

gulp.task('compile', function compile() {
  return gulp.src(['./src/**/*.js', '!**/__tests__/**/*.js'])
    .pipe(babel())
    .pipe(gulp.dest('./lib'))
})

gulp.task('clean', function clean() {
  return del('./lib')
})

gulp.task('default', ['copy', 'compile'])
