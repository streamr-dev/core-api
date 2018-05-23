// @flow

import React, {Component} from 'react'
import { Link } from 'react-router-dom'

import AuthPanel from '../AuthPanel'

type Props = {}

type State = {}

export default class RegisterPage extends Component<Props, State> {
    render() {
        return (
            <AuthPanel
                title={'Sign Up'}
                leftUtil={'Sign in with ethereum'}
                rightUtil={<Link to="/login">Sign in</Link>}
            >
                LoginPage
            </AuthPanel>
        )
    }
}
