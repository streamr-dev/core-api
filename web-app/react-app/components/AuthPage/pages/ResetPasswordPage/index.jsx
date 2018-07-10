// @flow

import * as React from 'react'
import cx from 'classnames'

import AuthPanel, { styles as authPanelStyles } from '../../shared/AuthPanel'
import Input from '../../shared/Input'
import Actions from '../../shared/Actions'
import Button from '../../shared/Button'
import AuthStep from '../../shared/AuthStep'

import withAuthFlow from '../../shared/withAuthFlow'
import { onInputChange } from '../../shared/utils'
import schemas from '../../schemas/resetPassword'
import type { AuthFlowProps } from '../../shared/types'
import qs from 'qs'
import createLink from '../../../../utils/createLink'
import axios from 'axios/index'

type Props = AuthFlowProps & {
    match: {
        params: {
            redirect?: ?string,
        },
    },
    history: {
        replace: (string) => void,
    },
    location: {
        pathname: string,
        search: string,
    },
    form: {
        password: string,
        confirmPassword: string,
    },
}

type State = {
    token: ?string,
}

const resetPasswordUrl = createLink('auth/resetPassword')
const defaultRedirectUrl = createLink('canvas/editor')
const inputNames = {
    password: 'password',
    confirmPassword: 'password2',
    token: 't',
}

class ResetPasswordPage extends React.Component<Props, State> {
    constructor(props) {
        super(props)
        const { t } = qs.parse(this.props.location.search.slice(1))
        this.state = {
            token: t || null,
        }
        if (t) {
            // TODO: uncomment
            // props.history.replace(props.location.pathname)
        } else {
            props.setFieldError('password', 'A token is needed. Please go back to the email you received, and click the click again.')
        }
    }

    submit = () => new Promise((resolve, reject) => {
        const { password, confirmPassword } = this.props.form
        const { token } = this.state
        const data = {
            [inputNames.password]: password,
            [inputNames.confirmPassword]: confirmPassword,
            [inputNames.token]: token,
        }
        axios({
            method: 'post',
            url: resetPasswordUrl,
            data: qs.stringify(data),
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Requested-With': 'XMLHttpRequest', // Required
            },
        })
            .then(() => {
                resolve()
            })
            .catch(({ response }) => {
                const { data } = response
                this.onFailure(new Error(data.error || 'Something went wrong'))
                reject()
            })
    })

    onSuccess = () => {
        const redirectUrl = this.props.match.params.redirect || defaultRedirectUrl
        window.location.assign(redirectUrl)
    }

    onFailure = (error: Error) => {
        const { setFieldError } = this.props
        setFieldError('confirmPassword', error.message)
    }

    render() {
        const { setIsProcessing, isProcessing, step, form, errors, setFieldError, next, prev, setFormField } = this.props
        const { token } = this.state
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
                <AuthStep title="Reset password">
                    <Input
                        name="password"
                        type="password"
                        label="Create a Password"
                        value={form.password}
                        onChange={onInputChange(setFormField)}
                        error={errors.password}
                        processing={step === 0 && isProcessing}
                        autoComplete="new-password"
                        disabled={!token}
                        measureStrength
                        autoFocus
                    />
                    <Actions>
                        <Button disabled={isProcessing}>Next</Button>
                    </Actions>
                </AuthStep>
                <AuthStep
                    title="Reset password"
                    onSubmit={this.submit}
                    onSuccess={this.onSuccess}
                    onFailure={this.onFailure}
                    showBack
                >
                    <Input
                        name="confirmPassword"
                        type="password"
                        label="Confirm your password"
                        value={form.confirmPassword}
                        onChange={onInputChange(setFormField)}
                        error={errors.confirmPassword}
                        processing={step === 1 && isProcessing}
                        autoComplete="new-password"
                        autoFocus
                    />
                    <Actions>
                        <Button disabled={isProcessing}>Next</Button>
                    </Actions>
                </AuthStep>
            </AuthPanel>
        )
    }
}

export default withAuthFlow(ResetPasswordPage, 0, {
    password: '',
    confirmPassword: '',
})
