// @flow

import React from 'react'
import cx from 'classnames'

import styles from './input.pcss'

type Props = {
    className?: string,
}

const Input = ({ className, ...props }: Props) => (
    <input
        {...props}
        className={cx(className, styles.input)}
    />
)

export default Input
