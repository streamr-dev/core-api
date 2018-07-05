// @flow

import * as React from 'react'
import { Link } from 'react-router-dom'

import AuthPanel, { styles as authPanelStyles } from '../../shared/AuthPanel'
import Input from '../../shared/Input'
import Actions from '../../shared/Actions'
import Button from '../../shared/Button'
import Checkbox from '../../shared/Checkbox'
import AuthStep from '../../shared/AuthStep'

import withAuthFlow from '../../shared/withAuthFlow'
import { onInputChange } from '../../shared/utils'
import schemas from '../../schemas/login'
import styles from './loginPage.pcss'
import type { AuthFlowProps } from '../../shared/types'

type Props = AuthFlowProps & {
    form: {
        email: string,
        password: string,
        rememberMe: boolean,
    },
}

class LoginPage extends React.Component<Props> {
    submit = () => new Promise((resolve, reject) => {
        // NOTE: Placeholder for axios.post(…) or some other
        //       async puppy.
        setTimeout(() => {
            // resolve()
            const { password } = this.props.form
            if (password === 'qwerty123') {
                resolve()
            } else {
                reject(new Error('Invalid creds.'))
            }
        }, 1000)
    })

    onSuccess = () => {
        /* noop */
    }

    onFailure = (error: Error) => {
        const { setFieldError } = this.props
        setFieldError('password', error.message)
    }

    render = () => {
        const { setIsProcessing, isProcessing, step, form, errors, setFieldError, next, prev, setFormField } = this.props

        return (
            <AuthPanel
                currentStep={step}
                form={form}
                onPrev={prev}
                onNext={next}
                setIsProcessing={setIsProcessing}
                validationSchemas={schemas}
                onValidationError={setFieldError}
            >
                <AuthStep title="Sign In" showEth showSignup>
                    <Input
                        name="email"
                        label="Email"
                        value={form.email}
                        onChange={onInputChange(setFormField)}
                        error={errors.email}
                        processing={step === 0 && isProcessing}
                        autoComplete="email"
                    />
                    <input
                        name="password"
                        type="password"
                        value={form.password}
                        style={{
                            display: 'none',
                        }}
                    />
                    <Actions>
                        <Button disabled={isProcessing}>Next</Button>
                    </Actions>
                </AuthStep>
                <AuthStep
                    title="Sign In"
                    showBack
                    onSubmit={this.submit}
                    onSuccess={this.onSuccess}
                    onFailure={this.onFailure}
                >
                    <input
                        type="email"
                        name="email"
                        value={form.email}
                        style={{
                            display: 'none',
                        }}
                    />
                    <Input
                        name="password"
                        type="password"
                        label="Password"
                        value={form.password}
                        onChange={onInputChange(setFormField)}
                        error={errors.password}
                        processing={step === 1 && isProcessing}
                        autoComplete="current-password"
                    />
                    <Actions>
                        <Checkbox
                            name="rememberMe"
                            checked={form.rememberMe}
                            onChange={onInputChange(setFormField)}
                        >
                            Remember me
                        </Checkbox>
                        <Link to="/register/forgotPassword">Forgot your password?</Link>
                        <Button className={styles.button} disabled={isProcessing}>Go</Button>
                    </Actions>
                </AuthStep>
                <AuthStep title="Logged in?">
                    <p className={authPanelStyles.spaceLarge}>
                        Remove this step and redirect in `onSuccess` prop/callback.
                    </p>
                </AuthStep>
            </AuthPanel>
        )
    }
}

export default withAuthFlow(LoginPage, 0, {
    email: '',
    password: '',
    rememberMe: false,
})
