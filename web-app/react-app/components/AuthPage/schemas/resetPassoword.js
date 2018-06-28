// @flow

import * as yup from 'yup'

import { password, confirmPassword } from './common'

export default [
    // Step 0: New password
    yup.object().shape({
        password,
    }),
    // Step 1: Confirm new password
    yup.object().shape({
        confirmPassword,
    }),
]
