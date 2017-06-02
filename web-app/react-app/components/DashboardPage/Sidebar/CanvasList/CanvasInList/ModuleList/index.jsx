// @flow

import React, {Component} from 'react'

import ModuleInModuleList from './ModuleInModuleList'

import type { Canvas, CanvasModule } from '../../../../../../flowtype/canvas-types'

export default class ModuleList extends Component {
    
    props: {
        modules: Array<CanvasModule>,
        canvasId: Canvas.id
    }
    
    render() {
        const {modules, canvasId} = this.props
        return (
            <ul className="mmc-dropdown-delay animated fadeInLeft">
                {modules.sort((a, b) => a.name.localeCompare(b.name)).map(module => (
                    <ModuleInModuleList key={module.hash} module={module} canvasId={canvasId} />
                ))}
            </ul>
        )
    }
}