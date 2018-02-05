// @flow

import createStore from './basicStore'

import integrationKeyReducer from '../reducers/integrationKey.js'
import keyReducer from '../reducers/key.js'

export default createStore({
    integrationKey: integrationKeyReducer,
    key: keyReducer
})