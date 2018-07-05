// @flow

import * as React from 'react'
import cx from 'classnames'

import AuthPanel, { styles as authPanelStyles } from '../../shared/AuthPanel'
import Input from '../../shared/Input'
import Actions from '../../shared/Actions'
import Button from '../../shared/Button'
import Checkbox from '../../shared/Checkbox'
import AuthStep from '../../shared/AuthStep'

import createLink from '../../../../utils/createLink'
import withAuthFlow from '../../shared/withAuthFlow'
import { onInputChange } from '../../shared/utils'
import schemas from '../../schemas/signup'
import type { AuthFlowProps } from '../../shared/types'
import qs from 'qs'
import axios from 'axios/index'

type Props = AuthFlowProps & {
    form: {
        email: string,
        password: string,
        confirmPassword: string,
        timezone: string,
        toc: boolean,
    },
}

const registerUrl = createLink('auth/signup')
const inputNames = {
    email: 'username',
}

class RegisterPage extends React.Component<Props> {
    submit = () => new Promise((resolve, reject) => {
        const { email } = this.props.form
        const data = {
            [inputNames.email]: email,
        }
        axios({
            method: 'post',
            url: registerUrl,
            data: qs.stringify(data),
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
        })
            .then(({ data }) => {
                if (data.error) {
                    this.onFailure(new Error(data.error))
                    reject()
                } else {
                    this.onSuccess()
                    resolve()
                }
            })
    })

    onSuccess = () => {
        /* noop */
    }

    onFailure = (error: Error) => {
        const { setFieldError } = this.props
        setFieldError('email', error.message)
    }

    render() {
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
                <AuthStep
                    title="Sign up"
                    showEth={false}
                    onSubmit={this.submit}
                    onSuccess={this.onSuccess}
                    onFailure={this.onFailure}
                    showSignin
                >
                    <Input
                        name="email"
                        label="Email"
                        value={form.email}
                        onChange={onInputChange(setFormField)}
                        error={errors.email}
                        processing={step === 0 && isProcessing}
                        autoComplete="off"
                    />
                    <Actions>
                        <Button disabled={isProcessing}>Next</Button>
                    </Actions>
                </AuthStep>
                <AuthStep title="Thanks for signing up!" showSignin>
                    <div className={cx(authPanelStyles.spaceLarge, 'text-center')}>
                        <p>We have sent a sign up link to your email.</p>
                        <p>Please click it to finish your registration.</p>
                    </div>
                </AuthStep>
            </AuthPanel>
        )
    }
}

export default withAuthFlow(RegisterPage, 0, {
    email: '',
})
