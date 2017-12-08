// @flow

import React, {Component} from 'react'
import {Button, FormControl} from 'react-bootstrap'
import StreamrInput from '../StreamrInput'

import styles from './streamrTextField.pcss'

export default class StreamrTextField extends Component {
    widget: any
    onModuleJson: Function
    onClick: Function
    onChange: Function
    state: {
        value: string
    }
    constructor() {
        super()
        this.state = {
            value: ''
        }
        this.onModuleJson = this.onModuleJson.bind(this)
        this.onClick = this.onClick.bind(this)
        this.onChange = this.onChange.bind(this)
    }
    onModuleJson({state}: {state: string}) {
        if (this.widget) {
            this.setState({
                value: state || ''
            })
        }
    }
    onClick() {
        if (this.widget) {
            this.widget.sendRequest({
                type: 'uiEvent',
                value: this.state.value
            })
        }
    }
    onChange(e: {
        preventDefault: () => void,
        target: {
            value: string
        }
    }) {
        e.preventDefault()
        this.setState({
            value: e.target.value
        })
    }
    render() {
        return (
            <StreamrInput
                {...this.props}
                onModuleJson={this.onModuleJson}
                widgetRef={(widget) => this.widget = widget}
            >
                <div className={styles.streamrTextField}>
                    <div className={styles.textareaContainer}>
                        <FormControl
                            componentClass="textarea"
                            placeholder="Message"
                            value={this.state.value}
                            onChange={this.onChange}
                            className={styles.textarea}
                        />
                    </div>
                    <div className={styles.buttonContainer}>
                        <Button
                            onClick={this.onClick}
                            bsStyle="primary"
                            bsSize="lg"
                            className={styles.button}
                        >
                            Send
                        </Button>
                    </div>
                </div>
            </StreamrInput>
        )
    }
}