// @flow

import React, {Component} from 'react'
import AuthPanel from '../AuthPanel'

type Props = {}

type State = {}

export default class LoginPage extends Component<Props, State> {
    render() {
        return (
            <AuthPanel
                title={'Login'}
            >
                LoginPage
            </AuthPanel>
        )
    }
}
