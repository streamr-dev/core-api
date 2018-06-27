// @flow

import * as React from 'react'
import { Link } from 'react-router-dom'

import AuthPanel from '../../shared/AuthPanel'
import Input from '../../shared/Input'
import Actions from '../../shared/Actions'
import Button from '../../shared/Button'
import Checkbox from '../../shared/Checkbox'
import AuthStep from '../../shared/AuthStep'

import withAuthFlow, { type AuthFlowProps } from '../../shared/withAuthFlow'
import { preventDefault, onInputChange } from '../../shared/utils'
import schemas from '../../schemas/login'

type Props = AuthFlowProps & {
    form: {
        email: string,
        password: string,
        rememberMe: boolean,
    },
}

const LoginPage = ({ processing, step, form: { email, password, rememberMe }, errors, next, prev, attach, setFormField }: Props) => (
    <AuthPanel currentStep={step} onBack={prev} ref={attach} onProceed={preventDefault(next, schemas)}>
        <AuthStep title="Sign In" showEth showSignup>
            <Input
                name="email"
                label="Email"
                value={email}
                onChange={onInputChange(setFormField)}
                error={errors.email}
                processing={step === 0 && processing}
                autoComplete="email"
            />
            <Actions>
                <Button disabled={processing}>Next</Button>
            </Actions>
        </AuthStep>
        <AuthStep title="Sign In" showBack>
            <Input
                name="password"
                type="password"
                label="Password"
                value={password}
                onChange={onInputChange(setFormField)}
                error={errors.password}
                processing={step === 1 && processing}
                autoComplete="current-password"
            />
            <Actions>
                <Checkbox
                    name="rememberMe"
                    checked={rememberMe}
                    onChange={onInputChange(setFormField)}
                >
                    Remember me
                </Checkbox>
                <Link to="/register/forgotPassword">Forgot your password?</Link>
                <Button disabled={processing}>Go</Button>
            </Actions>
        </AuthStep>
        <AuthStep title="Done" showBack>
            Signed in.
        </AuthStep>
    </AuthPanel>
)

export default withAuthFlow(LoginPage, 0, {
    email: '',
    password: '',
    rememberMe: false,
})
