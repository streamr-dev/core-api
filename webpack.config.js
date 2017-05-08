const path = require('path')
const webpack = require('webpack')
const ExtractTextPlugin = require('extract-text-webpack-plugin')
const WebpackNotifierPlugin = require('webpack-notifier')

const postcssConfig = require('./postcss.config.js')

const root = path.resolve(__dirname)
const jsRoot = path.resolve(root, 'web-app', 'js', 'unifina')

const inProduction = process.env.NODE_ENV === 'production'

module.exports = {
    entry: {
        profilePage: path.resolve(jsRoot, 'streamr-account-handler', 'StreamrAccountHandler.jsx')
    },
    output: {
        path: path.resolve(jsRoot, 'webpack-bundles'),
        filename: '[name]-bundle.js'
    },
    module: {
        rules: [
            {
                test: /\.jsx?$/,
                include: path.resolve(root),
                enforce: 'pre',
                use: [{
                    loader: 'eslint-loader',
                    options: {
                        configFile: path.resolve(root, '.eslintrc.js')
                    }
                }]
            },
            {
                test: /.jsx?$/,
                loader: 'babel-loader',
                exclude: /node_modules/,
                query: {
                    presets: ['es2015', 'stage-0', 'react']
                }
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
                        'css-loader?modules&importLoaders=1&localIdentName=[name]__[local]__[hash:base64:5]',
                        'postcss-loader'
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
        new webpack.LoaderOptionsPlugin({
            test: /\.pcss$/,
            options: {
                postcss: postcssConfig
            }
        }),
        new ExtractTextPlugin("[name]-bundle.css")
    ].concat(inProduction ? [
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
        new webpack.NoEmitOnErrorsPlugin(),
        new WebpackNotifierPlugin()
    ]),
    devtool: !inProduction && 'eval-source-map',
    devServer: !inProduction ? {
        progress: true,
        colors: true,
        inline: true
    } : {},
    resolve: {
        extensions: ['.js', '.jsx', '.json']
    }
}

