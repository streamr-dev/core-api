// @flow

import * as yup from 'yup'

export default [
    // Step 0: Email
    null,
    // Step 1: New password
    yup.object()
        .shape({
            password: yup.string()
                .min(3)
                .required('Password is required'),
        }),
    // Step 2: Confirm password
    null,
    // Step 3: Timezone
    null,
    // Step 4: Terms
    null,
]
