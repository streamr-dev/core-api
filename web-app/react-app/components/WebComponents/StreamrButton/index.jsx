// @flow

import React, {Component} from 'react'
import {Button} from 'react-bootstrap'
import StreamrInput from '../StreamrInput'

export default class StreamrButton extends Component {
    widget: any
    onModuleJson: Function
    onMessage: Function
    onClick: Function
    state: {
        name: string
    }
    constructor() {
        super()
        this.state = {
            name: 'Button'
        }
        this.onModuleJson = this.onModuleJson.bind(this)
        this.onMessage = this.onMessage.bind(this)
        this.onClick = this.onClick.bind(this)
    }
    onModuleJson({state}: {state: string}) {
        if (this.widget) {
            this.setState({
                name: state
            })
        }
    }
    onMessage({buttonName}: { buttonName?: string }) {
        if (this.widget) {
            this.setState({
                name: buttonName != undefined ? buttonName : this.state.name
            })
        }
    }
    onClick() {
        if (this.widget) {
            this.widget.sendRequest({
                type: 'uiEvent'
            })
        }
    }
    render() {
        return (
            <StreamrInput
                {...this.props}
                onModuleJson={this.onModuleJson}
                onMessage={this.onMessage}
                widgetRef={(widget) => this.widget = widget}
            >
                <Button
                    onClick={this.onClick}
                    bsStyle="primary"
                    bsSize="lg"
                >
                    {this.state.name}
                </Button>
            </StreamrInput>
        )
    }
}