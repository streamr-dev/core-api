// @flow

import * as React from 'react'
import cx from 'classnames'

import AuthPanel, { styles as authPanelStyles } from '../../shared/AuthPanel'
import Input from '../../shared/Input'
import Actions from '../../shared/Actions'
import Button from '../../shared/Button'
import Checkbox from '../../shared/Checkbox'
import AuthStep from '../../shared/AuthStep'

import withAuthFlow from '../../shared/withAuthFlow'
import { onInputChange } from '../../shared/utils'
import schemas from '../../schemas/register'
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

const RegisterPage = ({ setIsProcessing, isProcessing, step, form, errors, setFieldError, next, prev, setFormField }: Props) => (
    <AuthPanel
        currentStep={step}
        form={form}
        onPrev={prev}
        onNext={next}
        setIsProcessing={setIsProcessing}
        validationSchemas={schemas}
        onValidationError={setFieldError}
    >
        <AuthStep title="Sign up" showEth showSignin>
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
        <AuthStep title="Sign up" showBack>
            <Input
                name="password"
                type="password"
                label="Create a Password"
                value={form.password}
                onChange={onInputChange(setFormField)}
                error={errors.password}
                processing={step === 1 && isProcessing}
                autoComplete="new-password"
                meastureStrength
            />
            <Actions>
                <Button disabled={isProcessing}>Next</Button>
            </Actions>
        </AuthStep>
        <AuthStep title="Sign up" showBack>
            <Input
                name="confirmPassword"
                type="password"
                label="Confirm your password"
                value={form.confirmPassword}
                onChange={onInputChange(setFormField)}
                error={errors.confirmPassword}
                processing={step === 2 && isProcessing}
                autoComplete="new-password"
            />
            <Actions>
                <Button disabled={isProcessing}>Next</Button>
            </Actions>
        </AuthStep>
        <AuthStep title="Timezone" showBack>
            <Input
                name="timezone"
                type="text"
                label="Your timezone"
                value={form.timezone}
                onChange={onInputChange(setFormField)}
                error={errors.timezone}
                processing={step === 3 && isProcessing}
            />
            <Actions>
                <Button disabled={isProcessing}>Next</Button>
            </Actions>
        </AuthStep>
        <AuthStep title="Terms">
            <div className={cx(authPanelStyles.spaceMedium, authPanelStyles.centered)}>
                <Checkbox
                    name="toc"
                    checked={form.toc}
                    onChange={onInputChange(setFormField)}
                >
                    I agree with the <a href="#">terms and conditions</a>, and <a href="#">privacy policy</a>.
                </Checkbox>
            </div>
            <Actions>
                <Button disabled={isProcessing}>Finish</Button>
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

export default withAuthFlow(RegisterPage, 0, {
    email: '',
    password: '',
    confirmPassword: '',
    timezone: '',
    toc: false,
})
