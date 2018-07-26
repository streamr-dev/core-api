// @flow

/* eslint no-unused-vars: ["error", { "ignoreRestSiblings": true }] */

import * as React from 'react'
import ReactSelect from 'react-select'
import MediaQuery from 'react-responsive'
import { breakpoints } from '@streamr/streamr-layout'

import SelectOption from './SelectOption'
import FormControl from '../FormControl'
import styles from './select.pcss'

type Props = {
    name: string,
    label: string,
    value: string,
    onFocusChange?: any,
    onAutoComplete?: any,
}

const Select = ({ name, onAutoComplete, ...props }: Props) => (
    <MediaQuery maxWidth={breakpoints.xs.max}>
        {(isMobile) => (
            <ReactSelect
                {...props}
                className={styles.root}
                searchable={!isMobile}
                name={name}
                optionComponent={SelectOption}
            />
        )}
    </MediaQuery>
)

export default FormControl(Select, (option: ?{
    value: string,
}) => (option || {}).value || '')
