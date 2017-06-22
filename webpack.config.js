const path = require('path')
const webpack = require('webpack')
const ExtractTextPlugin = require('extract-text-webpack-plugin')
const WebpackNotifierPlugin = require('webpack-notifier')
const WriteFilePlugin = require('write-file-webpack-plugin')
const FlowtypePlugin = require('flowtype-loader/plugin')

const postcssConfig = require('./postcss.config.js')

const root = path.resolve(__dirname)

const inProduction = process.env.NODE_ENV === 'production'

module.exports = {
    entry: {
        profilePage: path.resolve(root, 'web-app', 'react-app', 'profilePageMain.js'),
        dashboardPage: path.resolve(root, 'web-app', 'react-app', 'dashboardPageMain.js')
    },
    output: {
        path: path.resolve(root, 'web-app', 'js', 'unifina', 'webpack-bundles'),
        publicPath: '/js/unifina/webpack-bundles/',
        filename: '[name].bundle.js'
    },
    module: {
        rules: [
            {
                test: /\.jsx?$/,
                include: path.resolve(root),
                exclude: /node_modules/,
                enforce: 'pre',
                use: [{
                    loader: 'eslint-loader',
                    options: {
                        configFile: path.resolve(root, '.eslintrc.js')
                    }
                }, !inProduction ? {
                    loader: 'flowtype-loader'
                } : undefined].filter(i => i) // remove possible undefined
            },
            {
                test: /.jsx?$/,
                loader: 'babel-loader',
                exclude: /node_modules/
            },
            {
                test: /\.(png|woff|woff2|eot|ttf|svg)$/,
                loader: 'url-loader?limit=100000'
            },
            // .pcss files treated as modules
            {
                test: /\.pcss$/,
                use: ExtractTextPlugin.extract({
                    fallback: 'style-loader',
                    use: [
                        {
                            loader: 'css-loader',
                            options: {
                                modules: true,
                                importLoaders: 1,
                                localIdentName: '[name]__[local]__[hash:base64:5]'
                            }
                        }, {
                            loader: 'postcss-loader'
                        }
                    ]
                })
            },
            // .css files imported as plain css files
            {
                test: /\.css$/,
                use: ExtractTextPlugin.extract({
                    fallback: 'style-loader',
                    use: 'css-loader'
                })
            }
        ]
    },
    plugins: [
        // Plugins always in use
        new webpack.LoaderOptionsPlugin({
            test: /\.pcss$/,
            options: {
                postcss: postcssConfig
            }
        }),
        new ExtractTextPlugin('[name].bundle.css')
    ].concat(inProduction ? [
        // Production plugins
        new webpack.optimize.OccurrenceOrderPlugin(),
        new webpack.DefinePlugin({
            'process.env': {
                'NODE_ENV': JSON.stringify('production')
            }
        }),
        new webpack.optimize.UglifyJsPlugin({
            compressor: {
                warnings: false
            }
        })
    ] : [
        // Dev plugins
        new FlowtypePlugin(),
        new webpack.NoEmitOnErrorsPlugin(),
        new WebpackNotifierPlugin(),
        new WriteFilePlugin(),
        new webpack.optimize.CommonsChunkPlugin('commons')
    ]),
    devtool: !inProduction && 'eval-source-map',
    resolve: {
        extensions: ['.js', '.jsx', '.json']
    }
}

