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
import * as schemas from '../../schemas/login'

type Props = {}

type State = {
    form: {
        email: string,
        password: string,
        rememberMe: boolean,
    },
    errors: {
        [string]: string,
    },
}

class LoginPage extends React.Component<Props, State> {
    state = {
        form: {
            email: '',
            password: '',
            rememberMe: false,
        },
        errors: {},
    }

    validate = (field: string) => () => schemas[field].validate(this.state.form)

    onInputChange = (e: SyntheticInputEvent<EventTarget>) => {
        const { form, errors: prevErrors } = this.state
        const field = e.target.name
        const value = e.target.type === 'checkbox' ? e.target.checked : e.target.value
        const errors = {
            ...prevErrors,
        }

        delete errors[field]

        this.setState({
            form: {
                ...form,
                [field]: value,
            },
            errors,
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

    render = () => {
        const { form: { email, password, rememberMe }, errors } = this.state

        return (
            <AuthPanel onValidationError={this.onValidationError}>
                <AuthStep title="Sign In" showEth showSignup validate={this.validate('email')}>
                    <Input
                        name="email"
                        placeholder="Email"
                        value={email}
                        onChange={this.onInputChange}
                        error={errors.email}
                    />
                    <Actions>
                        <Button proceed>Next</Button>
                    </Actions>
                </AuthStep>
                <AuthStep title="Sign In" showBack validate={this.validate('password')}>
                    <Input
                        name="password"
                        type="password"
                        placeholder="Password"
                        value={password}
                        onChange={this.onInputChange}
                        error={errors.password}
                    />
                    <Actions>
                        <Checkbox
                            name="rememberMe"
                            checked={rememberMe}
                            onChange={this.onInputChange}
                        >
                            Remember me
                        </Checkbox>
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
