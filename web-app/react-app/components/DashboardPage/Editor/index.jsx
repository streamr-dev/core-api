// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import {StreamrBreadcrumb} from '../../Breadcrumb'
import {MenuItem} from 'react-bootstrap'
import FontAwesome from 'react-fontawesome'

import {Responsive, WidthProvider} from 'react-grid-layout'
import 'react-grid-layout/css/styles.css'
import 'react-resizable/css/styles.css'

import DashboardItem from './DashboardItem'
import ShareDialog from '../../ShareDialog'

import {updateDashboard, deleteDashboard} from '../../../actions/dashboard'

import styles from './editor.pcss'

declare var Streamr: {
    createLink: Function
}

declare var _: any

import type {Dashboard} from '../../../flowtype/dashboard-types'
import ConfirmButton from '../../ConfirmButton/index'

const ResponsiveReactGridLayout = WidthProvider(Responsive)

class Editor extends Component {
    
    onLayoutChange: Function
    generateLayout: Function
    onResize: Function
    onDelete: Function
    props: {
        dashboard: Dashboard,
        dispatch: Function,
        canShare: boolean,
        canWrite: boolean
    }
    state: {
        breakpoints: {},
        cols: {},
        layoutsByItemId: {}
    }
    static defaultProps = {
        dashboard: {
            name: '',
            items: []
        }
    }
    
    constructor(props) {
        super(props)
        this.state = {
            breakpoints: {
                lg: 1200,
                md: 996,
                sm: 768,
                xs: 480
            },
            cols: {
                lg: 12,
                md: 8,
                sm: 4,
                xs: 2,
            },
            layoutsByItemId: {}
        }
        this.onLayoutChange = this.onLayoutChange.bind(this)
        this.generateLayout = this.generateLayout.bind(this)
        this.onResize = this.onResize.bind(this)
        this.onDelete = this.onDelete.bind(this)
    }
    
    onLayoutChange(layout, allLayouts) {
        this.onResize(layout)
        this.props.dispatch(updateDashboard({
            ...this.props.dashboard,
            layout: allLayouts
        }))
    }
    
    generateLayout() {
        const sizes = ['lg', 'md', 'sm', 'xs']
        const db = this.props.dashboard
        return _.zipObject(sizes, _.map(sizes, size => {
            return db.items.map(item => {
                const layout = db.layout && db.layout[size] && db.layout[size].find(layout => layout.i === item.id)
                const defaultLayout = {
                    i: item.id,
                    x: 0,
                    y: 0,
                    h: 2,
                    w: 3
                }
                return layout || defaultLayout
            })
        }))
    }
    
    onResize(items) {
        this.setState({
            layoutsByItemId: {
                ...this.state.layoutsByItemId,
                ...(_.groupBy(items, item => item.i))
            }
        })
    }
    
    onDelete() {
    
    }

    render() {
        const {dashboard} = this.props
        const layout = dashboard.items && this.generateLayout()
        const items = dashboard.items || []
        const dragCancelClassName = 'cancelDragging' + Date.now()
        return (
            <div id="content-wrapper" className="scrollable" style={{
                height: '100%'
            }}>
                <StreamrBreadcrumb className="breadcrumb-page">
                    <StreamrBreadcrumb.Item href={Streamr.createLink('dashboard', 'list')}>
                        Dashboards
                    </StreamrBreadcrumb.Item>
                    <StreamrBreadcrumb.Item active={true}>
                        {dashboard && dashboard.name || 'New Dashboard'}
                    </StreamrBreadcrumb.Item>
                    {(this.props.canShare || this.props.canWrite) && (
                        <StreamrBreadcrumb.DropdownButton title={(
                            <FontAwesome name="cog"/>
                        )} className={styles.streamrDropdownButton}>
                            {this.props.canShare && (
                                <ShareDialog
                                    resourceType="DASHBOARD"
                                    resourceId={this.props.dashboard.id}
                                    resourceTitle={`Dashboard ${this.props.dashboard.name}`}
                                >
                                    <MenuItem>
                                        <FontAwesome name="user"/> Share
                                    </MenuItem>
                                </ShareDialog>
                            )}
                            {this.props.canWrite && (
                                <MenuItem>
                                    <FontAwesome name="pencil"/> Rename
                                </MenuItem>
                            )}
                            {this.props.canWrite && (
                                <ConfirmButton
                                    buttonProps={{
                                        componentClass: MenuItem,
                                        bsClass: ''
                                    }}
                                    confirmMessage={`Are you sure you want to delete Dashboard ${this.props.dashboard.name}?`}
                                    confirmCallback={() => {
                                    }}>
                                    <FontAwesome name="trash-o"/> Delete
                                </ConfirmButton>
                            )}
                        </StreamrBreadcrumb.DropdownButton>
                    )}
                </StreamrBreadcrumb>
                <streamr-client id="client" url="ws://dev.streamr/api/v1/ws" autoconnect="true" autodisconnect="false"/>
                <ResponsiveReactGridLayout
                    className={styles.dashboard}
                    layouts={layout}
                    rowHeight={100}
                    breakpoints={this.state.breakpoints}
                    cols={this.state.cols}
                    draggableCancel={`.${dragCancelClassName}`}
                    onLayoutChange={this.onLayoutChange}
                    onResize={this.onResize}
                    onResizeEnd={this.onResize}
                >
                    {items.map(dbItem => (
                        <div key={dbItem.id.toString()}>
                            <DashboardItem currentLayout={this.state.layoutsByItemId[dbItem.id]} item={dbItem}
                                           dashboard={dashboard} dragCancelClassName={dragCancelClassName}/>
                        </div>
                    ))}
                </ResponsiveReactGridLayout>
            </div>
        )
    }
}

const mapStateToProps = ({dashboard}) => {
    const db = dashboard.dashboardsById[dashboard.openDashboard.id] || {}
    return {
        dashboard: db,
        canShare: db.new !== true && (db.ownPermissions && db.ownPermissions.includes('share')),
        canWrite: db.new !== true && (db.ownPermissions && db.ownPermissions.includes('write'))
    }
}

const mapDispatchToProps = (dispatch, ownProps) => ({
    deleteDashboard: () => dispatch(deleteDashboard(ownProps.dashboard.id))
})

export default connect(mapStateToProps, mapDispatchToProps)(Editor)