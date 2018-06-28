// @flow

import * as React from 'react'
import cx from 'classnames'
import elementSize from 'element-size'

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

    state = {
        height: 'auto',
    }

    elements: Array<?HTMLDivElement> = []

    timeout: TimeoutID

    bumpHeight = () => {
        const { current } = this.props
        const root = this.elements[current]

        if (root) {
            this.setState({
                height: elementSize(root)[1],
            })
        }
    }

    focus = () => {
        clearTimeout(this.timeout)

        this.timeout = setTimeout(() => {
            const { current } = this.props
            const root = this.elements[current]

            if (root) {
                const input = root.querySelector('input')

                if (input) {
                    input.focus()
                }
            }
        }, 100)
    }

    setElementAt = (index: number) => (element: ?HTMLDivElement) => {
        this.elements[index] = element
    }

    componentDidMount = () => {
        this.bumpHeight()
        this.focus()
    }

    componentDidUpdate = (prevProps: Props) => {
        const { current } = this.props

        if (current !== prevProps.current) {
            this.bumpHeight()
            this.focus()
        }
    }

    render = () => {
        const { current, children } = this.props
        const { height } = this.state

        return (
            <div
                className={styles.root}
                style={{
                    height,
                }}
            >
                {React.Children.map(children, (child, index) => (
                    <div
                        className={cx(styles.switchable, {
                            [styles.active]: current === index,
                        })}
                        ref={this.setElementAt(index)}
                    >
                        {child}
                    </div>
                ))}
            </div>
        )
    }
}

export default Switch
