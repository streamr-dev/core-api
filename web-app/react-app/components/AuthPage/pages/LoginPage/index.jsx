// @flow

import * as React from 'react'
import { Link } from 'react-router-dom'

import AuthPanel from '../../shared/AuthPanel'
import TextInput from '../../shared/TextInput'
import Actions from '../../shared/Actions'
import Button from '../../shared/Button'
import Checkbox from '../../shared/Checkbox'
import AuthStep from '../../shared/AuthStep'

import createLink from '../../../../utils/createLink'
import withAuthFlow from '../../shared/withAuthFlow'
import { onInputChange } from '../../shared/utils'
import schemas from '../../schemas/login'
import styles from './loginPage.pcss'
import type { AuthFlowProps } from '../../shared/types'
import { post } from '../../shared/utils'

type Props = AuthFlowProps & {
    form: {
        email: string,
        password: string,
        rememberMe: boolean,
    },
}

// NOTE: Spring security service requires its own input names

class LoginPage extends React.Component<Props> {
    submit = () => {
        const url = createLink('j_spring_security_check')
        const { email: j_username, password: j_password, rememberMe } = this.props.form

        return post(url, {
            j_username,
            j_password,
            ...(rememberMe ? {
                _spring_security_remember_me: 'on',
            } : {})
        }, true, true)
    }

    onFailure = (error: Error) => {
        const { setFieldError } = this.props
        setFieldError('password', error.message)
    }

    render = () => {
        const { setIsProcessing, isProcessing, step, form, errors, setFieldError, next, prev, setFormField, redirect } = this.props

        return (
            <AuthPanel
                currentStep={step}
                form={form}
                onPrev={prev}
                onNext={next}
                setIsProcessing={setIsProcessing}
                isProcessing={isProcessing}
                validationSchemas={schemas}
                onValidationError={setFieldError}
            >
                <AuthStep title="Sign in" showSignup autoSubmitOnChange={['hiddenPassword']}>
                    <TextInput
                        name="email"
                        label="Email"
                        value={form.email}
                        onChange={setFormField}
                        error={errors.email}
                        processing={step === 0 && isProcessing}
                        autoComplete="email"
                        className={styles.emailInput}
                        autoFocus
                    />
                    <input
                        name="hiddenPassword"
                        type="password"
                        onChange={(e) => {
                            onInputChange(setFormField, 'password')(e)
                        }}
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
                    title="Sign in"
                    showBack
                    onSubmit={this.submit}
                    onSuccess={redirect}
                    onFailure={this.onFailure}
                >
                    <input
                        name="email"
                        type="text"
                        value={form.email}
                        style={{
                            display: 'none',
                        }}
                    />
                    <TextInput
                        name="password"
                        type="password"
                        label="Password"
                        value={form.password}
                        onChange={setFormField}
                        error={errors.password}
                        processing={step === 1 && isProcessing}
                        autoComplete="current-password"
                        className={styles.passwordInput}
                        autoFocus
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
            </AuthPanel>
        )
    }
}

export default withAuthFlow(LoginPage, 0, {
    email: '',
    password: '',
    rememberMe: false,
})
