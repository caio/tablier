'use strict';

var browserSync = require('browser-sync');
var gulp = require('gulp');
var gzip = require('gulp-gzip');
var postcss = require('gulp-postcss')
var uncss = require('postcss-uncss')
var cssnano = require('cssnano')
var rename = require('gulp-rename');
var sass = require('gulp-sass');
var mustache = require('gulp-mustache');

var pkg = require('./package.json');
var dirs = pkg['configuration'].directories;
var reload = browserSync.reload;

var browserSyncOptions = {
    logPrefix: 'TABLIER',
    notify: false,
    open: false,
    port: 1313,
    injectChanges: false,
};

gulp.task('sass', function() {
    return gulp.src(dirs.src + '/sass/style.scss')
        .pipe(sass().on('error', sass.logError))
        .pipe(gulp.dest(dirs.src + '/css/'));
});

gulp.task('html', function() {
    return gulp.src(dirs.src + '/template/*.mustache')
        .pipe(mustache(dirs.src + '/render_data.json'))
        .pipe(rename(function(path) { path.extname = ".html"; }))
        .pipe(gulp.dest(dirs.src));
});

gulp.task('css', gulp.series('html', 'sass', function () {
    var postConfig = [
        uncss({
            html: [
                dirs.src + '/*.html',
            ],
            ignore: [
                /^\.navbar/,
                /^\.burger/,
                /^\.columns$/,
                '.content',
                /^\.pagination-next/,
            ],
        }),
        cssnano(),
    ];
    return gulp.src(dirs.src + '/css/style.css')
            .pipe(postcss(postConfig))
            .pipe(rename('main.css'))
            .pipe(gulp.dest(dirs.src + '/css/'));
}));

gulp.task('build', gulp.series('css', function () {
    return gulp.src(dirs.src + '/css/main.css')
            .pipe(gzip())
            .pipe(rename('main.css.gz'))
            .pipe(gulp.dest(dirs.src + '/css/'));
}));

gulp.task('serve', gulp.series('css', function () {

    browserSyncOptions.server = dirs.src;
    browserSync(browserSyncOptions);

    gulp.watch([
        dirs.src + '/template/**/*.mustache',
        dirs.src + '/render_data.json',
    ], gulp.series('css'));

    gulp.watch([
        dirs.src + '/sass/**/*.scss'
    ], gulp.series('sass'));
}));

gulp.task('default', gulp.series('serve'));
