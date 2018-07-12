// @flow

import * as React from 'react'
import * as yup from 'yup'

import AuthPanel from '../../shared/AuthPanel'
import Input from '../../shared/Input'
import Actions from '../../shared/Actions'
import Button from '../../shared/Button'
import AuthStep from '../../shared/AuthStep'

import withAuthFlow from '../../shared/withAuthFlow'
import { onInputChange, post } from '../../shared/utils'
import schemas from '../../schemas/resetPassword'
import type { AuthFlowProps } from '../../shared/types'
import qs from 'qs'
import createLink from '../../../../utils/createLink'

type Props = AuthFlowProps & {
    location: {
        search: string,
    },
    form: {
        password: string,
        confirmPassword: string,
        token: string,
    },
}

class ResetPasswordPage extends React.Component<Props> {
    submit = () => {
        const url = createLink('auth/resetPassword')
        const { password, confirmPassword: password2, token: t } = this.props.form

        return post(url, {
            password,
            password2,
            t,
        }, false, false)
    }

    onFailure = (error: Error) => {
        const { setFieldError } = this.props
        setFieldError('confirmPassword', error.message)
    }

    componentDidMount = () => {
        const { setFormField, location: { search }, setFieldError } = this.props

        setFormField('token', qs.parse(search, {
            ignoreQueryPrefix: true,
        }).token || '', () => {
            yup
                .object()
                .shape({
                    token: yup.reach(schemas[0], 'token'),
                })
                .validate(this.props.form)
                .then(() => {}, (error: yup.ValidationError) => {
                    setFieldError('password', error.message)
                })
        })
    }

    render() {
        const { setIsProcessing, isProcessing, step, form, errors, setFieldError, next, prev, setFormField, redirect } = this.props

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
                        disabled={!form.token}
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
                    onSuccess={redirect}
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
    token: '',
})
