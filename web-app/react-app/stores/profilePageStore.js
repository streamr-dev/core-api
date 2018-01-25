// @flow

import createStore from './basicStore'

import integrationKeyReducer from '../reducers/integrationKey.js'

export default createStore({
    integrationKey: integrationKeyReducer,
})