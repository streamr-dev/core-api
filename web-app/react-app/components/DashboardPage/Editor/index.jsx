// @flux

import React, {Component} from 'react'
import {Breadcrumb, BreadcrumbItem} from '../../Breadcrumb'

declare var Streamr: {
    createLink: Function
}

type Dashboard = {
    id: number,
    name: string,
    items: Array<{}>
}

export default class Editor extends Component {
    
    props: {
        dashboard: Dashboard
    }
    
    render() {
        return (
            <div id="content-wrapper" className="scrollable">
                <Breadcrumb>
                    <BreadcrumbItem href={Streamr.createLink('dashboard', 'list')}>
                        Dashboards
                    </BreadcrumbItem>
                    <BreadcrumbItem href={Streamr.createLink('dashboard', 'show', 1)} active={true}>
                        moi
                    </BreadcrumbItem>
                </Breadcrumb>
            </div>
        )
    }
}