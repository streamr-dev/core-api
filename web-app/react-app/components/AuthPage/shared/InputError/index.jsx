// @flow

import React from 'react'
import styles from './inputError.pcss'

type Props = {
    error: ?string,
}

const InputError = ({ error }: Props) => (error ? (
    <div className={styles.inputError}>
        {error}
    </div>
) : null)

export default InputError
