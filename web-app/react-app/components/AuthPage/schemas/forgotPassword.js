// @flow

import * as yup from 'yup'

import { email } from './common'

export default [
    // Step 0: Email
    yup.object()
        .shape({
            email,
        }),
]
