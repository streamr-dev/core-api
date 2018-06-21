import * as yup from 'yup'

export default [
    yup.object()
        .shape({
            email: yup.string()
                .trim()
                .required('Email is required'),
        }),
    yup.object()
        .shape({
            password: yup.string().required('Password is required'),
        }),
]
