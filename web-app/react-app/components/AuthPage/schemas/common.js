// @flow

import * as yup from 'yup'
import zxcvbn from 'zxcvbn'

export const email = yup.string()
    .trim()
    .required('Email is required')
    .email('Please enter a valid email address')

export const password = yup.string()
    .required('Password is required')

export const passwordWithStrength = yup.string()
    .required('Password is required')
    .min(8, 'Password must be at least 8 characters long')
    .test({
        path: 'is-strong',
        message: 'Please use a stronger password',
        test: function(value) {
            if (zxcvbn(value).score > 1) {
                return true
            } else {
                return this.createError({
                    message: zxcvbn(value).feedback.warning,
                    path: this.path,
                })
            }
        }
    })

export const confirmPassword = yup.string()
    .oneOf([yup.ref('password')], 'Passwords do not match')
