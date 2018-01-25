// @flow

import React, {Component} from 'react'
import ComplexStreamrWidget from '../ComplexStreamrWidget'

declare var StreamrMap: Function

import type {WebcomponentProps} from '../../../flowtype/webcomponent-types'
import type {ModuleOptions} from '../../../flowtype/streamr-client-types'

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

type Props = WebcomponentProps & {}

type State = {
    options: Options
}

export default class StreamrMapComponent extends Component<Props, State> {
    map: ?StreamrMap
    state = {
        options: {}
    }
    
    componentWillReceiveProps(newProps: Props) {
        const changed = (key) => newProps[key] != undefined && newProps[key] !== this.props[key]
        
        if (changed('width') || changed('height')) {
            this.map && this.map.redraw()
        }
    }
    
    renderWidget = (root: ?HTMLDivElement, options: Options) => {
        if (root) {
            this.map = new StreamrMap(root, options)
        }
    }
    
    onMessage = (msg: {}) => {
        this.map && this.map.handleMessage(msg)
    }
    
    onResize = () => {
        this.map && this.map.redraw()
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
                onResize={this.onResize}
            />
        )
    }
}