// @flow

import React from 'react'

import TextField from './TextField'
import FormControl from '../FormControl'

const WrappedTextField = FormControl(TextField)

const changeFormatter = ({ target: { name, value } }: SyntheticInputEvent<EventTarget>) => ({
    name,
    value,
})

type Props = {
    label: string,
    value: string,
}

const TextInput = ({ ...props }: Props) => (
    <WrappedTextField
        {...props}
        changeFormatter={changeFormatter}
    />
)

export default TextInput
