// @flow

import React, {Component} from 'react'
import {any} from 'prop-types'
import {connect} from 'react-redux'
import {
    StreamrBreadcrumb,
    StreamrBreadcrumbItem,
    StreamrBreadcrumbDropdownButton,
    StreamrBreadcrumbToolbar,
    StreamrBreadcrumbToolbarButton
} from '../../Breadcrumb'
import {MenuItem} from 'react-bootstrap'
import FontAwesome from 'react-fontawesome'
import Fullscreen from 'react-full-screen'
import StreamrClient from 'streamr-client'
import _ from 'lodash'

import {parseDashboard} from '../../../helpers/parseState'
import createLink from '../../../helpers/createLink'

import {Responsive, WidthProvider} from 'react-grid-layout'
import 'react-grid-layout/css/styles.css'

import DashboardItem from './DashboardItem'
import ShareDialog from '../../ShareDialog'
import DeleteButton from '../DashboardDeleteButton'
import StreamrClientProvider from '../../WebComponents/StreamrClientProvider'

import {
    updateDashboardChanges,
    lockDashboardEditing,
    unlockDashboardEditing,
    updateDashboardLayout
} from '../../../actions/dashboard'

import type {Dashboard} from '../../../flowtype/dashboard-types'

type BeforeUnloadEvent = {
    returnValue: any
}

type Layout = Array<{
    i: string
}>

const ResponsiveReactGridLayout = WidthProvider(Responsive)

const client = new StreamrClient({
    url: 'ws://127.0.0.1:8890/api/v1/ws',
    authKey: 'tester1-api-key', //TODO: CHANGE!!!!!!!
    autoconnect: true,
    autoDisconnect: false
})

class Editor extends Component {
    
    onLayoutChange: Function
    generateLayout: Function
    onResize: Function
    onBeforeUnload: Function
    onMenuToggle: Function
    onFullscreenToggle: Function
    
    props: {
        dashboard: Dashboard,
        canShare: boolean,
        canWrite: boolean,
        delete: Function,
        update: Function,
        editorLocked: Function,
        lockEditing: Function,
        unlockEditing: Function,
        updateDashboardLayout: Function
    }
    state: {
        breakpoints: {},
        cols: {},
        layoutsByItemId: {},
        isFullscreen: boolean
    }
    
