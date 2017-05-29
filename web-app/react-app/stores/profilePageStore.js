// @flow

import thunk from 'redux-thunk'

import {createStore, applyMiddleware, compose, combineReducers} from 'redux'
import  { createLogger } from 'redux-logger'

import integrationKeyReducer from '../reducers/integrationKeys.js'

const reducer = combineReducers({
    integrationKey: integrationKeyReducer,
})

const middleware = [thunk]
let toBeComposed = [applyMiddleware(...middleware)]

if (process.env.NODE_ENV !== 'production') {
    middleware.push(createLogger())
    
    if (window.__REDUX_DEVTOOLS_EXTENSION__ && window.__REDUX_DEVTOOLS_EXTENSION__()) {
        toBeComposed.push(window.__REDUX_DEVTOOLS_EXTENSION__())
    }
}

const store = createStore(
    reducer,
    compose.apply(null, toBeComposed)
)

export default store