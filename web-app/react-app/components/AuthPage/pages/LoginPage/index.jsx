// @flow

import * as React from 'react'
import { Link } from 'react-router-dom'

import AuthPanel from '../../shared/AuthPanel'
import Input from '../../shared/Input'
import Actions from '../../shared/Actions'
import Button from '../../shared/Button'
import Checkbox from '../../shared/Checkbox'
import AuthStep from '../../shared/AuthStep'

type Props = {}

type State = {
    form: {
        email: ?string,
        password: ?string,
        rememberMe: boolean,
    },
}

// HERE: we need to make Actions > [proceed] to go through to the next step.

class LoginPage extends React.Component<Props, State> {
    state = {
        form: {
            email: null,
            password: null,
            rememberMe: false,
        }
    }

    render = () => {
        const { form: { rememberMe } } = this.state

        return (
            <AuthPanel title="Sign In">
                <AuthStep>
                    <Input placeholder="Email" />
                    <Actions>
                        <Button proceed>Next</Button>
                    </Actions>
                </AuthStep>
                <AuthStep>
                    <Input placeholder="Password" type="password" />
                    <Actions>
                        <Checkbox checked={rememberMe}>Remember me</Checkbox>
                        <Link to="#">Forgot your password?</Link>
                        <Button proceed>Go</Button>
                    </Actions>
                </AuthStep>
                <AuthStep>
                    Signed in.
                </AuthStep>
            </AuthPanel>
        )
    }
}

export default LoginPage
