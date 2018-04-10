// @flow

import React from 'react'
import {render} from 'react-dom'

import AuthPage from './components/AuthPage'

const root = document.getElementById('authPageRoot')

if (!root) {
    throw new Error('Couldn\'t find element with id authPageRoot')
}

render(
    <AuthPage />,
    root
)
