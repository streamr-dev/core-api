// @flow

import thunk from 'redux-thunk'

import {createStore, applyMiddleware, compose, combineReducers} from 'redux'

import notificationReducer from '../reducers/notification'

export default (reducers: {}) => {
    const middleware = [thunk]
    let toBeComposed = [applyMiddleware(...middleware)]
    
    if (process.env.NODE_ENV !== 'production') {
        if (window.__REDUX_DEVTOOLS_EXTENSION__ && window.__REDUX_DEVTOOLS_EXTENSION__()) {
            toBeComposed.push(window.__REDUX_DEVTOOLS_EXTENSION__())
        }
    }
    
    return createStore(
        combineReducers({
            notifications: notificationReducer,
            ...reducers
        }),
        compose.apply(null, toBeComposed)
    )
}

