// @flow

import createStore from './basicStore'

import dashboardReducer from '../reducers/dashboard.js'
import canvasReducer from '../reducers/canvas.js'
import permissionReducer from '../reducers/permission.js'

export default createStore({
    dashboard: dashboardReducer,
    canvas: canvasReducer,
    permission: permissionReducer
})
