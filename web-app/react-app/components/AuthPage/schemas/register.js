// @flow

import * as yup from 'yup'

import { passwordWithStrength, confirmPassword } from './common'

export default [
    // Step 0: Name
    yup.object()
        .shape({
            name: yup.string()
                .required('Name is required'),
        }),
    // Step 1: New password
    yup.object()
        .shape({
            password: passwordWithStrength,
        }),
    // Step 2: Confirm password
    yup.object()
        .shape({
            confirmPassword,
        }),
    // Step 3: Timezone
    yup.object()
        .shape({
            timezone: yup.string()
                .required('Timezone is required'),
        }),
    // Step 4: Terms
    yup.object()
        .shape({
            toc: yup.boolean()
                .oneOf([true], 'Must Accept Terms and Conditions'),
        }),
]
