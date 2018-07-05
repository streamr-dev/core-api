// @flow

import * as React from 'react'
import { Link } from 'react-router-dom'
import cx from 'classnames'

import AuthPanel, { styles as authPanelStyles } from '../../shared/AuthPanel'
import Input from '../../shared/Input'
import Actions from '../../shared/Actions'
import Button from '../../shared/Button'
import AuthStep from '../../shared/AuthStep'

import withAuthFlow from '../../shared/withAuthFlow'
import { onInputChange } from '../../shared/utils'
import schemas from '../../schemas/forgotPassword'
import type { AuthFlowProps } from '../../shared/types'

type Props = AuthFlowProps & {
    form: {
        email: string,
    },
}

const ForgotPasswordPage = ({ setIsProcessing, isProcessing, step, form, errors, setFieldError, next, prev, setFormField }: Props) => (
    <AuthPanel
        currentStep={step}
        form={form}
        onPrev={prev}
        onNext={next}
        setIsProcessing={setIsProcessing}
        validationSchemas={schemas}
        onValidationError={setFieldError}
    >
        <AuthStep title="Get a link to reset your password">
            <Input
                name="email"
                label="Email"
                value={form.email}
                onChange={onInputChange(setFormField)}
                error={errors.email}
                processing={step === 0 && isProcessing}
                autocomplete="email"
            />
            <input type="password" name="password" style={{
                display: 'none',
            }} />
            <Actions>
                <Button disabled={isProcessing}>Send</Button>
            </Actions>
        </AuthStep>
        <AuthStep title="Link sent">
            <p className={cx(authPanelStyles.spaceLarge, 'text-center')}>
                If a user with that email exists, we have sent a link to reset the password.
                Please check your email and click the link — it may be in your spam folder!
            </p>
            <p>
                <Link to="/register/resetPassword">Reset</Link>
            </p>
        </AuthStep>
    </AuthPanel>
)

export default withAuthFlow(ForgotPasswordPage, 0, {
    email: '',
})
