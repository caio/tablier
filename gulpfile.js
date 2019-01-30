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
var fs = require('fs');

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

gulp.task('html_basic', function() {
    return gulp.src([
            dirs.src + '/template/index.mustache',
            dirs.src + '/template/error.mustache',
            dirs.src + '/template/search.mustache',
            dirs.src + '/template/zero_results.mustache',
        ]).pipe(mustache(dirs.src + '/render_data.json'))
        .pipe(rename(function(path) { path.extname = ".html"; }))
        .pipe(gulp.dest(dirs.src));
});

function getRenderDataSync() {
    var dataFile = './' + dirs.src + '/render_data.json';
    var renderData = JSON.parse(fs.readFileSync(dataFile, 'utf8'));
    return renderData;
}

function renderCustomSearch(suffix, cb) {
    var renderData = getRenderDataSync();
    cb(renderData);
    return gulp.src(dirs.src + '/template/search.mustache')
        .pipe(mustache(renderData))
        .pipe(rename(function(path) { path.basename += suffix; path.extname = ".html"; }))
        .pipe(gulp.dest(dirs.src));
};

gulp.task('html_index_unstable', function() {
    var renderData = getRenderDataSync();
    renderData.show_unstable_warning = true;
    renderData.search_is_disabled = true;
    return gulp.src(dirs.src + '/template/index.mustache')
        .pipe(mustache(renderData))
        .pipe(rename(function(path) { path.basename += "_unstable"; path.extname = ".html"; }))
        .pipe(gulp.dest(dirs.src));
});

gulp.task('html_search_no_next', function() {
    return renderCustomSearch('_no_next', function(data) {
        data.pagination_next_href = null;
    });
});

gulp.task('html_search_no_prev', function() {
    return renderCustomSearch('_no_prev', function(data) {
        data.pagination_prev_href = null;
    });
});

gulp.task('html_search_no_both', function() {
    return renderCustomSearch('_no_both', function(data) {
        data.pagination_next_href = null;
        data.pagination_prev_href = null;
    });
});

gulp.task('html',
    gulp.parallel(
        'html_basic',
        'html_index_unstable',
        'html_search_no_next',
        'html_search_no_prev',
        'html_search_no_both'));

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
