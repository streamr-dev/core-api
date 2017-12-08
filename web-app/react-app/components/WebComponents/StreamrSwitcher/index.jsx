// @flow

import React, {Component} from 'react'
import StreamrInput from '../StreamrInput'

import styles from './streamrSwitcher.pcss'

export default class StreamrSwitcher extends Component {
    widget: any
    onModuleJson: Function
    onClick: Function
    state: {
        value: boolean
    }
    constructor() {
        super()
        this.state = {
            value: false
        }
        this.onModuleJson = this.onModuleJson.bind(this)
        this.onClick = this.onClick.bind(this)
    }
    onModuleJson({state}: {state: boolean}) {
        if (this.widget) {
            this.setState({
                value: state
            })
        }
    }
    onClick() {
        const newValue = !this.state.value
        this.setState({
            value: newValue
        })
        this.widget.sendRequest({
            type: 'uiEvent',
            value: newValue
        })
    }
    render() {
        return (
            <StreamrInput
                {...this.props}
                onModuleJson={this.onModuleJson}
                widgetRef={(widget) => this.widget = widget}
            >
                <div className={styles.streamrSwitcher}>
                    <div className={`${styles.switcher} ${this.state.value ? styles.on : styles.off}`} onClick={this.onClick}>
                        <div className={styles.switcherInner} />
                    </div>
                </div>
            </StreamrInput>
        )
    }
}