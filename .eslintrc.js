
const OFF = 0, WARN = 1, ERROR = 2;

module.exports = exports = {
    
    extends: [
        'eslint:recommended',
        'plugin:react/recommended'
    ],
    
    globals: {
        'baseUrl': true
    },
    
    global: true,
    
    parserOptions: {
        ecmaVersion: 6,
        sourceType: 'module',
        ecmaFeatures: {
            jsx: true,
            modules: true,
            experimentalObjectRestSpread: true,
            destructuring: true
        }
    },
    
    env: {
        es6: true,
        node: true,
        browser: true,
        mocha: true
    },
    
    plugins: [
        'react',
        'mocha'
    ],
    
    rules: {
        'no-debugger': WARN,
        'no-console': [
            WARN,
            {
                allow: [
                    'warn',
                    'error'
                ]
            }
        ],
        'semi': [ERROR, 'never'],
        'no-unused-vars': WARN,
        'curly': [ERROR, 'all'],
        'keyword-spacing': [ERROR, {
            'before': true,
            'after': true
        }],
        'quotes': [ERROR, 'single'],
        'space-before-function-paren': [ERROR, 'never'],
        'space-before-blocks': [ERROR, 'always'],
        'space-in-parens': ['error', 'never'],
        'space-unary-ops': [ERROR, {
            'words': true,
            'nonwords': false
        }],
        'brace-style': [ERROR, '1tbs'],
        'newline-per-chained-call': [ERROR, {
            'ignoreChainWithDepth': 2
        }],
        'object-curly-newline': [ERROR, {
            "ObjectExpression": {
                "minProperties": 1
            },
            "ObjectPattern": "never"
        }],
        'indent': [ERROR, 4, {
            'SwitchCase': WARN,
            "MemberExpression": WARN,
            "ObjectExpression": WARN
        }],
        'object-property-newline': [ERROR, {
            'allowMultiplePropertiesPerLine': true
        }],
        'no-unexpected-multiline': ERROR,
        'wrap-iife': [ERROR, 'inside']
    }
}