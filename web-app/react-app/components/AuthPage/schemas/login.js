// @flow

import * as yup from 'yup'

import { email, password } from './common'

export default [
    // Step 0: Email
    yup.object()
        .shape({
            email,
        }),
    // Step 1: Passowrd
    yup.object()
        .shape({
            password,
        }),
]
