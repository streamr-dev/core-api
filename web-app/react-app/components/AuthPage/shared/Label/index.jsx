// @flow

import React from 'react'
import cx from 'classnames'

import styles from './label.pcss'

type Props = {
    value: string,
    strengthLevel: number,
}

const Label = ({ value, strengthLevel }: Props) => (
    <label className={styles.root}>
        <div className={cx(styles.level, {
            [styles.active]: strengthLevel === -1,
        })}>
            {value}
        </div>
        <div className={cx(styles.level, styles.weak, {
            [styles.active]: strengthLevel === 0,
        })}>
            Password is weak
        </div>
        <div className={cx(styles.level, styles.moderate, {
            [styles.active]: [1, 2].indexOf(strengthLevel) !== -1,
        })}>
            Password is not strong
        </div>
        <div className={cx(styles.level, styles.strong, {
            [styles.active]: [3, 4].indexOf(strengthLevel) !== -1,
        })}>
            Password is quite strong
        </div>
    </label>
)

export default Label
