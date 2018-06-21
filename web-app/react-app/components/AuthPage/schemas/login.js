import * as yup from 'yup'

export const email = yup.object()
    .shape({
        email: yup.string()
            .trim()
            .required('is required'),
    })

export const password = yup.object()
    .shape({
        password: yup.string().required('is required'),
    })
