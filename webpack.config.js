const path = require('path')
const webpack = require('webpack')
const ExtractTextPlugin = require('extract-text-webpack-plugin')
const WebpackNotifierPlugin = require('webpack-notifier')
const WriteFilePlugin = require('write-file-webpack-plugin')
const FlowtypePlugin = require('flowtype-loader/plugin')
const CleanWebpackPlugin = require('clean-webpack-plugin')
const UglifyJsPlugin = require('uglifyjs-webpack-plugin')

const postcssConfig = require('./postcss.config.js')

const root = path.resolve(__dirname)

const inProduction = process.env.NODE_ENV === 'production'

const port = process.env.PORT || 9000

const noActualFiles = !!process.env.NO_FILES

const commonPlugins = [
    // Plugins always in use
    new webpack.LoaderOptionsPlugin({
        test: /\.pcss$/,
        options: {
            postcss: postcssConfig
        }
    }),
    new ExtractTextPlugin(inProduction ? '[name].bundle.[chunkhash].css' : '[name].bundle.css'),
    new webpack.optimize.CommonsChunkPlugin('commons')]

const environmentPlugins = inProduction ? [
    // Production plugins
    new CleanWebpackPlugin(['web-app/webpack-bundles/*.js', 'web-app/webpack-bundles/*.css'], {
        verbose: true
    }),
    new webpack.optimize.OccurrenceOrderPlugin(),
    new webpack.DefinePlugin({
        'process.env': {
            'NODE_ENV': JSON.stringify('production')
        }
    }),
    new UglifyJsPlugin({
        uglifyOptions: {
            compressor: {
                warnings: false
            }
        }
    })
] : [
    // Dev plugins
    new FlowtypePlugin(),
    new webpack.NoEmitOnErrorsPlugin(),
    new WebpackNotifierPlugin()
]

const additionalPlugins = [].concat(
    (!inProduction && !noActualFiles) ? [new WriteFilePlugin()] : []
)

module.exports = {
    entry: {
        profilePage: path.resolve(root, 'web-app', 'react-app', 'profilePageMain.js'),
        dashboardPage: path.resolve(root, 'web-app', 'react-app', 'dashboardPageMain.js'),
        // TODO: remove this when CORE-1075-reactify-stream-page is ready
        streamPageDataPreview: path.resolve(root, 'web-app', 'react-app', 'streamPageDataPreviewMain.js')
    },
    output: {
        path: path.resolve(root, 'web-app', 'webpack-bundles'),
        publicPath: '/',
        filename: inProduction ? '[name].bundle.[chunkhash].js' : '[name].bundle.js'
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
                                localIdentName: inProduction ? '[local]_[hash:base64:5]' : '[name]_[local]'
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
    plugins: commonPlugins
        .concat(environmentPlugins)
        .concat(additionalPlugins),
    devtool: !inProduction && 'eval-source-map',
    devServer: {
        port: port,
        contentBase: root
    },
    resolve: {
        extensions: ['.js', '.jsx', '.json'],
        alias: {
            'ws': 'empty/functionThatReturnsTrue'
        }
    }
}

