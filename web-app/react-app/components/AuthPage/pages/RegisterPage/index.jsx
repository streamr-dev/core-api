// @flow

import * as React from 'react'
import qs from 'qs'
import Select from '../../shared/Select'
import moment from 'moment-timezone'
import * as yup from 'yup'

import AuthPanel from '../../shared/AuthPanel'
import TextInput from '../../shared/TextInput'
import Actions from '../../shared/Actions'
import Button from '../../shared/Button'
import Checkbox from '../../shared/Checkbox'
import AuthStep from '../../shared/AuthStep'

import styles from './registerPage.pcss'
import withAuthFlow from '../../shared/withAuthFlow'
import { onInputChange, post } from '../../shared/utils'
import schemas from '../../schemas/register'
import type { AuthFlowProps } from '../../shared/types'
import createLink from '../../../../utils/createLink'

import links from '../../../../links'

type Props = AuthFlowProps & {
    history: {
        replace: (string) => void,
    },
    location: {
        search: string,
        pathname: string,
    },
    form: {
        email: string,
        password: string,
        confirmPassword: string,
        timezone: string,
        toc: boolean,
        invite: string,
    },
}

class RegisterPage extends React.Component<Props> {
    constructor(props: Props) {
        super(props)

        const { setFormField, location: { search }, setFieldError } = props
        setFormField('invite', qs.parse(search, {
            ignoreQueryPrefix: true,
        }).invite || '', () => {
            yup
                .object()
                .shape({
                    invite: yup.reach(schemas[0], 'invite'),
                })
                .validate(this.props.form)
                .then(
                    () => {
                        // To make sure that the registerPage invite doesn't stick in the browser history
                        props.history.replace(props.location.pathname)
                    },
                    (error: yup.ValidationError) => {
                        setFieldError('name', error.message)
                    }
                )
        })
    }

    submit = () => {
        const url = createLink('auth/register')
        const { name, password, confirmPassword: password2, timezone, toc: tosConfirmed, invite } = this.props.form

        return post(url, {
            name,
            password,
            password2,
            timezone,
            tosConfirmed,
            invite,
        }, false, false)
    }

    onFailure = (error: Error) => {
        const { setFieldError } = this.props
        setFieldError('toc', error.message)
    }

    render() {
        const { setIsProcessing, isProcessing, step, form, errors, setFieldError, next, prev, setFormField, redirect } = this.props
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
                <AuthStep title="Sign up" showEth={false} showSignin>
                    <TextInput
                        name="name"
                        label="Your Name"
                        type="text"
                        value={form.name}
                        onChange={setFormField}
                        error={errors.name}
                        processing={step === 0 && isProcessing}
                        autoComplete="name"
                        disabled={!form.invite}
                        autoFocus
                    />
                    <Actions>
                        <Button disabled={isProcessing}>Next</Button>
                    </Actions>
                </AuthStep>
                <AuthStep title="Sign up" showBack>
                    <TextInput
                        name="password"
                        type="password"
                        label="Create a Password"
                        value={form.password}
                        onChange={setFormField}
                        error={errors.password}
                        processing={step === 1 && isProcessing}
                        autoComplete="new-password"
                        measureStrength
                        autoFocus
                    />
                    <Actions>
                        <Button disabled={isProcessing}>Next</Button>
                    </Actions>
                </AuthStep>
                <AuthStep title="Sign up" showBack>
                    <TextInput
                        name="confirmPassword"
                        type="password"
                        label="Confirm your password"
                        value={form.confirmPassword}
                        onChange={setFormField}
                        error={errors.confirmPassword}
                        processing={step === 2 && isProcessing}
                        autoComplete="new-password"
                        autoFocus
                    />
                    <Actions>
                        <Button disabled={isProcessing}>Next</Button>
                    </Actions>
                </AuthStep>
                <AuthStep title="Timezone" showBack>
                    <Select
                        name="timezone"
                        label="Your timezone"
                        value={form.timezone}
                        options={moment.tz.names().map(tz => ({
                            value: tz,
                            label: tz,
                        }))}
                        onChange={setFormField}
                        error={errors.timezone}
                        processing={step === 3 && isProcessing}
                        autoFocus
                    />
                    <Actions>
                        <Button disabled={isProcessing}>Next</Button>
                    </Actions>
                </AuthStep>
                <AuthStep
                    title="Terms"
                    onSubmit={this.submit}
                    onSuccess={redirect}
                    onFailure={this.onFailure}
                    showBack
                >
                    <div className={styles.termsWrapper}>
                        <Checkbox
                            name="toc"
                            checked={form.toc}
                            onChange={onInputChange(setFormField)}
                            error={errors.toc}
                            autoFocus
                            keepError
                        >
                            I agree with the <a href={links.termsOfUse}>terms and conditions</a>, and <a href={links.privacyPolicy}>privacy policy</a>.
                        </Checkbox>
                    </div>
                    <Actions>
                        <Button disabled={isProcessing}>Finish</Button>
                    </Actions>
                </AuthStep>
            </AuthPanel>
        )
    }
}

export default withAuthFlow(RegisterPage, 0, {
    name: '',
    password: '',
    confirmPassword: '',
    timezone: '',
    toc: false,
    invite: '',
})
