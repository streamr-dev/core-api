// @flow

import React, {Component} from 'react'
import path from 'path'
import createLink from '../../helpers/createLink'

type type = 'streamr-chart' | 'streamr-table' | 'streamr-label' | 'streamr-map'

import type {Dashboard} from '../../flowtype/dashboard-types.js'
import type {Canvas, CanvasModule} from '../../flowtype/canvas-types.js'

export default class WebComponent extends Component {
    webcomponent: HTMLElement
    bindError: Function
    unbindError: Function
    props: {
        type: type,
        webComponentRef: Function,
        onError: Function,
        dashboardId: Dashboard.id,
        canvasId: Canvas.id,
        moduleId: CanvasModule.id
    }
    static defaultProps = {
        onError: () => {}
    }
    constructor() {
        super()
        this.bindError = this.bindError.bind(this)
        this.unbindError = this.unbindError.bind(this)
    }
    
    bindError() {
        this.webcomponent.addEventListener('error', this.props.onError)
    }
    
    unbindError() {
        this.webcomponent.removeEventListener('error', this.props.onError)
    }
    
    componentDidUpdate() {
        this.bindError()
    }
    
    componentWillUpdate() {
        this.unbindError()
    }
    
    render() {
        const {type: WebcomponentType, dashboardId, canvasId, moduleId} = this.props
        return (
            <WebcomponentType
                ref={item => {
                    this.webcomponent = item
                    if (this.props.webComponentRef) {
                        this.props.webComponentRef(item)
                    }
                }}
                className="streamr-widget non-draggable"
                url={createLink(path.resolve('/api/v1/dashboards', dashboardId, 'canvases', canvasId, 'modules', moduleId))}
            />
        )
    }
}