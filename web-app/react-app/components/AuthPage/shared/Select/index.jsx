// @flow

/* eslint no-unused-vars: ["error", { "ignoreRestSiblings": true }] */

import React from 'react'
import ReactSelect from 'react-select'
import MediaQuery from 'react-responsive'
import { breakpoints } from '@streamr/streamr-layout'

import styles from './select.pcss'
import FormControl from '../FormControl'

type Props = {
    name: string,
    label: string,
    value: string,
    onFocusChange?: any,
    onAutoComplete?: any,
}

type OptionProps = {
    label: string,
}

const OptionRenderer = ({ label }: OptionProps) => (
    <div className={styles.optionLabel}>{label}</div>
)

const Select = ({ name, onAutoComplete, ...props }: Props) => (
    <MediaQuery maxWidth={breakpoints.xs.max}>
        {(isMobile) => (
            <ReactSelect
                {...props}
                className={styles.root}
                searchable={!isMobile}
                name={name}
                optionRenderer={OptionRenderer}
            />
        )}
    </MediaQuery>
)

export default FormControl(Select, (option: ?{
    value: string,
}) => (option || {}).value || '')
