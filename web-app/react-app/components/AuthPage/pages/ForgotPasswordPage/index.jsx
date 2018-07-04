// @flow

import * as React from 'react'
import { Link } from 'react-router-dom'
import cx from 'classnames'

import AuthPanel, { styles as authPanelStyles } from '../../shared/AuthPanel'
import Input from '../../shared/Input'
import Actions from '../../shared/Actions'
import Button from '../../shared/Button'
import AuthStep from '../../shared/AuthStep'

import withAuthFlow, { type AuthFlowProps } from '../../shared/withAuthFlow'
import { preventDefault, onInputChange } from '../../shared/utils'
import schemas from '../../schemas/forgotPassword'

type Props = AuthFlowProps & {
    form: {
        email: string,
    },
}

// const ForgotPasswordPage = ({ processing, step, form: { email }, errors, next, prev, attach, setFormField }: Props) => (
//     <AuthPanel currentStep={step} onBack={prev} ref={attach} onProceed={preventDefault(next, schemas)}>
//         <AuthStep title="Get a link to reset your password">
//             <Input
//                 name="email"
//                 label="Email"
//                 value={email}
//                 onChange={onInputChange(setFormField)}
//                 error={errors.email}
//                 processing={step === 0 && processing}
//                 autocomplete="email"
//             />
//             <input type="password" name="password" style={{
//                 display: 'none',
//             }} />
//             <Actions>
//                 <Button disabled={processing}>Send</Button>
//             </Actions>
//         </AuthStep>
//         <AuthStep title="Link sent">
//             <p className={cx(authPanelStyles.spaceLarge, 'text-center')}>
//                 If a user with that email exists, we have sent a link to reset the password.
//                 Please check your email and click the link — it may be in your spam folder!
//             </p>
//             <p>
//                 <Link to="/register/resetPassword">Reset</Link>
//             </p>
//         </AuthStep>
//     </AuthPanel>
// )

const ForgotPasswordPage = () => <div />

export default withAuthFlow(ForgotPasswordPage, 0, {
    email: '',
})
