// @flux

import React, {Component} from 'react'
import {connect} from 'react-redux'
import path from 'path'
import createLink from '../../../../createLink'
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
        const dbItem = this.props.item || {}
        return (
            <div className={styles.dashboardItem}>
                <div className={styles.header}>
                    <TitleRow dashboard={this.props.dashboard} item={dbItem} dragCancelClassName={this.props.dragCancelClassName}/>
                </div>
                <div className={`${styles.body} ${this.props.dragCancelClassName || ''}`}>
                    <div className={`${styles.wrapper} ${styles[dbItem.webcomponent] || dbItem.webcomponent}`}>
                        {dbItem.webcomponent && (
                            <WebComponent
                                ref={i => this.a = i}
                                type={dbItem.webcomponent}
                                onError={this.props.showError}
                                webComponentRef={item => this.webcomponent = item}
                                url={createLink(path.resolve('/api/v1/dashboards', dbItem.dashboard.toString(), 'canvases', dbItem.canvas.toString(), 'modules', dbItem.module.toString()))}
                            />
                        )}
                    </div>
                </div>
            </div>
        )
    }
}

const mapStateToProps = () => ({})

const mapDispatchToProps = (dispatch) => ({
    showError({detail}) {
        dispatch(showError({
            title: 'Error!',
            message: detail.message
        }))
    }
})

export default connect(mapStateToProps, mapDispatchToProps)(DashboardItem)