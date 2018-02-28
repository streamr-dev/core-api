// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import {error} from 'react-notification-system-redux'
import path from 'path'
import createLink from '../../../../helpers/createLink'
import TitleRow from './DashboardItemTitleRow'

import styles from './dashboardItem.pcss'

import type {DashboardState} from '../../../../flowtype/states/dashboard-state'
import type {Dashboard, DashboardItem as DashboardItemType} from '../../../../flowtype/dashboard-types'
import type {Webcomponent} from '../../../../flowtype/webcomponent-types'

const config = require('../../dashboardConfig')

type StateProps = {
    dashboard: ?Dashboard,
    config: {
        components: {
            [$ElementType<Webcomponent, 'type'>]: {
                component?: any,
                props: {}
            }
        }
    }
}

type DispatchProps = {
    error: (message: string) => void
}

type GivenProps = {
    item: DashboardItemType,
    layout?: $ElementType<DashboardItemType, 'layout'>,
    dragCancelClassName?: string,
    currentLayout: ?{},
    isLocked: boolean
}

type Props = StateProps & DispatchProps & GivenProps

type State = {
    height: ?number,
    width: ?number
}

export class DashboardItem extends Component<Props, State> {
    wrapper: ?HTMLElement
    static defaultProps = {
        item: {},
        dashboard: {},
    }
    state = {
        height: null,
        width: null,
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
            (dashboard && !dashboard.new) ? `dashboards/${dashboard.id}` : '',
            `canvases/${canvas}`,
            `modules/${itemModule}`,
        ))
    }

    onError = (err: { message: string, stack: string }) => {
        const inProd = process.env.NODE_ENV === 'production'
        this.props.error(inProd ? 'Something went wrong!' : err.message)
        if (!inProd) {
            console.error(err.stack)
        }
    }

    createCustomComponent = () => {
        const {item, config: conf} = this.props

        const {component: CustomComponent, props} = item && item.webcomponent && conf.components[item.webcomponent] || {}

        return CustomComponent ? (
            <CustomComponent
                url={this.createWebcomponentUrl()}
                height={this.state.height}
                width={this.state.width}
                onError={this.onError}
                {...(props || {})}
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
                        className={`${styles.wrapper} ${item && (item.webcomponent && styles[item.webcomponent] || item.webcomponent)}`}
                        ref={wrapper => this.wrapper = wrapper}
                    >
                        {this.createCustomComponent() || (
                            <div style={{
                                color: 'red',
                                textAlign: 'center',
                            }}>
                                Sorry, unknown component:(
                            </div>
                        )}
                    </div>
                </div>
            </div>
        )
    }
}

export const mapStateToProps = ({dashboard: {dashboardsById, openDashboard}}: { dashboard: DashboardState }): StateProps => ({
    dashboard: openDashboard.id ? dashboardsById[openDashboard.id] : null,
    config: config,
})

export const mapDispatchToProps = (dispatch: Function): DispatchProps => ({
    error(message: string) {
        dispatch(error({
            title: 'Error!',
            message,
        }))
    },
})

export default connect(mapStateToProps, mapDispatchToProps)(DashboardItem)
