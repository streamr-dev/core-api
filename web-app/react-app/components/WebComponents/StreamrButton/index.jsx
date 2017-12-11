// @flow

import React, {Component} from 'react'
import {Button} from 'react-bootstrap'
import StreamrInput from '../StreamrInput'
import StreamrWidget from '../StreamrWidget'

import type {WebcomponentProps as Props} from '../../../flowtype/webcomponent-types'

type State = {
    name: string
}

export default class StreamrButton extends Component<Props, State> {
    widget: ?StreamrWidget
    state = {
        name: 'Button'
    }
    
    onModuleJson = ({state}: { state: string }) => {
        if (this.widget) {
            this.setState({
                name: state
            })
        }
    }
    
    onMessage = ({buttonName}: { buttonName?: string }) => {
        if (this.widget) {
            this.setState({
                name: buttonName != undefined ? buttonName : this.state.name
            })
        }
    }
    
    onClick = () => {
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