// @flow

import React from 'react'
import {render} from 'react-dom'
import {Provider} from 'react-redux'
import ProfilePage from './components/ProfilePage'

import store from './stores/profilePageStore.js'

render(
    <Provider store={store}>
        <ProfilePage />
    </Provider>,
    document.getElementById('profilePageRoot')
)