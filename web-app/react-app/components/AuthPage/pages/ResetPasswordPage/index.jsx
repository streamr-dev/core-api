// @flow

import * as React from 'react'
import cx from 'classnames'

import AuthPanel from '../../shared/AuthPanel'
import Input from '../../shared/Input'
import Actions from '../../shared/Actions'
import Button from '../../shared/Button'
import AuthStep from '../../shared/AuthStep'

import withAuthFlow, { type AuthFlowProps } from '../../shared/withAuthFlow'
import { preventDefault, onInputChange } from '../../shared/utils'
import schemas from '../../schemas/resetPassoword'

type Props = AuthFlowProps & {
    form: {
        email: string,
        password: string,
        confirmPassword: string,
    },
}

const ResetPasswordPage = ({ processing, step, form: { email, password, confirmPassword }, errors, next, prev, attach, setFormField }: Props) => {
    const onNextClick = preventDefault(() => next(schemas))

    return (
        <AuthPanel currentStep={step} onBack={prev} ref={attach}>
            <AuthStep title="Reset password">
                <Input
                    name="email"
                    label="Email"
                    value={email}
                    onChange={onInputChange(setFormField)}
                    error={errors.email}
                    processing={step === 0 && processing}
                />
                <Actions>
                    <Button onClick={onNextClick} disabled={processing}>
                        Next
                    </Button>
                </Actions>
            </AuthStep>
            <AuthStep title="Reset password">
                <Input
                    name="password"
                    type="password"
                    label="Create a Password"
                    value={password}
                    onChange={onInputChange(setFormField)}
                    error={errors.password}
                    processing={step === 1 && processing}
                />
                <Actions>
                    <Button onClick={onNextClick} disabled={processing}>Next</Button>
                </Actions>
            </AuthStep>
            <AuthStep title="Reset password" showBack>
                <Input
                    name="confirmPassword"
                    type="password"
                    label="Confirm your password"
                    value={confirmPassword}
                    onChange={onInputChange(setFormField)}
                    error={errors.confirmPassword}
                    processing={step === 2 && processing}
                />
                <Actions>
                    <Button onClick={onNextClick} disabled={processing}>Next</Button>
                </Actions>
            </AuthStep>
            <AuthStep title="Done." showSignin>
                <div className={cx(AuthPanel.styles.spaceLarge, 'text-center')}>
                    <p>Done.</p>
                </div>
            </AuthStep>
        </AuthPanel>
    )
}

export default withAuthFlow(ResetPasswordPage, 0, {
    email: '',
    password: '',
    confirmPassword: '',
})
