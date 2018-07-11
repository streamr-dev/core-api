// @flow

import * as yup from 'yup'

import { email } from './common'

export default [
    // Email
    yup.object()
        .shape({
            email,
        }),
]
