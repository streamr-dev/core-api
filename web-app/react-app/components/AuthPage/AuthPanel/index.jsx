// @flow

import * as React from 'react'
import { Card, CardHeader, CardBody } from '@streamr/streamr-layout'
import classnames from 'classnames'

import styles from './authPanel.pcss'

type Props = {
    leftUtil?: React.Node,
    rightUtil?: React.Node,
    title: React.Node,
    children: React.Node
}

const AuthPanel = ({ leftUtil, rightUtil, title, children }: Props) => (
    <div className={styles.authPanel}>
        <div className={classnames(styles.utilRow, 'uppercase', 'ff-plex-mono')}>
            <div className={styles.util}>
                {leftUtil}
            </div>
            <div className={styles.util}>
                {rightUtil}
            </div>
        </div>
        <Card className={styles.panel}>
            <CardHeader className={styles.panelHeading}>
                {title}
            </CardHeader>
            <CardBody className={styles.panelBody}>
                <div>
                    {children}
                </div>
            </CardBody>
        </Card>
    </div>
)

export default AuthPanel
