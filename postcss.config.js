/* eslint-disable global-require */
const webpack = require('webpack')

module.exports = {
    plugins: [
        require('postcss-import')({
            addDependencyTo: webpack,
        }),
        require('precss'),
        require('autoprefixer'),
        require('postcss-nested'),
        require('postcss-color-function'),
        require('css-mqpacker'),
        require('postcss-extend')
    ]
}