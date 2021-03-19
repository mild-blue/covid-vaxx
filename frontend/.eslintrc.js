module.exports = {
  env: {
    browser: true,
    es6: true,
    node: true
  },
  extends: [
    'prettier',
    'prettier/@typescript-eslint'
  ],
  parser: '@typescript-eslint/parser',
  parserOptions: {
    project: 'tsconfig.eslint.json',
    sourceType: 'module'
  },
  plugins: [
    '@typescript-eslint',
    'no-null',
    'prefer-arrow',
    'deprecation'
  ],
  rules: {
    'deprecation/deprecation': 'error',
    '@typescript-eslint/array-type': [
      'error',
      {
        'default': 'array'
      }
    ],
    '@typescript-eslint/await-thenable': 'error',
    '@typescript-eslint/consistent-type-definitions': ['error', 'interface'],
    '@typescript-eslint/explicit-member-accessibility': [
      'off',
      {
        'accessibility': 'explicit'
      }
    ],
    '@typescript-eslint/indent': [
      'error',
      2,
      {
        'SwitchCase': 1,
        'flatTernaryExpressions': true,
        'FunctionDeclaration': {
          'parameters': 'first'
        },
        'FunctionExpression': {
          'parameters': 'first'
        },
        'MemberExpression': 0,
        'outerIIFEBody': 1
      }
    ],
    '@typescript-eslint/member-delimiter-style': [
      'error',
      {
        'multiline': {
          'delimiter': 'semi',
          'requireLast': true
        },
        'singleline': {
          'delimiter': 'semi',
          'requireLast': true
        }
      }
    ],
    '@typescript-eslint/member-ordering': [
      'error',
      {
        'default': [
          'signature',
          'private-static-field',
          'protected-static-field',
          'public-static-field',
          'public-static-method',
          'protected-static-method',
          'private-static-method',
          'private-field',
          'protected-field',
          'public-field',
          'public-constructor',
          'protected-constructor',
          'private-constructor'
          // getters & setters
          // 'public-method',
          // 'protected-method',
          // 'private-method'
        ]
      }
    ],
    '@typescript-eslint/no-empty-function': 'off',
    '@typescript-eslint/no-explicit-any': 'error',
    '@typescript-eslint/no-invalid-void-type': 'error',
    '@typescript-eslint/no-non-null-assertion': 'error',
    '@typescript-eslint/no-parameter-properties': 'off',
    '@typescript-eslint/no-use-before-define': 'off',
    '@typescript-eslint/no-unnecessary-boolean-literal-compare': 'error',
    '@typescript-eslint/no-var-requires': 'error',
    '@typescript-eslint/prefer-for-of': 'error',
    '@typescript-eslint/prefer-function-type': 'error',
    '@typescript-eslint/promise-function-async': 'error',
    '@typescript-eslint/quotes': [
      'error',
      'single',
      {
        'avoidEscape': true
      }
    ],
    '@typescript-eslint/restrict-plus-operands': 'error',
    '@typescript-eslint/semi': [
      'error',
      'always'
    ],
    '@typescript-eslint/type-annotation-spacing': [
      'error',
      {
        'before': false,
        'after': true,
        'overrides': {
          'arrow':
            {
              'before': true,
              'after': true
            }
        }
      }
    ],
    '@typescript-eslint/typedef': [
      'error',
      {
        'arrowParameter': false
      }
    ],
    '@typescript-eslint/unified-signatures': 'error',
    'arrow-parens': [
      'off',
      'as-needed'
    ],
    'comma-dangle': 'error',
    'complexity': 'off',
    'constructor-super': 'error',
    'curly': 'error',
    'dot-notation': 'error',
    'grouped-accessor-pairs': ['error', 'getBeforeSet'],
    'eol-last': 'error',
    'eqeqeq': [
      'error',
      'smart'
    ],
    'guard-for-in': 'error',
    /*'id-blacklist': [
      'error',
      'any',
      'Number',
      'number',
      'String',
      'string',
      'Boolean',
      'boolean',
      'Undefined',
      'undefined'
    ],*/
    'id-match': 'error',
    'import/no-deprecated': 'off',
    'import/order': 'off',
    'jsdoc/no-types': 'off',
    'max-classes-per-file': [
      'error',
      1
    ],
    'max-len': [
      'error',
      {
        'code': 200
      }
    ],
    'new-parens': 'error',
    'no-alert': 'error',
    'no-bitwise': 'error',
    'no-caller': 'error',
    'no-cond-assign': 'error',
    'no-console': [
      'error',
      {
        'allow': ['log', 'warn', 'error', 'time', 'timeEnd', 'assert', 'groupCollapsed', 'groupEnd']
      }
    ],
    'no-debugger': 'error',
    'no-fallthrough': 'error',
    '@typescript-eslint/no-inferrable-types': [
      'error',
      {
        'ignoreParameters': true,
        'ignoreProperties': true
      }
    ],
    'no-irregular-whitespace': 'error',
    'no-magic-numbers': [
      'error',
      {
        'ignore': [
          -1,
          0,
          1,
          2,
          200,
          10,
          100,
          1000,
          401,
          403,
          404,
          409,
          2000
        ]
      }
    ],
    'no-multiple-empty-lines': 'off',
    'no-new-wrappers': 'error',
    'no-null/no-null': 2,
    'no-restricted-imports': [
      'error',
      'rxjs/Rx',
      'firebase'
    ],
    'no-restricted-syntax': [
      'error',
      'ForInStatement'
    ],
    'no-return-await': 'error',
    // not working correctly with enums
    // 'no-shadow': [
    //   'error',
    //   {
    //     'hoist': 'all'
    //   }
    // ],
    'no-throw-literal': 'error',
    'no-trailing-spaces': 'error',
    'no-undef-init': 'error',
    'no-underscore-dangle': 'off',
    'no-unsafe-finally': 'error',
    'no-unused-expressions': 'off',
    'no-unused-labels': 'error',
    'no-var': 'error',
    'object-shorthand': 'error',
    'one-var': [
      'error',
      'never'
    ],
    'prefer-arrow/prefer-arrow-functions': 'error',
    'prefer-const': 'error',
    'prefer-object-spread': 'error',
    'prefer-template': 'error',
    'quote-props': [
      'error',
      'as-needed'
    ],
    'radix': 'error',
    'semi': 'off',
    'space-before-function-paren': [
      'error',
      {
        'anonymous': 'never',
        'asyncArrow': 'always',
        'named': 'never'
      }
    ],
    'spaced-comment': 'off',
    'use-isnan': 'error',
    'valid-typeof': 'off',
    '@typescript-eslint/naming-convention': [
      'error',
      { 'selector': 'typeLike', 'format': ['PascalCase'] },
      {
        'selector': 'memberLike',
        'modifiers': ['private'],
        'format': ['camelCase'],
        'leadingUnderscore': 'require'
      },
      {
        'selector': 'memberLike',
        'modifiers': ['protected'],
        'format': ['camelCase'],
        'leadingUnderscore': 'forbid'
      },
      {
        'selector': 'memberLike',
        'modifiers': ['public'],
        'format': ['camelCase', 'PascalCase', 'snake_case'],
        'leadingUnderscore': 'forbid'
      },
      {
        'selector': 'variableLike',
        'format': ['camelCase', 'snake_case'],
        'leadingUnderscore': 'allow'
      },
      {
        'selector': 'interface',
        'format': ['PascalCase']
      }
    ]
  }
};
