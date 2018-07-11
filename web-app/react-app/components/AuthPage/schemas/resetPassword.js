// @flow

import * as yup from 'yup'

import { passwordWithStrength, confirmPassword } from './common'

export default [
    // Step 0: New password
    yup.object().shape({
        password: passwordWithStrength,
        token: yup
            .string()
            .required('A token is needed. Please go back to the email you received, and click the link again.'),
    }),
    // Step 1: Confirm new password
    yup.object().shape({
        confirmPassword,
    }),
]
