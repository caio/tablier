'use strict';

var gulp = require('gulp');
var gzip = require('gulp-gzip');
var postcss = require('gulp-postcss')
var uncss = require('postcss-uncss')
var cssnano = require('cssnano')
var rename = require('gulp-rename');
var sass = require('gulp-sass');
var fs = require('fs');
const PluginError = require('plugin-error');

var pkg = require('./package.json');
var dirs = pkg['configuration'].directories;

gulp.task('sass', function() {
    return gulp.src(dirs.src + '/sass/style.scss')
        .pipe(sass.sync().on('error', function(err) {
            const message = new PluginError('sass', err.messageFormatted).toString();
            process.stderr.write(`${message}\n`);
            process.exit(1);
        }))
        .pipe(gulp.dest(dirs.src + '/css/'));
});

gulp.task('css', gulp.series('sass', function () {
    var postConfig = [
        uncss({
            html: [
                dirs.src + '/*.html',
                dirs.src + '/pages/*.html',
            ],
        }),
        cssnano(),
    ];
    return gulp.src(dirs.src + '/css/style.css')
            .pipe(postcss(postConfig))
            .pipe(rename('main.css'))
            .pipe(gulp.dest(dirs.src + '/css/'));
}));

gulp.task('compress_pages', function() {
    return gulp.src(dirs.src + '/pages/*.html')
            .pipe(gzip())
            .pipe(gulp.dest(dirs.src + '/pages/'))
});

gulp.task('build', gulp.series('css', 'compress_pages', function () {
    return gulp.src(dirs.src + '/css/main.css')
            .pipe(gzip())
            .pipe(rename('main.css.gz'))
            .pipe(gulp.dest(dirs.src + '/css/'));
}));

gulp.task('default', gulp.series('build'));
