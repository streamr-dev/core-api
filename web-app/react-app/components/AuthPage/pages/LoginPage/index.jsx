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

const LoginPage = ({ processing, step, form: { email, password, rememberMe }, errors, next, prev, attach, setFormField }: Props) => {
    const onNextClick = preventDefault(() => next(schemas))

    return (
        <AuthPanel currentStep={step} onBack={prev} ref={attach}>
            <AuthStep title="Sign In" showEth showSignup>
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
            <AuthStep title="Sign In" showBack>
                <Input
                    name="password"
                    type="password"
                    label="Password"
                    value={password}
                    onChange={onInputChange(setFormField)}
                    error={errors.password}
                    processing={step === 1 && processing}
                />
                <Actions>
                    <Checkbox
                        name="rememberMe"
                        checked={rememberMe}
                        onChange={onInputChange(setFormField)}
                    >
                        Remember me
                    </Checkbox>
                    <Link to="/password/new">Forgot your password?</Link>
                    <Button onClick={onNextClick} disabled={processing}>Go</Button>
                </Actions>
            </AuthStep>
            <AuthStep title="Done" showBack>
                Signed in.
            </AuthStep>
        </AuthPanel>
    )
}

export default withAuthFlow(LoginPage, {
    email: '',
    password: '',
    rememberMe: false,
})
