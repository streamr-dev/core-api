// @flow

import * as React from 'react'
import { CSSTransitionGroup } from 'react-transition-group'

import styles from './switch.pcss'

type Props = {
    children: React.Node,
    current: number,
}

type State = {
    height: number | string,
}

class Switch extends React.Component<Props, State> {
    static defaultProps = {
        current: 0,
    }

    render = () => {
        const { current, children } = this.props

        return (
            <div className={styles.switch}>
                <CSSTransitionGroup
                    transitionName="switchTransition"
                    transitionAppear={false}
                    transitionEnterTimeout={200}
                    transitionLeaveTimeout={200}
                >
                    {React.Children.toArray(children)[current]}
                </CSSTransitionGroup>
            </div>
        )
    }
}

export default Switch
