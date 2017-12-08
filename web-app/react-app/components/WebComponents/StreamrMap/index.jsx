// @flow

import React, {Component} from 'react'
import StreamrWidget from '../StreamrWidget'

declare var StreamrMap: Function

import type {StreamId, ModuleOptions} from '../../../flowtype/streamr-client-types'

type Options = ModuleOptions | {
    centerLat?: number,
    centerLng?: number,
    zoom?: number,
    minZoom?: number,
    maxZoom?: number,
    traceWidth?: number,
    drawTrace?: boolean,
    skin?: 'default' | 'cartoDark' | 'esriDark',
    directionalMarkers?: boolean,
    directionalMarkerIcon?: 'arrow' | 'arrowhead' | 'longArrow',
    markerIcon?: 'pin' | 'circle',
    customImageUrl?: string
}

type Props = {
    url: string,
    stream?: StreamId,
    onError?: Function,
    width: number,
    height: number
}

export default class StreamrMapComponent extends Component {
    props: Props
    root: HTMLDivElement
    onMessage: Function
    widget: StreamrWidget
    map: ?any
    
    state: {
        options: Options
    }
    
    constructor() {
        super()
        this.state = {
            options: {}
        }
        this.onMessage = this.onMessage.bind(this)
    }
    
    onModuleJson({options}: { options: Options }) {
        this.setState({
            options
        })
        this.map = new StreamrMap(this.root, options)
    }
    
    componentWillReceiveProps(newProps: Props) {
        const changed = (key) => newProps[key] !== undefined && newProps[key] !== this.props[key]
        
        if (changed('width') || changed('height')) {
            this.map && this.map.redraw()
        }
    }
    
    onMessage(msg: {}) {
        this.map && this.map.handleMessage(msg)
    }
    
    render() {
        return (
            <StreamrWidget
                subscriptionOptions={{
                    stream: this.props.stream
                }}
                onModuleJson={this.onModuleJson}
                url={this.props.url}
                onMessage={this.onMessage}
                onError={this.props.onError}
                ref={(w) => this.widget = w}
            >
                <div ref={root => this.root = root}/>
            </StreamrWidget>
        )
    }
}