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
import { onInputChange } from '../../shared/utils'
import schemas from '../../schemas/login'
import styles from './loginPage.pcss'

type Props = AuthFlowProps & {
    form: {
        email: string,
        password: string,
        rememberMe: boolean,
    },
}

const LoginPage = ({ setIsProcessing, isProcessing, step, form: { email, password, rememberMe }, errors, setFieldError, next, prev, setFormField }: Props) => (
    <AuthPanel
        currentStep={step}
        onPrev={prev}
        onNext={next}
        onProcessing={setIsProcessing}
        validationSchemas={schemas}
        onValidationError={setFieldError}
    >
        <AuthStep title="Sign In" showEth showSignup>
            <Input
                name="email"
                label="Email"
                value={email}
                onChange={onInputChange(setFormField)}
                error={errors.email}
                processing={step === 0 && isProcessing}
                autoComplete="email"
            />
            <Actions>
                <Button disabled={isProcessing}>Next</Button>
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
                processing={step === 1 && isProcessing}
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
                <Button className={styles.button} disabled={isProcessing}>Go</Button>
            </Actions>
        </AuthStep>
    </AuthPanel>
)

export default withAuthFlow(LoginPage, 0, {
    email: '',
    password: '',
    rememberMe: false,
})
