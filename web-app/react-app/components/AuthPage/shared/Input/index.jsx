// @flow

import React from 'react'
import cx from 'classnames'

import styles from './input.pcss'

type Props = {
    error?: string,
    className?: string,
}

const Input = ({ error, ...props }: Props) => (
    <label
        className={cx(styles.root, {
            [styles.withError]: !!error,
        })}
    >
        <input {...props} className={styles.input} />
        {!!error && (
            <div className={styles.error}>{error}</div>
        )}
    </label>
)

export default Input
