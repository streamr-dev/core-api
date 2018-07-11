// @flow

import * as React from 'react'
import cx from 'classnames'

import AuthPanel, { styles as authPanelStyles } from '../../shared/AuthPanel'
import Input from '../../shared/Input'
import Actions from '../../shared/Actions'
import Button from '../../shared/Button'
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

class SignupPage extends React.Component<Props> {
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
            .then(() => {
                resolve()
            })
            .catch(({ response }) => {
                const { data } = response
                this.onFailure(new Error(data.error || 'Something went wrong'))
                reject()
            })
    })

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
                        autoFocus
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

export default withAuthFlow(SignupPage, 0, {
    email: '',
})
