// @flow

import * as React from 'react'
import styles from './actions.pcss'

type Props = {
    children: React.Node,
}

export {
    styles,
}

const Actions = ({ children }: Props) => (
    <div className={styles.root}>
        {React.Children.count(children) === 1 && <span />}
        {children}
    </div>
)

export default Actions
