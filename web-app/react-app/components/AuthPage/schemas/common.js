// @flow

import * as yup from 'yup'

export const email = yup.string()
    .trim()
    .required('Email is required')
    .email('Email must be a valid email address')

export const password = yup.string()
    .required('Password is required')

export const confirmPassword = yup.string()
    .oneOf([yup.ref('password')], 'Passwords do not match')
