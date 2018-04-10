// @flow

import React, { type Node } from 'react'

import styles from './authPanel.pcss'

type Props = {
    leftUtil?: Node,
    rightUtil?: Node,
    title: Node,
    children: Node
}

const AuthPanel = ({ leftUtil, rightUtil, title, children }: Props) => (
    <div className={styles.authPanel}>
        <div className={styles.utilRow}>
            <div className={styles.util}>
                {leftUtil}
            </div>
            <div className={styles.util}>
                {rightUtil}
            </div>
        </div>
        <div className={styles.panel}>
            <div className={styles.panelHeading}>
                {title}
            </div>
            <div className={styles.panelBody}>
                {children}
            </div>
        </div>
    </div>
)

export default AuthPanel
