// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import CanvasInList from './CanvasInList'

import styles from './canvasList.pcss'

import type {Canvas} from '../../../../flowtype/canvas-types'

class CanvasList extends Component {
    
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

const mapStateToProps = ({canvas}) => ({
    canvases: canvas.list || []
})

export default connect(mapStateToProps)(CanvasList)