    static contextTypes = {
        router: any
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
            layoutsByItemId: {},
            isFullscreen: false
        }
        this.onLayoutChange = this.onLayoutChange.bind(this)
        this.generateLayout = this.generateLayout.bind(this)
        this.onResize = this.onResize.bind(this)
        this.onBeforeUnload = this.onBeforeUnload.bind(this)
        this.onMenuToggle = this.onMenuToggle.bind(this)
        this.onFullscreenToggle = this.onFullscreenToggle.bind(this)
    }
    
    componentDidMount() {
        window.addEventListener('beforeunload', this.onBeforeUnload)
        
        const menuToggle = document.getElementById('main-menu-toggle')
        menuToggle && menuToggle.addEventListener('click', this.onMenuToggle)
    }
    
    componentWillReceiveProps(nextProps) {
        if (this.props.dashboard.id !== nextProps.dashboard.id) {
            this.context.router.push(`/${nextProps.dashboard.id || ''}`)
        }
    }
    
    onMenuToggle() {
        const menuIsOpen = document.body && document.body.classList && document.body.classList.contains('mmc')
        if (menuIsOpen) {
            this.props.unlockEditing(this.props.dashboard.id)
        } else {
            this.props.lockEditing(this.props.dashboard.id)
        }
    }
    
    onLayoutChange(layout, allLayouts) {
        this.onResize(layout)
        this.props.updateDashboardLayout(this.props.dashboard.id, allLayouts)
    }
    
    onFullscreenToggle(value?: boolean) {
        this.setState({
            isFullscreen: value !== undefined ? value : !this.state.isFullscreen
        })
    }
    
    generateLayout() {
        const sizes = ['lg', 'md', 'sm', 'xs']
        const db = this.props.dashboard
        return _.zipObject(sizes, _.map(sizes, size => {
            return db.items.map(item => {
                const layout = db.layout && db.layout[size] && db.layout[size].find(layout => layout.i === Editor.generateItemId(item)) || {}
                const defaultLayout = {
                    i: Editor.generateItemId(item),
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
    
    onResize(layout: Layout) {
        this.setState({
            layoutsByItemId: {
                ...this.state.layoutsByItemId,
                ...(_.groupBy(layout, item => item.i))
            }
        })
    }
    
    onBeforeUnload(e: BeforeUnloadEvent) {
        if (!this.props.dashboard.saved) {
            const message = 'You have unsaved changes in your Dashboard. Are you sure you want to leave?'
            e.returnValue = message
            return message
        }
    }
    
    static generateItemId(item: DashboardItem) {
        return `${item.canvas.id}-${item.module}`
    }
    
    render() {
        const {dashboard} = this.props
        const layout = dashboard.items && this.generateLayout()
        const items = dashboard.items ? _.sortBy(dashboard.items, ['canvas', 'module']) : []
        const dragCancelClassName = 'cancelDragging' + Date.now()
        return (
            <div id="content-wrapper" className="scrollable" style={{
                height: '100%'
            }}>
                <Fullscreen
                    enabled={this.state.isFullscreen}
                    onChange={(value: boolean) => this.onFullscreenToggle(value)}
                >
                    <StreamrBreadcrumb>
                        <StreamrBreadcrumbItem href={createLink('dashboard/list')}>
                            Dashboards
                        </StreamrBreadcrumbItem>
                        <StreamrBreadcrumbItem active={true}>
                            {dashboard && dashboard.name || 'New Dashboard'}
                        </StreamrBreadcrumbItem>
                        {(this.props.canShare || this.props.canWrite) && (
                            <StreamrBreadcrumbDropdownButton title={(
                                <FontAwesome name="cog"/>
                            )}>
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
                                    <DeleteButton buttonProps={{
                                        componentClass: MenuItem,
                                        bsClass: ''
                                    }}>
                                        <FontAwesome name="trash-o"/> Delete
                                    </DeleteButton>
                                )}
                            </StreamrBreadcrumbDropdownButton>
                        )}
                        <StreamrBreadcrumbToolbar>
                            <StreamrBreadcrumbToolbarButton iconName="expand" onClick={this.onFullscreenToggle}/>
                        </StreamrBreadcrumbToolbar>
                    </StreamrBreadcrumb>
                    <StreamrClientProvider client={client}>
                        <ResponsiveReactGridLayout
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
                                <div key={Editor.generateItemId(dbItem)}>
                                    <DashboardItem item={dbItem}
                                                   currentLayout={this.state.layoutsByItemId[Editor.generateItemId(dbItem)]}
                                                   dragCancelClassName={dragCancelClassName}/>
                                </div>
                            ))}
                        </ResponsiveReactGridLayout>
                    </StreamrClientProvider>
                </Fullscreen>
            </div>
        )
    }
}

const mapStateToProps = (state) => {
    const baseState = parseDashboard(state)
    return {
        ...baseState,
        editorLocked: !baseState.dashboard.new && !baseState.canWrite
    }
}

const mapDispatchToProps = (dispatch) => ({
    update(id, changes) {
        return dispatch(updateDashboardChanges(id, changes))
    },
    lockEditing(id) {
        return dispatch(lockDashboardEditing(id))
    },
    unlockEditing(id) {
        return dispatch(unlockDashboardEditing(id))
    },
    updateDashboardLayout(id, layout) {
        return dispatch(updateDashboardLayout(id, layout))
    }
})

export default connect(mapStateToProps, mapDispatchToProps)(Editor)