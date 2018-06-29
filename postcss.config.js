const webpack = require('webpack')
const postcssImport = require('postcss-import')({
    addDependencyTo: webpack,
})
const precss = require('precss')
const prefixer = require('autoprefixer')
const nested = require('postcss-nested')
const colorFunction = require('postcss-color-function')
const mqpacker = require('css-mqpacker')
const extend = require('postcss-extend')
const vars = require('@streamr/streamr-layout/postcss-variables')

module.exports = {
    plugins: [
        postcssImport,
        precss,
        prefixer,
        nested,
        colorFunction,
        mqpacker,
        extend,
        vars,
    ]
}
