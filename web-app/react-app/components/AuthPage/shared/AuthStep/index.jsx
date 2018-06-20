// @flow

import * as React from 'react'
import cx from 'classnames'
import elementSize from 'element-size'
import { ValidationError } from 'yup'

import Actions from '../Actions'
import styles from './authStep.pcss'

type Props = {
    children: React.Node,
    active?: boolean,
    onProceed?: () => void,
    onHeightChange?: (number) => void,
    validate?: () => Promise<any>,
    onValidationError?: (ValidationError) => void,
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

    onProceed = () => {
        const { onProceed, validate, onValidationError } = this.props
        const proceed = onProceed || (() => {})

        if (validate) {
            validate().then(proceed, onValidationError)
        } else {
            proceed()
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
                {React.Children.map(children, (child) => {
                    if (child.type === Actions) {
                        return React.cloneElement(child, {
                            onProceed: this.onProceed,
                        })
                    }
                    return child
                })}
            </div>
        )
    }
}

export default AuthStep
