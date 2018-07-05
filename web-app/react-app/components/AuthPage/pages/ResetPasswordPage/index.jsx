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
import schemas from '../../schemas/resetPassoword'
import type { AuthFlowProps } from '../../shared/types'

type Props = AuthFlowProps & {
    form: {
        password: string,
        confirmPassword: string,
    },
}

const ResetPasswordPage = ({ setIsProcessing, isProcessing, step, form, errors, setFieldError, next, prev, setFormField }: Props) => (
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
                measureStrength
            />
            <Actions>
                <Button disabled={isProcessing}>Next</Button>
            </Actions>
        </AuthStep>
        <AuthStep title="Reset password" showBack>
            <Input
                name="confirmPassword"
                type="password"
                label="Confirm your password"
                value={form.confirmPassword}
                onChange={onInputChange(setFormField)}
                error={errors.confirmPassword}
                processing={step === 1 && isProcessing}
                autoComplete="new-password"
            />
            <Actions>
                <Button disabled={isProcessing}>Next</Button>
            </Actions>
        </AuthStep>
        <AuthStep title="Done." showSignin>
            <div className={cx(authPanelStyles.spaceLarge, 'text-center')}>
                <p>Done.</p>
            </div>
        </AuthStep>
    </AuthPanel>
)

export default withAuthFlow(ResetPasswordPage, 0, {
    password: '',
    confirmPassword: '',
})
