// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import {StreamrBreadcrumb} from '../../Breadcrumb'
import {MenuItem} from 'react-bootstrap'
import FontAwesome from 'react-fontawesome'
import createLink from '../../../createLink'

import {Responsive, WidthProvider} from 'react-grid-layout'
import 'react-grid-layout/css/styles.css'
import 'react-resizable/css/styles.css'

import DashboardItem from './DashboardItem'
import ShareDialog from '../../ShareDialog'

import {updateDashboard, deleteDashboard, lockDashboardEditing, unlockDashboardEditing} from '../../../actions/dashboard'

import styles from './editor.pcss'

declare var _: any

import type {Dashboard} from '../../../flowtype/dashboard-types'
import ConfirmButton from '../../ConfirmButton/index'

const ResponsiveReactGridLayout = WidthProvider(Responsive)

class Editor extends Component {
    
    onLayoutChange: Function
    generateLayout: Function
    onResize: Function
    onBeforeUnload: Function
    onMenuToggle: Function
    props: {
        dashboard: Dashboard,
        canShare: boolean,
        canWrite: boolean,
        delete: Function,
        update: Function,
        editorLocked: Function,
        lockEditing: Function,
        unlockEditing: Function
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
                lg: 16,
                md: 10,
                sm: 4,
                xs: 2,
            },
            layoutsByItemId: {}
        }
        this.onLayoutChange = this.onLayoutChange.bind(this)
        this.generateLayout = this.generateLayout.bind(this)
        this.onResize = this.onResize.bind(this)
        this.onBeforeUnload = this.onBeforeUnload.bind(this)
        this.onMenuToggle = this.onMenuToggle.bind(this)
    }
    
    componentDidMount() {
        window.addEventListener('beforeunload', this.onBeforeUnload)
        
        const menuToggle = document.getElementById('main-menu-toggle')
        menuToggle && menuToggle.addEventListener('click', this.onMenuToggle)
    }
    
    onMenuToggle() {
        const menuIsOpen = document.body && document.body.classList && document.body.classList.contains('mmc')
        if (menuIsOpen) {
            this.props.unlockEditing()
        } else {
            this.props.lockEditing()
        }
    }
    
    onLayoutChange(layout, allLayouts) {
        this.onResize(layout)
        this.props.update({
            layout: allLayouts
        })
    }
    
    generateLayout() {
        const sizes = ['lg', 'md', 'sm', 'xs']
        const db = this.props.dashboard
        return _.zipObject(sizes, _.map(sizes, size => {
            return db.items.map(item => {
                const layout = db.layout && db.layout[size] && db.layout[size].find(layout => layout.i === item.id) || {}
                const defaultLayout = {
                    i: item.id,
                    x: 0,
                    y: 0,
                    h: 2,
                    w: 4,
                    minH: 2,
                    minW: 3
                }
                return {
                    ...defaultLayout,
                    ...layout
                }
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
    
    onBeforeUnload() {
        if (!this.props.dashboard.saved) {
            return 'You have unsaved changes in your Dashboard. Are you sure you want to leave?'
        }
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
                    <StreamrBreadcrumb.Item href={createLink('dashboard/list')}>
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
                    rowHeight={60}
                    breakpoints={this.state.breakpoints}
                    cols={this.state.cols}
                    draggableCancel={`.${dragCancelClassName}`}
                    onLayoutChange={this.onLayoutChange}
                    onResize={this.onResize}
                    onResizeEnd={this.onResize}
                    isDraggable={!this.props.editorLocked}
                    isResizable={!this.props.editorLocked}
                >
                    {items.map(dbItem => (
                        <div key={dbItem.id.toString()}>
                            <DashboardItem item={dbItem} currentLayout={this.state.layoutsByItemId[dbItem.id]} dragCancelClassName={dragCancelClassName}/>
                        </div>
                    ))}
                </ResponsiveReactGridLayout>
            </div>
        )
    }
}

const mapStateToProps = ({dashboard}) => {
    const db = dashboard.dashboardsById[dashboard.openDashboard.id] || {}
    const canShare = db.new !== true && (db.ownPermissions && db.ownPermissions.includes('share'))
    const canWrite = db.new !== true && (db.ownPermissions && db.ownPermissions.includes('write'))
    return {
        dashboard: db,
        canShare,
        canWrite,
        editorLocked: db.editingLocked || (!db.new && !canWrite)
    }
}

const mapDispatchToProps = (dispatch, ownProps) => ({
    delete: () => dispatch(deleteDashboard(ownProps.dashboard.id)),
    update: (changes) => dispatch(updateDashboard({
        ...ownProps.dashboard,
        ...changes
    })),
    lockEditing() {
        dispatch(lockDashboardEditing(ownProps.dashboard.id))
    },
    unlockEditing() {
        dispatch(unlockDashboardEditing(ownProps.dashboard.id))
    }
})

export default connect(mapStateToProps, mapDispatchToProps)(Editor)