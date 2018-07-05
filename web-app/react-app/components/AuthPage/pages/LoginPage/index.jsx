// @flow

import * as React from 'react'
import { Link } from 'react-router-dom'

import AuthPanel from '../../shared/AuthPanel'
import Input from '../../shared/Input'
import Actions from '../../shared/Actions'
import Button from '../../shared/Button'
import Checkbox from '../../shared/Checkbox'
import AuthStep from '../../shared/AuthStep'

import withAuthFlow from '../../shared/withAuthFlow'
import { onInputChange } from '../../shared/utils'
import schemas from '../../schemas/login'
import styles from './loginPage.pcss'
import type { AuthFlowProps } from '../../shared/types'

type Props = AuthFlowProps & {
    form: {
        email: string,
        password: string,
        rememberMe: boolean,
    },
}

const LoginPage = ({ setIsProcessing, isProcessing, step, form, errors, setFieldError, next, prev, setFormField }: Props) => (
    <AuthPanel
        currentStep={step}
        form={form}
        onPrev={prev}
        onNext={next}
        setIsProcessing={setIsProcessing}
        validationSchemas={schemas}
        onValidationError={setFieldError}
    >
        <AuthStep title="Sign In" showEth showSignup>
            <Input
                name="email"
                label="Email"
                value={form.email}
                onChange={onInputChange(setFormField)}
                error={errors.email}
                processing={step === 0 && isProcessing}
                autoComplete="email"
            />
            <input
                name="password"
                type="password"
                value={form.password}
                style={{
                    display: 'none',
                }}
            />
            <Actions>
                <Button disabled={isProcessing}>Next</Button>
            </Actions>
        </AuthStep>
        <AuthStep title="Sign In" showBack>
            <input
                type="email"
                name="email"
                value={form.email}
                style={{
                    display: 'none',
                }}
            />
            <Input
                name="password"
                type="password"
                label="Password"
                value={form.password}
                onChange={onInputChange(setFormField)}
                error={errors.password}
                processing={step === 1 && isProcessing}
                autoComplete="current-password"
            />
            <Actions>
                <Checkbox
                    name="rememberMe"
                    checked={form.rememberMe}
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
