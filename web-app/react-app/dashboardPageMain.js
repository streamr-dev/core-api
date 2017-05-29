// @flow

import React from 'react'
import {render} from 'react-dom'
import {Provider} from 'react-redux'
import DashboardPage from './components/DashboardPage'

import store from './stores/dashboardPageStore.js'

render(
    <Provider store={store}>
        <DashboardPage/>
    </Provider>,
    document.getElementById('dashboardPageRoot')
)