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
import schemas from '../../schemas/login'

type Props = {}

type State = {
    step: number,
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
    panel: ?AuthPanel

    state = {
        step: 0,
        form: {
            email: '',
            password: '',
            rememberMe: false,
        },
        errors: {},
    }

    setPanel = (panel: ?AuthPanel) => {
        this.panel = panel
    }

    numSteps = () => (this.panel ? React.Children.count(this.panel.props.children) : 0)

    validateCurrentStep = () => (schemas[this.state.step] || yup.object()).validate(this.state.form)

    onBack = () => {
        this.setState({
            step: Math.max(0, this.state.step - 1),
        })
    }

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

    onNextClick = (e: SyntheticInputEvent<EventTarget>) => {
        const { step, errors } = this.state
        e.persist()
        e.target.disabled = true
        e.preventDefault()

        this.validateCurrentStep()
            .then(() => {
                this.setState({
                    step: Math.min(this.numSteps(), step + 1),
                })
            }, (error: yup.ValidationError) => {
                this.setState({
                    errors: {
                        ...errors,
                        [error.path]: error.message,
                    },
                })
            })
            .finally(() => {
                e.target.disabled = false
            })
    }

    render = () => {
        const { step, form: { email, password, rememberMe }, errors } = this.state

        return (
            <AuthPanel currentStep={step} onBack={this.onBack} ref={this.setPanel}>
                <AuthStep title="Sign In" showEth showSignup>
                    <Input
                        name="email"
                        placeholder="Email"
                        value={email}
                        onChange={this.onInputChange}
                        error={errors.email}
                    />
                    <Actions>
                        <Button onClick={this.onNextClick}>Next</Button>
                    </Actions>
                </AuthStep>
                <AuthStep title="Sign In" showBack>
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
                        <Link to="/password/new">Forgot your password?</Link>
                        <Button onClick={this.onNextClick}>Go</Button>
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
