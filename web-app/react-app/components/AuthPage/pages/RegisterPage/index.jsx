// @flow

import * as React from 'react'
import qs from 'qs'
import cx from 'classnames'

import AuthPanel, {styles as authPanelStyles} from '../../shared/AuthPanel'
import Input from '../../shared/Input'
import Actions from '../../shared/Actions'
import Button from '../../shared/Button'
import Checkbox from '../../shared/Checkbox'
import AuthStep from '../../shared/AuthStep'

import withAuthFlow from '../../shared/withAuthFlow'
import {onInputChange} from '../../shared/utils'
import schemas from '../../schemas/register'
import type {AuthFlowProps} from '../../shared/types'
import axios from 'axios/index'
import createLink from '../../../../utils/createLink'

type Props = AuthFlowProps & {
    match: {
        params: {
            redirect?: ?string,
        },
    },
    history: {
        replace: (string) => void,
    },
    location: {
        pathname: string,
        search: string,
    },
    form: {
        email: string,
        password: string,
        confirmPassword: string,
        timezone: string,
        toc: boolean,
    },
}

type State = {
    invite: ?string,
}

const registerUrl = createLink('auth/register')
const defaultRedirectUrl = createLink('canvas/editor')
const inputNames = {
    name: 'name',
    password: 'password',
    confirmPassword: 'password2',
    timezone: 'timezone',
    toc: 'tosConfirmed',
    invite: 'invite',
}

class RegisterPage extends React.Component<Props, State> {
    constructor(props) {
        super(props)
        const {invite} = qs.parse(this.props.location.search.slice(1))
        this.state = {
            invite: invite || null,
        }
        if (invite) {
            // TODO: uncomment
            // props.history.replace(props.location.pathname)
        } else {
            props.setFieldError('name', 'An invite is needed. Please go back to the email you received, and click the click again.')
        }
    }

    submit = () => new Promise((resolve, reject) => {
        const {name, password, confirmPassword, timezone, toc} = this.props.form
        const {invite} = this.state
        const data = {
            [inputNames.name]: name,
            [inputNames.password]: password,
            [inputNames.confirmPassword]: confirmPassword,
            [inputNames.timezone]: timezone,
            [inputNames.toc]: toc,
            [inputNames.invite]: invite,
        }
        axios({
            method: 'post',
            url: registerUrl,
            data: qs.stringify(data),
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Requested-With': 'XMLHttpRequest', // Required
            },
        })
            .then(() => {
                this.onSuccess()
                resolve()
            })
            .catch(({response}) => {
                const {data} = response
                this.onFailure(new Error(data.error || 'Something went wrong'))
                reject()
            })
    })

    onSuccess = () => {
        const redirectUrl = this.props.match.params.redirect || defaultRedirectUrl
        window.location.assign(redirectUrl)
    }

    onFailure = (error: Error) => {
        const {setFieldError} = this.props
        setFieldError('toc', error.message)
    }

    render() {
        const {setIsProcessing, isProcessing, step, form, errors, setFieldError, next, prev, setFormField} = this.props
        return (
            <AuthPanel
                currentStep={step}
                form={form}
                onPrev={prev}
                onNext={next}
                setIsProcessing={setIsProcessing}
                validationSchemas={schemas}
                onValidationError={setFieldError}
            >
                <AuthStep title="Sign up" showEth={false} showSignin>
                    <Input
                        name="name"
                        label="Your Name"
                        type="text"
                        value={form.name}
                        onChange={onInputChange(setFormField)}
                        error={errors.name}
                        processing={step === 0 && isProcessing}
                        autoComplete="name"
                        disabled={!this.state.invite}
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
                        measureStrength
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
                <AuthStep
                    title="Terms"
                    onSubmit={this.submit}
                    onSuccess={this.onSuccess}
                    onFailure={this.onFailure}
                    showBack
                >
                    <div className={cx(authPanelStyles.spaceMedium, authPanelStyles.centered)}>
                        <Checkbox
                            name="toc"
                            checked={form.toc}
                            onChange={onInputChange(setFormField)}
                            error={errors.toc}
                        >
                            I agree with the <a href="#">terms and conditions</a>, and <a href="#">privacy policy</a>.
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
})
