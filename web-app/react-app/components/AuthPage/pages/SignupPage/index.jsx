// @flow

import * as React from 'react'

import AuthPanel from '../../shared/AuthPanel'
import TextInput from '../../shared/TextInput'
import Actions from '../../shared/Actions'
import Button from '../../shared/Button'
import AuthStep, { styles as stepStyles } from '../../shared/AuthStep'

import createLink from '../../../../utils/createLink'
import withAuthFlow from '../../shared/withAuthFlow'
import { post } from '../../shared/utils'
import schemas from '../../schemas/signup'
import type { AuthFlowProps } from '../../shared/types'

type Props = AuthFlowProps & {
    form: {
        email: string,
        password: string,
        confirmPassword: string,
        timezone: string,
        toc: boolean,
    },
}

class SignupPage extends React.Component<Props> {
    submit = () => {
        const url = createLink('api/v1/signups')
        const { email: username } = this.props.form

        return post(url, {
            username,
        }, false, false)
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
                isProcessing={isProcessing}
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
                    <TextInput
                        name="email"
                        label="Email"
                        value={form.email}
                        onChange={setFormField}
                        error={errors.email}
                        processing={step === 0 && isProcessing}
                        autoComplete="off"
                        autoFocus
                    />
                    <Actions>
                        <Button disabled={isProcessing}>Next</Button>
                    </Actions>
                </AuthStep>
                <AuthStep
                    title="Thanks for signing up!"
                    showSignin
                    className={stepStyles.spaceLarge}
                >
                    <p>We have sent a sign up link to your email.</p>
                    <p>Please click it to finish your registration.</p>
                </AuthStep>
            </AuthPanel>
        )
    }
}

export default withAuthFlow(SignupPage, 0, {
    email: '',
})
