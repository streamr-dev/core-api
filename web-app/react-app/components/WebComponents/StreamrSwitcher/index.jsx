// @flow

import React, {Component} from 'react'
import StreamrInput from '../StreamrInput'
import StreamrWidget from '../StreamrWidget'

import styles from './streamrSwitcher.pcss'

import type {WebcomponentProps} from '../../../flowtype/webcomponent-types'

type State = {
    value: boolean
}

export default class StreamrSwitcher extends Component<WebcomponentProps, State> {
    widget: StreamrWidget
    //onModuleJson: Function
    //onClick: Function
    state = {
        value: false
    }
    //constructor() {
    //    super()
    //    this.onModuleJson = this.onModuleJson.bind(this)
    //    this.onClick = this.onClick.bind(this)
    //}
    onModuleJson = ({state}: {state: boolean}) => {
        if (this.widget) {
            this.setState({
                value: state
            })
        }
    }
    onClick = () => {
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