// @flow

import * as React from 'react'
import { Link } from 'react-router-dom'

import AuthPanel from '../AuthPanel'
import Input from '../Input'
import Actions from '../Actions'
import Button from '../Button'
import Checkbox from '../Checkbox'

const STEP_EMAIL = 0
const STEP_PASSWORD = 1
const STEP_DONE = 2

type Props = {}

type State = {
    step: number,
    form: {
        email: ?string,
        password: ?string,
        rememberMe: boolean,
    },
}

class LoginPage extends React.Component<Props, State> {
    state = {
        step: STEP_EMAIL,
        form: {
            email: null,
            password: null,
            rememberMe: false,
        }
    }

    onProceed = (e: SyntheticInputEvent<EventTarget>) => {
        const { step } = this.state
        e.preventDefault()

        switch (step) {
            case STEP_EMAIL:
                this.setState({
                    step: STEP_PASSWORD,
                })
                return
            case STEP_PASSWORD:
                this.setState({
                    step: STEP_DONE,
                })
                return
            default:
        }
    }

    goBack = () => {
        this.setState({
            step: Math.max(0, this.state.step - 1),
        })
    }

    render = () => {
        const { step, form: { rememberMe } } = this.state

        return (
            <React.Fragment>
                {step === STEP_EMAIL && (
                    <AuthPanel title="Sign In" signupLink>
                        <Input placeholder="Email" />
                        <Actions>
                            <Button onClick={this.onProceed}>Next</Button>
                        </Actions>
                    </AuthPanel>
                )}
                {step === STEP_PASSWORD && (
                    <AuthPanel title="Sign In" onGoBack={this.goBack}>
                        <Input placeholder="Password" type="password" />
                        <Actions>
                            <Checkbox checked={rememberMe}>Remember me</Checkbox>
                            <Link to="#">Forgot your password?</Link>
                            <Button onClick={this.onProceed}>Go</Button>
                        </Actions>
                    </AuthPanel>
                )}
                {step === STEP_DONE && (
                    <AuthPanel title="Sign In" signupLink onGoBack={this.goBack}>
                        Signed in.
                    </AuthPanel>
                )}
            </React.Fragment>
        )
    }
}

export default LoginPage
