// @flow

import React from 'react'
import styles from './inputError.pcss'

type Props = {
    eligible?: boolean,
    message: ?string,
}

const InputError = ({ message, eligible }: Props) => (
    <div className={styles.root}>
        {eligible && message ? message : null}
    </div>
)

export default InputError
