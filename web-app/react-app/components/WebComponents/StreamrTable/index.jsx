// @flow

import React, {Component} from 'react'
import ComplexStreamrWidget from '../ComplexStreamrWidget'

declare var StreamrTable: Function

import type {WebcomponentProps} from '../../../flowtype/webcomponent-types'
import type {ModuleOptions} from '../../../flowtype/streamr-client-types'

type Options = ModuleOptions | {}

type Props = WebcomponentProps & {}

type State = {
    options: Options
}

export default class StreamrTableComponent extends Component<Props, State> {
    table: ?StreamrTable
    state = {
        options: {}
    }
    
    renderWidget = (root: ?HTMLDivElement, options: Options) => {
        if (root) {
            this.table = new StreamrTable(root, options)
            this.table.initTable()
        }
    }
    
    onMessage = (msg: {}) => {
        this.table && this.table.receiveResponse(msg)
    }
    
    render() {
        return (
            <ComplexStreamrWidget
                stream={this.props.stream}
                url={this.props.url}
                onError={this.props.onError}
                width={this.props.width}
                height={this.props.height}
                onMessage={this.onMessage}
                renderWidget={this.renderWidget}
            />
        )
    }
}