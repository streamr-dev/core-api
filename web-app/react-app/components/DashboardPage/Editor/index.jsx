// @flux

import React, {Component} from 'react'
import {connect} from 'react-redux'
import {Breadcrumb, BreadcrumbItem} from '../../Breadcrumb'

import DashboardItem from './DashboardItem'

declare var Streamr: {
    createLink: Function
}

import type {Dashboard} from '../../../types/dashboard-types'

class Editor extends Component {
    
    props: {
        dashboard: Dashboard
    }
    
    render() {
        const dashboard = this.props.dashboard || {
            name: '',
            items: []
        }
        return (
            <div id="content-wrapper" className="scrollable">
                <Breadcrumb>
                    <BreadcrumbItem href={Streamr.createLink('dashboard', 'list')}>
                        Dashboards
                    </BreadcrumbItem>
                    <BreadcrumbItem href={Streamr.createLink('dashboard', 'show', 1)} active={true}>
                        {dashboard.name}
                    </BreadcrumbItem>
                </Breadcrumb>
                <streamr-client id="client" url="ws://dev.streamr/api/v1/ws" autoconnect="true" autodisconnect="false"/>
                <div>
                    {dashboard.items.map(dbItem => (
                        <DashboardItem key={dbItem.canvas + dbItem.module} item={dbItem} dashboard={dashboard} />
                    ))}
                </div>
            </div>
        )
    }
}

export default connect()(Editor)