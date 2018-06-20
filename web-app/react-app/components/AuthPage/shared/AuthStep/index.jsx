// @flow

import * as React from 'react'
import cx from 'classnames'

import Actions from '../Actions'
import styles from './authStep.pcss'

type Props = {
    children: React.Node,
    active?: boolean,
    onProceed?: () => void,
}

class AuthStep extends React.Component<Props> {
    render = () => {
        const { children, active, onProceed } = this.props

        return (
            <div
                className={cx(styles.root, {
                    [styles.active]: !!active,
                })}
            >
                {React.Children.map(children, (child) => {
                    if (child.type === Actions) {
                        return React.cloneElement(child, {
                            onProceed: onProceed || (() => {}),
                        })
                    }
                    return child
                })}
            </div>
        )
    }
}

export default AuthStep
