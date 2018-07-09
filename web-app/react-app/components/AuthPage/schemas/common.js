// @flow

import * as yup from 'yup'
import zxcvbn from 'zxcvbn'

export const email = yup.string()
    .trim()
    .required('Email is required')
    .email('Email must be a valid email address')

export const password = yup.string()
    .required('Password is required')
    .min(8, 'Password must be at least 8 characters long')
    .test('is-strong', 'Please use a stronger password', (value) => zxcvbn(value).score > 1)

export const confirmPassword = yup.string()
    .oneOf([yup.ref('password')], 'Passwords do not match')
