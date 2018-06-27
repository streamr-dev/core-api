// @flow

import * as React from 'react'
import cx from 'classnames'

import AuthPanel from '../../shared/AuthPanel'
import Input from '../../shared/Input'
import Actions from '../../shared/Actions'
import Button from '../../shared/Button'
import Checkbox from '../../shared/Checkbox'
import AuthStep from '../../shared/AuthStep'

import withAuthFlow, { type AuthFlowProps } from '../../shared/withAuthFlow'
import { preventDefault, onInputChange } from '../../shared/utils'
import schemas from '../../schemas/register'

type Props = AuthFlowProps & {
    form: {
        email: string,
        password: string,
        confirmPassword: string,
        timezone: string,
        toc: boolean,
    },
}

const RegisterPage = ({ processing, step, form: { email, password, confirmPassword, timezone, toc }, errors, next, prev, attach, setFormField }: Props) => (
    <AuthPanel currentStep={step} onBack={prev} ref={attach} onProceed={preventDefault(next, schemas)}>
        <AuthStep title="Sign up" showEth showSignin>
            <Input
                name="email"
                label="Email"
                value={email}
                onChange={onInputChange(setFormField)}
                error={errors.email}
                processing={step === 0 && processing}
                autoComplete="off"
            />
            <Actions>
                <Button disabled={processing}>Next</Button>
            </Actions>
        </AuthStep>
        <AuthStep title="Sign up" showBack>
            <Input
                name="password"
                type="password"
                label="Create a Password"
                value={password}
                onChange={onInputChange(setFormField)}
                error={errors.password}
                processing={step === 1 && processing}
                autoComplete="new-password"
                meastureStrength
            />
            <Actions>
                <Button disabled={processing}>Next</Button>
            </Actions>
        </AuthStep>
        <AuthStep title="Sign up" showBack>
            <Input
                name="confirmPassword"
                type="password"
                label="Confirm your password"
                value={confirmPassword}
                onChange={onInputChange(setFormField)}
                error={errors.confirmPassword}
                processing={step === 2 && processing}
                autoComplete="new-password"
            />
            <Actions>
                <Button disabled={processing}>Next</Button>
            </Actions>
        </AuthStep>
        <AuthStep title="Timezone" showBack>
            <Input
                name="timezone"
                type="text"
                label="Your timezone"
                value={timezone}
                onChange={onInputChange(setFormField)}
                error={errors.timezone}
                processing={step === 3 && processing}
            />
            <Actions>
                <Button disabled={processing}>Next</Button>
            </Actions>
        </AuthStep>
        <AuthStep title="Terms">
            <div className={cx(AuthPanel.styles.spaceMedium, AuthPanel.styles.centered)}>
                <Checkbox
                    name="toc"
                    checked={toc}
                    onChange={onInputChange(setFormField)}
                >
                    I agree with the <a href="#">terms and conditions</a>, and <a href="#">privacy policy</a>.
                </Checkbox>
            </div>
            <Actions>
                <Button disabled={processing}>Finish</Button>
            </Actions>
        </AuthStep>
        <AuthStep title="Thanks for signing up!" showSignin>
            <div className={cx(AuthPanel.styles.spaceLarge, 'text-center')}>
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
