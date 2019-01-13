// @flow

import * as React from 'react'
import * as yup from 'yup'

import AuthPanel from '../../shared/AuthPanel'
import TextInput from '../../shared/TextInput'
import Actions from '../../shared/Actions'
import Button from '../../shared/Button'
import AuthStep from '../../shared/AuthStep'

import withAuthFlow from '../../shared/withAuthFlow'
import { post } from '../../shared/utils'
import schemas from '../../schemas/resetPassword'
import type { AuthFlowProps } from '../../shared/types'
import qs from 'qs'
import createLink from '../../../../utils/createLink'

type Props = AuthFlowProps & {
    history: {
        replace: (string) => void,
    },
    location: {
        search: string,
        pathname: string,
    },
    form: {
        password: string,
        confirmPassword: string,
        token: string,
    },
}

class ResetPasswordPage extends React.Component<Props> {
    constructor(props: Props) {
        super(props)
        const { setFormField, location: { search }, setFieldError } = props
        const token = qs.parse(search, {
            ignoreQueryPrefix: true,
        }).t || ''

        setFormField('token', token, () => {
            yup
                .object()
                .shape({
                    token: yup.reach(schemas[0], 'token'),
                })
                .validate(this.props.form)
                .then(
                    () => {
                        // To make sure that the resetPassword token doesn't stick in the browser history
                        props.history.replace(props.location.pathname)
                    },
                    (error: yup.ValidationError) => {
                        setFieldError('password', error.message)
                    }
                )
        })
    }

    submit = () => {
        const url = createLink('api/v1/passwords')
        const loginUrl = createLink('j_spring_security_check')
        const { password, confirmPassword: password2, token: t } = this.props.form

        return post(url, {
            password,
            password2,
            t,
        }, false, false).then(({ username: j_username }) => post(loginUrl, {
            j_username,
            j_password: password,
        }, true, true))
    }

    onFailure = (error: Error) => {
        const { setFieldError } = this.props
        setFieldError('confirmPassword', error.message)
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
                isProcessing={isProcessing}
                validationSchemas={schemas}
                onValidationError={setFieldError}
            >
                <AuthStep title="Reset password">
                    <TextInput
                        name="password"
                        type="password"
                        label="Create a Password"
                        value={form.password}
                        onChange={setFormField}
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
                    <TextInput
                        name="confirmPassword"
                        type="password"
                        label="Confirm your password"
                        value={form.confirmPassword}
                        onChange={setFormField}
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
