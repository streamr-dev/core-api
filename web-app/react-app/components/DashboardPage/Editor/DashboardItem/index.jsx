// @flux

import React, {Component} from 'react'
import {connect} from 'react-redux'
import {showError} from '../../../../actions/notification'

import TitleRow from './DashboardItemTitleRow'
import WebComponent from '../../../WebComponent'

import styles from './dashboardItem.pcss'
import './webcomponentStyles.css'

import type {Dashboard, DashboardItem as DBItem} from '../../../../flowtype/dashboard-types'

class DashboardItem extends Component {
    
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
        if (this.webcomponent) {
            this.webcomponent.dispatchEvent(event)
        }
    }

    render() {
        const {item, dashboard} = this.props
        return (
            <div className={styles.dashboardItem}>
                <div className={styles.header}>
                    <TitleRow dashboard={this.props.dashboard} item={item} dragCancelClassName={this.props.dragCancelClassName}/>
                </div>
                <div className={`${styles.body} ${this.props.dragCancelClassName || ''}`}>
                    <div className={`${styles.wrapper} ${styles[item.webcomponent] || item.webcomponent}`}>
                        {item.webcomponent && (
                            <WebComponent
                                ref={i => this.a = i}
                                type={item.webcomponent}
                                onError={this.props.showError}
                                webComponentRef={item => this.webcomponent = item}
                                dashboardId={dashboard.id}
                                canvasId={item.canvas}
                                moduleId={item.module.toString()}
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