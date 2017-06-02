// @flow

import React from 'react'
import {render} from 'react-dom'
import {Provider} from 'react-redux'
import { Router, Route, useRouterHistory } from 'react-router'
import { createHistory } from 'history'
import uuid from 'uuid'

import {getDashboard, getMyDashboardPermissions, newDashboard} from './actions/dashboard'
import {getRunningCanvases} from './actions/canvas'

import DashboardPage from './components/DashboardPage'

import store from './stores/dashboardPageStore.js'

declare var Streamr: any

const basename = Streamr.createLink({
    uri: '/dashboard/editor'
}).replace(window.location.origin, '')

const history = useRouterHistory(createHistory)({
    basename
})

render(
    <Provider store={store}>
        <Router history={history}>
            <Route path="/:id" component={DashboardPage} onEnter={(id) => {
                store.dispatch(getRunningCanvases())
                store.dispatch(getDashboard(id))
                store.dispatch(getMyDashboardPermissions(id))
            }}/>
            <Route path="/" component={DashboardPage} onEnter={() => {
                const id = uuid.v4()
                store.dispatch(getRunningCanvases())
                store.dispatch(newDashboard(id))
            }}/>
        </Router>
    </Provider>,
    document.getElementById('dashboardPageRoot')
)