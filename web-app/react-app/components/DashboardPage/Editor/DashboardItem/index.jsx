// @flux

import React, {Component} from 'react'
import {connect} from 'react-redux'
import path from 'path'

import TitleRow from './DashboardItemTitleRow'

import type {Dashboard, DashboardItem as DBItem} from '../../../../types/dashboard-types'

declare var Streamr: {}

class DashboardItem extends Component {
    
    props: {
        item: DBItem,
        dashboard: Dashboard
    }
    
    render() {
        const item = this.props.item || {}
        const WebComponent = item.webcomponent
        return (
            <div className="dashboarditem small-size col-xs-12 col-sm-6 col-md-4 col-lg-3 col-centered">
                <div className="contains">
                    <div className="stat-panel">
                        <div className="stat-row">
                            <TitleRow dashboard={this.props.dashboard} item={item} />
                        </div>
                        <div className="stat-row">
                            <div className="widget-content stat-cell bordered no-border-t text-center">
                                <WebComponent
                                    className="streamr-widget non-draggable"
                                    url={Streamr.createLink({
                                        uri: path.resolve('api/v1/dashboards', item.dashboard.toString(), 'canvases', item.canvas.toString(), 'modules', item.module.toString())
                                    })}
                                />
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        )
    }
}

export default connect()(DashboardItem)