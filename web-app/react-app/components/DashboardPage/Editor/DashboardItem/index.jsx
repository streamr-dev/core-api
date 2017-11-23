// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import {showError} from '../../../../actions/notification'
import path from 'path'
import createLink from '../../../../helpers/createLink'

import TitleRow from './DashboardItemTitleRow'
import StreamrLabel from '../../../WebComponents/StreamrLabel'
import StreamrButton from '../../../WebComponents/StreamrButton'
import StreamrTextField from '../../../WebComponents/StreamrTextField'
import StreamrSwitcher from '../../../WebComponents/StreamrSwitcher'

import styles from './dashboardItem.pcss'
import './webcomponentStyles.css'

import type {Dashboard, DashboardItem as DBItem} from '../../../../flowtype/dashboard-types'

class DashboardItem extends Component {
    wrapper: HTMLElement
    onResize: Function
    createWebcomponentUrl: Function
    props: {
        item: DBItem,
        dashboard: Dashboard,
        layout?: DBItem.layout,
        dragCancelClassName?: string,
        currentLayout: ?{},
        showError: Function,
        isLocked: boolean
    }
    state: {
        height: ?number,
        width: ?number
    }
    static defaultProps = {
        item: {},
        dashboard: {}
    }
    
    constructor() {
        super()
        this.state = {
            height: null,
            width: null
        }
        this.onResize = this.onResize.bind(this)
        this.createWebcomponentUrl = this.createWebcomponentUrl.bind(this)
    }
    
    onResize() {
        this.setState({
            height: this.wrapper && this.wrapper.offsetHeight,
            width: this.wrapper && this.wrapper.offsetWidth,
        })
    }
    
    createWebcomponentUrl() {
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
    
    createCustomComponent() {
        const {item} = this.props
        
        const componentsAndProps = {
            'streamr-label': {
                component: StreamrLabel,
                props: {}
            },
            'streamr-button': {
                component: StreamrButton
            },
            'streamr-text-field': {
                component: StreamrTextField
            },
            'streamr-switcher': {
                component: StreamrSwitcher
            }
        }
        
        const {component, props} = componentsAndProps[item.webcomponent] || {}
        
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

const mapStateToProps = ({dashboard: {dashboardsById, openDashboard}}) => ({
    dashboard: dashboardsById[openDashboard.id]
})

const mapDispatchToProps = (dispatch) => ({
    showError(message) {
        dispatch(showError({
            title: 'Error!',
            message
        }))
    }
})

export default connect(mapStateToProps, mapDispatchToProps)(DashboardItem)