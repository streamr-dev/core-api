// @flow

import React from 'react'
import {render} from 'react-dom'
import {Provider} from 'react-redux'
import { BrowserRouter, Route } from 'react-router-dom'
import createLink from './helpers/createLink'
import ShortcutHandler from './components/DashboardPage/ShortcutHandler'

import DashboardPage from './components/DashboardPage'

import store from './stores/dashboardPageStore.js'

const basename = createLink('/dashboard/editor').replace(window.location.origin, '')

const root = document.getElementById('dashboardPageRoot')

if (!root) {
    throw new Error('Couldn\'t find element with id dashboardPageRoot')
}

render(
    <Provider store={store}>
        <BrowserRouter basename={basename}>
            <ShortcutHandler>
                <Route path="/:id?" component={DashboardPage}/>
            </ShortcutHandler>
        </BrowserRouter>
    </Provider>,
    root
)
