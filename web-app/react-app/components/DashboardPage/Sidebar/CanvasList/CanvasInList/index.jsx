// @flow

import React, {Component} from 'react'

import type { Canvas } from '../../../../../flowtype/canvas-types'
import ModuleList from './ModuleList/index'

import styles from './canvasInList.pcss'

export default class CanvasInList extends Component {
    
    props: {
        canvas: Canvas
    }
    
    state: {
        open: boolean
    }
    
    constructor() {
        super()
        this.state = {
            open: false
        }
    }
    
    render() {
        const {canvas} = this.props
        return (
            <li className={`canvas mm-dropdown mm-dropdown-root ${this.state.open ? 'open' : ''}`}>
                <a className="canvas-title" title={canvas.state} onClick={() => {
                    this.setState({
                        open: !this.state.open
                    })
                }}>
                    <span className={`mm-text mmc-dropdown-delay animated fadeIn ${styles.canvasTitle} ${canvas.state === 'STOPPED' ? styles.stopped : ''}`}>
                        {canvas.name}
                    </span>
                    <span className="howmanychecked badge badge-primary">
                        {(canvas.modules.filter(module => module.checked)).length || ''}
                    </span>
                </a>
                <ModuleList modules={canvas.modules} canvasId={canvas.id} />
            </li>
        )
    }
}