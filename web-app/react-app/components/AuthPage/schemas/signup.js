// @flow

import * as yup from 'yup'

import { email, password, confirmPassword } from './common'

export default [
    // Email
    yup.object()
        .shape({
            email,
        }),
]
