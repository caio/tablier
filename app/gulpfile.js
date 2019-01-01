'use strict';

var browserSync = require('browser-sync');
var del = require('del');
var gulp = require('gulp');
var postcss = require('gulp-postcss')
var uncss = require('postcss-uncss')
var cssnano = require('cssnano')
var plugins = require('gulp-load-plugins')();
var gzip = require('gulp-gzip');
var sass = require('gulp-sass');

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

gulp.task('clean:before', function (done) {
    del([dirs.dist]).then(function () {
        done();
    });
});

gulp.task('clean:after', function (done) {
    del([
        dirs.dist + '/{css,css/**}',
        dirs.dist + '/{img,/img/**}',
        dirs.dist + '/{js,/js/**}'
    ]).then(function () {
        done();
    });
});


gulp.task('copy:css', function () {
    return gulp.src(dirs.src + '/css/main.css')
        .pipe(gulp.dest(dirs.dist + '/css/'));
});

gulp.task('copy', gulp.series('copy:css'));

gulp.task('sass', function() {
    return gulp.src(dirs.src + '/sass/style.scss')
        .pipe(sass().on('error', sass.logError))
        .pipe(gulp.dest(dirs.src + '/css/'));
});

gulp.task('generate:main.css', gulp.series('sass', function () {
    var postConfig = [
        uncss({
            html: [
                dirs.src + '/index.html',
                dirs.src + '/search.html',
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
            .pipe(plugins.rename('main.css'))
            .pipe(gulp.dest(dirs.src + '/css/'))
            .pipe(reload({ stream: true }));
}));


gulp.task('compress', function() {
   gulp.src(dirs.dist + '/**/*.{css,html,ico,js,svg,txt,xml}')
        .pipe(gzip())
        .pipe(gulp.dest(dirs.dist));
});

gulp.task('build', gulp.series(
    'clean:before',
    'generate:main.css',
    'copy',
    'clean:after',
    'compress',
));

gulp.task('default', gulp.series('build'));

gulp.task('serve', gulp.series('generate:main.css', function () {

    browserSyncOptions.server = dirs.src;
    browserSync(browserSyncOptions);

    gulp.watch([
        dirs.src + '/**/*.html'
    ], reload);

    gulp.watch([
        dirs.src + '/sass/**/*.scss'
    ], gulp.series('sass', function() { console.log("hue?"); }));

    gulp.watch([
        dirs.src + '/css/**/*.css',
        '!' + dirs.src + '/css/main.css'
    ], gulp.series('generate:main.css', reload));
}));

gulp.task('serve:build', gulp.series('build', function () {
    browserSyncOptions.server = dirs.dist;
    browserSync(browserSyncOptions);
}));
