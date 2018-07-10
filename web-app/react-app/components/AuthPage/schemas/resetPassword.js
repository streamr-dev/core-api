// @flow

import * as yup from 'yup'

import { passwordWithStrength, confirmPassword } from './common'

export default [
    // Step 0: New password
    yup.object().shape({
        password: passwordWithStrength,
    }),
    // Step 1: Confirm new password
    yup.object().shape({
        confirmPassword,
    }),
]
