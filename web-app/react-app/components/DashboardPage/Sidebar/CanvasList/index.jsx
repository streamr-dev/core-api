// @flow

import React, {Component} from 'react'

import CanvasInList from './CanvasInList'

import styles from './canvasList.pcss'

import type {Canvas} from '../../../../flowtype/canvas-types'

export default class CanvasList extends Component {
    
    props: {
        canvases: Array<Canvas>
    }
    
    render() {
        return (
            <ul className="navigation">
                <li className={styles.canvasListTitle}>
                    <label>Running Canvases</label>
                </li>
                {this.props.canvases.map(canvas => (
                    <CanvasInList key={canvas.id} canvas={canvas}/>
                ))}
            </ul>
        )
    }
}