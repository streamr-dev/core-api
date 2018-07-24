// @flow

/* eslint no-unused-vars: ["error", { "ignoreRestSiblings": true }] */

import React from 'react'
import ReactSelect from 'react-select'

import styles from './select.pcss'
import FormControl from '../FormControl'

type Props = {
    name: string,
    label: string,
    value: string,
    onFocusChange?: any,
    onAutoComplete?: any,
}

const Select = ({ name, onAutoComplete, ...props }: Props) => (
    <ReactSelect
        {...props}
        className={styles.root}
        name={name}
    />
)

export default FormControl(Select, (option: ?{
    value: string,
}) => (option || {}).value || '')
