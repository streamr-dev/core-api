// @flow

import React from 'react'
import {render} from 'react-dom'
import {Provider} from 'react-redux'
import ProfilePage from './components/ProfilePage'

import store from './stores/profilePageStore.js'

const root = document.getElementById('profilePageRoot')

if (!root) {
    throw new Error('Couldn\'t find element with id profilePageRoot')
}

render(
    <Provider store={store}>
        <ProfilePage />
    </Provider>,
    root
)
