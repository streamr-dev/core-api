// @flow

import * as React from 'react'
import { Link } from 'react-router-dom'
import * as yup from 'yup'

import AuthPanel from '../../shared/AuthPanel'
import Input from '../../shared/Input'
import Actions from '../../shared/Actions'
import Button from '../../shared/Button'
import Checkbox from '../../shared/Checkbox'
import AuthStep from '../../shared/AuthStep'

type Props = {}

type State = {
    email: string,
    password: string,
    rememberMe: boolean,
    errors: {
        [string]: string,
    },
}

class LoginPage extends React.Component<Props, State> {
    state = {
        email: '',
        password: '',
        rememberMe: false,
        errors: {},
    }

    validateEmail = () => yup.object()
        .shape({
            email: yup.string()
                .trim()
                .required('is required'),
        })
        .validate(this.state)

    validatePassword = () => yup.object()
        .shape({
            password: yup.string().required('is required'),
        })
        .validate(this.state)

    onInputChange = (field: string) => (e: SyntheticInputEvent<EventTarget>) => {
        this.setState({
            [field]: e.target.value,
            errors: {
                ...this.state.errors,
                [field]: '',
            },
        })
    }

    onValidationError = (error: ?yup.ValidationError) => {
        if (error) {
            this.setState({
                errors: {
                    ...this.state.errors,
                    [error.path]: error.message,
                },
            })
        }
    }

    onRememberMeChange = (e: any) => {
        this.setState({
            rememberMe: e.target.checked,
        })
    }

    render = () => {
        const { email, password, rememberMe, errors } = this.state

        return (
            <AuthPanel>
                <AuthStep title="Sign In" showEth showSignup validate={this.validateEmail} onValidationError={this.onValidationError}>
                    <Input
                        placeholder="Email"
                        onChange={this.onInputChange('email')}
                        value={email}
                        error={errors.email}
                    />
                    <Actions>
                        <Button proceed>Next</Button>
                    </Actions>
                </AuthStep>
                <AuthStep title="Sign In" showBack validate={this.validatePassword} onValidationError={this.onValidationError}>
                    <Input
                        placeholder="Password"
                        onChange={this.onInputChange('password')}
                        value={password}
                        type="password"
                        error={errors.password}
                    />
                    <Actions>
                        <Checkbox checked={rememberMe} onChange={this.onRememberMeChange}>Remember me</Checkbox>
                        <Link to="#">Forgot your password?</Link>
                        <Button proceed>Go</Button>
                    </Actions>
                </AuthStep>
                <AuthStep title="Done" showBack>
                    Signed in.
                </AuthStep>
            </AuthPanel>
        )
    }
}

export default LoginPage
