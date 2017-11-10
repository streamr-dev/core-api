// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import {showError} from '../../../../actions/notification'
import path from 'path'
import createLink from '../../../../helpers/createLink'

import TitleRow from './DashboardItemTitleRow'
import WebComponent from '../../../WebComponent'

import styles from './dashboardItem.pcss'

import type {Dashboard, DashboardItem as DBItem} from '../../../../flowtype/dashboard-types'

class DashboardItem extends Component {
    webcomponent: HTMLElement
    onResize: Function
    createWebcomponentUrl: Function
    props: {
        item: DBItem,
        dashboard: Dashboard,
        layout?: DBItem.layout,
        dragCancelClassName?: string,
        currentLayout: ?{},
        showError: Function
    }
    static defaultProps = {
        item: {},
        dashboard: {}
    }
    
    constructor() {
        super()
        this.onResize = this.onResize.bind(this)
        this.createWebcomponentUrl = this.createWebcomponentUrl.bind(this)
    }
    
    componentDidMount() {
        // TODO: why it does not work without this?
        setTimeout(() => this.onResize(), 500)
    }

    componentWillReceiveProps(props) {
        if (props.currentLayout) {
            this.onResize()
        }
    }
    
    onResize() {
        const event = new Event('resize', {
            bubbles: false,
            cancelable: true
        })
        if (this.webcomponent) {
            this.webcomponent.dispatchEvent(event)
        }
    }
    
    createWebcomponentUrl() {
        const {dashboard, item: {canvas, module: itemModule}} = this.props
        // If the db is new the user must have the ownership of the canvas so use url /api/v1/canvases/<canvasId>/modules/<module>
        // Else use the url /api/v1/dashboards/<dashboardId>/canvases/<canvasId>/modules/<module>
        return createLink(path.resolve(
            '/api/v1',
            !dashboard.new ? 'dashboards' : '',
            !dashboard.new ? dashboard.id.toString() : '',
            'canvases',
            canvas.toString(),
            'modules',
            itemModule.toString()
        ))
    }

    render() {
        const {item} = this.props
        return (
            <div className={styles.dashboardItem}>
                <div className={styles.header}>
                    <TitleRow item={item} dragCancelClassName={this.props.dragCancelClassName}/>
                </div>
                <div className={`${styles.body} ${this.props.dragCancelClassName || ''}`}>
                    <div className={`${styles.wrapper} ${styles[item.webcomponent] || item.webcomponent}`}>
                        {item.webcomponent && (
                            <WebComponent
                                type={item.webcomponent}
                                onError={this.props.showError}
                                webComponentRef={(item: HTMLElement) => this.webcomponent = item}
                                url={this.createWebcomponentUrl()}
                            />
                        )}
                    </div>
                </div>
            </div>
        )
    }
}

const mapStateToProps = ({dashboard: {dashboardsById, openDashboard}}) => {
    const dashboard = dashboardsById[openDashboard.id]
    return {
        dashboard
    }
}

const mapDispatchToProps = (dispatch) => ({
    showError({detail}) {
        dispatch(showError({
            title: 'Error!',
            message: detail.message
        }))
    }
})

export default connect(mapStateToProps, mapDispatchToProps)(DashboardItem)