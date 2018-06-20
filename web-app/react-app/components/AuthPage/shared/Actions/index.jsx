// @flow

import * as React from 'react'

import Button from '../Button'
import styles from './actions.pcss'

type Props = {
    children: React.Node,
    onProceed?: () => void,
}

class Actions extends React.Component<Props> {
    onProceedClick = (e: SyntheticInputEvent<EventTarget>) => {
        const onProceed = this.props.onProceed || (() => {})
        e.preventDefault()
        onProceed()
    }

    render = () => {
        const { children } = this.props

        return (
            <div className={styles.actions}>
                {React.Children.count(children) === 1 && <span />}
                {React.Children.map(children, (child) => {
                    if (child.type === Button && child.props.proceed) {
                        return React.cloneElement(child, {
                            onClick: this.onProceedClick,
                        })
                    }
                    return child
                })}
            </div>
        )
    }
}

export default Actions
