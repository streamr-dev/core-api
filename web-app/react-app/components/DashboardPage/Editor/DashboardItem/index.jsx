// @flux

import React, {Component} from 'react'
import {connect} from 'react-redux'
import path from 'path'

import TitleRow from './DashboardItemTitleRow'

import styles from './dashboardItem.pcss'
import './webcomponentStyles.css'

import type {Dashboard, DashboardItem as DBItem} from '../../../../flowtype/dashboard-types'

declare var Streamr: {}

class DashboardItem extends Component {
    
    props: {
        item: DBItem,
        dashboard: Dashboard,
        layout?: DBItem.layout,
        dragCancelClassName?: string,
        currentLayout: ?{}
    }
    
    constructor() {
        super()
        this.onResize = this.onResize.bind(this)
    }
    
    componentDidMount() {
        // TODO: why not work?
        setTimeout(() => this.onResize(), 500)
    }

    componentWillReceiveProps(props) {
        if (props.currentLayout) {
            this.onResize()
        }
    }
    
    onResize() {
        const event = new Event('Event')
        event.initEvent('resize', false, true)
        this.webcomponent.dispatchEvent(event)
    }

    render() {
        const item = this.props.item || {}
        const WebComponent = item.webcomponent || 'div'
        return (
            <div className={styles.dashboardItem}>
                <div className={styles.header}>
                    <TitleRow dashboard={this.props.dashboard} item={item} dragCancelClassName={this.props.dragCancelClassName}/>
                </div>
                <div className={`${styles.body} ${this.props.dragCancelClassName || ''}`}>
                    <div className={styles.wrapper}>
                        <WebComponent
                            ref={item => this.webcomponent = item}
                            className="streamr-widget non-draggable"
                            url={Streamr.createLink({
                                uri: path.resolve('/api/v1/dashboards', item.dashboard.toString(), 'canvases', item.canvas.toString(), 'modules', item.module.toString())
                            })}
                        />
                    </div>
                </div>
            </div>
        )
    }
}

export default connect()(DashboardItem)