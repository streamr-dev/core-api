// @flow

import React from 'react'
import {render} from 'react-dom'
import {Provider} from 'react-redux'
import StreamPage from './components/StreamPage'

import store from './stores/streamPageStore.js'

const id = 'streamPageDataPreviewRoot'

const root = document.getElementById(id)

if (!root) {
    throw new Error(`Couldn't find element with id ${id}!`)
}

render(
    <Provider store={store}>
        <StreamPage/>
    </Provider>,
    root
)
