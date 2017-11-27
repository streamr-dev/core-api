// @flow

import React, {Component} from 'react'

declare var StreamrMap: Function

type Props = {
    width: number,
    height: number,
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

export default class MapWrapper extends Component {
    props: Props
    componentDidMount() {
        if (this.root && !this.map) {
            this.map = new StreamrMap(this.root, {
                ...this.props
            })
        }
    }
    componentWillreceiveProps(newProps: Props) {
        const changed = (key) => newProps[key] !== undefined && newProps[key] !== this.props[key]
        
        if (changed('width') || changed('height')) {
            this.map.redraw()
        }
        if (changed('centerLat') || changed('centerLng')) {
            this.map.setCenter(newProps['centerLat'], newProps['centerLng'])
        }
        
    }
    shouldComponentUpdate() {
        return false
    }
    render() {
        return (
            <div ref={root => this.root = root}/>
        )
    }
}