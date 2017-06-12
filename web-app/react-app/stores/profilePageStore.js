// @flow

import thunk from 'redux-thunk'

import {createStore, applyMiddleware, compose, combineReducers} from 'redux'

import integrationKeyReducer from '../reducers/integrationKeys.js'
import userReducer from '../reducers/user.js'

const reducer = combineReducers({
    integrationKey: integrationKeyReducer,
    user: userReducer,
})

const middleware = [thunk]
let toBeComposed = [applyMiddleware(...middleware)]

if (process.env.NODE_ENV !== 'production') {
    // Needed for redux devtools
    if (window.__REDUX_DEVTOOLS_EXTENSION__ && window.__REDUX_DEVTOOLS_EXTENSION__()) {
        toBeComposed.push(window.__REDUX_DEVTOOLS_EXTENSION__())
    }
}

const store = createStore(
    reducer,
    // Compose does not work with undefined, that's why apply
    compose.apply(null, toBeComposed)
)

export default store