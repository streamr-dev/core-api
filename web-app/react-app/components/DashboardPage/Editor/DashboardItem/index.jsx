// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import {error} from 'react-notification-system-redux'
import path from 'path'
import createLink from '../../../../helpers/createLink'

import TitleRow from './DashboardItemTitleRow'

import styles from './dashboardItem.pcss'

import type {Dashboard, DashboardItem as DBItem, DashboardReducerState as DashboardState} from '../../../../flowtype/dashboard-types'

const config = require('../../dashboardConfig')

type Props = {
    item: DBItem,
    dashboard: Dashboard,
    layout?: DBItem.layout,
    dragCancelClassName?: string,
    currentLayout: ?{},
    error: Function,
    isLocked: boolean,
    config: {
        components: {}
    }
}

type State = {
    height: ?number,
    width: ?number
}

export class DashboardItem extends Component<Props, State> {
    wrapper: ?HTMLElement
    static defaultProps = {
        item: {},
        dashboard: {}
    }
    state = {
        height: null,
        width: null
    }
    
    onResize = () => {
        this.setState({
            height: this.wrapper && this.wrapper.offsetHeight,
            width: this.wrapper && this.wrapper.offsetWidth,
        })
    }
    
    componentWillReceiveProps = () => {
        this.onResize()
    }
    
    createWebcomponentUrl = () => {
        const {dashboard, item: {canvas, module: itemModule}} = this.props
        // If the db is new the user must have the ownership of the canvas so use url /api/v1/canvases/<canvasId>/modules/<module>
        // Else use the url /api/v1/dashboards/<dashboardId>/canvases/<canvasId>/modules/<module>
        return createLink(path.resolve(
            '/api/v1',
            !dashboard.new ? `dashboards/${dashboard.id}` : '',
            `canvases/${canvas}`,
            `modules/${itemModule}`
        ))
    }
    
    onError = (err: {
        message: string,
        stack: string
    }) => {
        const inProd = process.env.NODE_ENV === 'production'
        this.props.error(inProd ? 'Something went wrong!' : err)
        if (!inProd) {
            console.error(err.stack)
        }
    }
    
    createCustomComponent = () => {
        const {item, config: conf} = this.props
        
        const {component, props} = conf.components[item.webcomponent] || {}
        
        const CustomComponent = component || (() => (
            <div style={{
                color: 'red',
                textAlign: 'center'
            }}>
                Sorry, unknown component:(
            </div>
        ))
        
        const customProps = props || {}
        
        return CustomComponent ? (
            <CustomComponent
                url={this.createWebcomponentUrl()}
                height={this.state.height}
                width={this.state.width}
                onError={this.onError}
                {...customProps}
            />
        ) : null
    }
    
    render() {
        const {item} = this.props
        return (
            <div className={styles.dashboardItem}>
                <div className={styles.header}>
                    <TitleRow item={item} dragCancelClassName={this.props.dragCancelClassName} isLocked={this.props.isLocked}/>
                </div>
                <div className={`${styles.body} ${this.props.dragCancelClassName || ''}`}>
                    <div
                        className={`${styles.wrapper} ${styles[item.webcomponent] || item.webcomponent}`}
                        ref={wrapper => this.wrapper = wrapper}
                    >
                        {this.createCustomComponent()}
                    </div>
                </div>
            </div>
        )
    }
}

export const mapStateToProps = ({dashboard: {dashboardsById, openDashboard}}: {dashboard: DashboardState}) => ({
    dashboard: dashboardsById[openDashboard.id],
    config: config
})

export const mapDispatchToProps = (dispatch: Function) => ({
    error(message: string) {
        dispatch(error({
            title: 'Error!',
            message
        }))
    }
})

export default connect(mapStateToProps, mapDispatchToProps)(DashboardItem)