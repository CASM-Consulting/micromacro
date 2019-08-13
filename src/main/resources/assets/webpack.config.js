const path = require('path');

module.exports = {
    entry: './src/index.js',
    output: {
        path: path.resolve(__dirname, 'dist'),
        filename: 'bundle.js'
    },
    devServer: {
        contentBase: path.join(__dirname, 'dist'),
        port: 8080,
        proxy: {
            '/api/*': {
                target: 'http://localhost:8090',
                secure: false,
                changeOrigin: true
            }
        }
    },
    resolve: {
        extensions: [ '.tsx', '.ts', '.js' ]
    },

    devtool: 'inline-source-map',
    module: {
        rules: [
            {
                test: /\.tsx?$/,
                use: 'ts-loader',
                exclude: /node_modules/
            },
            {
                test: /\.css$/,
                use: ['style-loader', 'css-loader']
            },
            {
                test: /\.(png|woff|woff2|eot|ttf|svg)$/,
                use: ['file-loader']
            }
        ],
    },
};
