// @flow

import React from 'react'
import classnames from 'classnames'

import styles from './input.pcss'

const Input = (props: {
    className: string,
    placeholder: ?string,
    block?: boolean,
}) => (
    <input
        {...props}
        block={undefined}
        data-placeholder={props.placeholder}
        className={classnames(props.className, styles.input, {
            [styles.block]: props.block,
        })}
    />
)

export default Input
