import * as yup from 'yup'

export const email = yup.object()
    .shape({
        email: yup.string()
            .trim()
            .required('Email is required'),
    })

export const password = yup.object()
    .shape({
        password: yup.string().required('Password is required'),
    })
