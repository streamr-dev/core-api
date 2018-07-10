// @flow

import * as React from 'react'
import axios from 'axios'
import qs from 'qs'
import { Link } from 'react-router-dom'
import { debounce } from 'lodash'

import AuthPanel from '../../shared/AuthPanel'
import Input from '../../shared/Input'
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

// Spring security service requires its own input names
const loginUrl = createLink('j_spring_security_check')
const defaultRedirectUrl = createLink('canvas/editor')
const inputNames = {
    email: 'j_username',
    password: 'j_password',
    rememberMe: '_spring_security_remember_me',
}

type Props = AuthFlowProps & {
    match: {
        params: {
            redirect?: ?string,
        },
    },
    form: {
        email: string,
        password: string,
        rememberMe: boolean,
    },
}

class LoginPage extends React.Component<Props> {
    submit = () => new Promise((resolve, reject) => {
        const { email, password, rememberMe } = this.props.form
        const data = {
            [inputNames.email]: email,
            [inputNames.password]: password,
            [inputNames.rememberMe]: rememberMe ? 'on' : undefined,
        }
        axios({
            method: 'post',
            url: loginUrl,
            data: qs.stringify(data),
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Requested-With': 'XMLHttpRequest', // Required
            },
        })
            .then(({ data }) => {
                if (data.error) {
                    this.onFailure(new Error(data.error))
                    reject()
                } else {
                    resolve()
                }
            })
    })

    onSuccess = () => {
        const redirectUrl = this.props.match.params.redirect || defaultRedirectUrl
        window.location.assign(redirectUrl)
    }

    onFailure = (error: Error) => {
        const { setFieldError } = this.props
        setFieldError('password', error.message)
    }

    debouncedNext = debounce(this.props.next, 500)

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
                <AuthStep title="Sign in" showEth={false} showSignup>
                    <Input
                        name="email"
                        label="Email"
                        value={form.email}
                        onChange={onInputChange(setFormField)}
                        error={errors.email}
                        processing={step === 0 && isProcessing}
                        autoComplete="email"
                        className={styles.emailInput}
                    />
                    <input
                        name="hiddenPassword"
                        type="password"
                        onChange={(e) => {
                            onInputChange(setFormField, 'password')(e)
                            this.debouncedNext()
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
                    onSuccess={this.onSuccess}
                    onFailure={this.onFailure}
                >
                    <Input
                        name="password"
                        type="password"
                        label="Password"
                        value={form.password}
                        onChange={onInputChange(setFormField)}
                        error={errors.password}
                        processing={step === 1 && isProcessing}
                        autoComplete="current-password"
                        className={styles.passwordInput}
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
