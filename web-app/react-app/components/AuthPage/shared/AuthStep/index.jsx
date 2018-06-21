// @flow

import * as React from 'react'
import cx from 'classnames'
import elementSize from 'element-size'

import styles from './authStep.pcss'

type Props = {
    children: React.Node,
    active?: boolean,
    onHeightChange?: (number) => void,
}

class AuthStep extends React.Component<Props> {
    root: ?HTMLDivElement = null

    setRoot = (root: ?HTMLDivElement) => {
        this.root = root
    }

    componentDidMount = () => {
        if (this.props.active) {
            this.bumpHeight()
        }
    }

    componentDidUpdate = (prevProps: Props) => {
        const { active } = this.props

        if (active !== prevProps.active && active) {
            this.bumpHeight()
        }
    }

    bumpHeight = () => {
        const { onHeightChange } = this.props

        if (this.root && onHeightChange) {
            onHeightChange(elementSize(this.root)[1])
        }
    }

    render = () => {
        const { children, active } = this.props

        return (
            <div
                ref={this.setRoot}
                className={cx(styles.root, {
                    [styles.active]: !!active,
                })}
            >
                {children}
            </div>
        )
    }
}

export default AuthStep
