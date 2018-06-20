// @flow

import * as React from 'react'

import styles from './authPanel.pcss'

type Props = {
    title: string,
    children: React.Node,
}

type State = {
    step: number,
    height: string | number,
}

class AuthPanel extends React.Component<Props, State> {
    state = {
        step: 0,
        height: 'auto',
    }

    onProceed = () => {
        const { children } = this.props
        const step = Math.min(React.Children.count(children) - 1, this.state.step + 1)

        this.setState({
            step,
        })
    }

    setHeight = (height: number) => {
        this.setState({
            height,
        })
    }

    render = () => {
        const { children, title } = this.props
        const { step, height } = this.state

        return (
            <div className={styles.authPanel}>
                <div className={styles.panel}>
                    <div className={styles.header}>
                        {title}
                    </div>
                    <div className={styles.body}>
                        <div
                            className={styles.inner}
                            style={{
                                height,
                            }}
                        >
                            {React.Children.map(children, (child, index) => React.cloneElement(child, {
                                active: index === step,
                                onProceed: this.onProceed,
                                onHeightChange: this.setHeight,
                            }))}
                        </div>
                    </div>
                </div>
            </div>
        )
    }
}

export default AuthPanel
