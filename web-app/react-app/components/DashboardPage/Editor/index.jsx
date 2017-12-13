// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import {withRouter} from 'react-router-dom'
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

const config = require('../dashboardConfig')

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

type Props = {
    dashboard: Dashboard,
    canShare: boolean,
    canWrite: boolean,
    delete: Function,
    update: Function,
    editorLocked: Function,
    lockEditing: Function,
    unlockEditing: Function,
    updateDashboardLayout: Function,
    history: {
        push: Function
    }
}

type State = {
    breakpoints: {},
    cols: {},
    layoutsByItemId: {},
    isFullscreen: boolean
}

class Editor extends Component<Props, State> {
    
    static defaultProps = {
        dashboard: {
            name: '',
            items: []
        }
    }
    
    state = {
        breakpoints: config.layout.breakpoints,
        cols: config.layout.cols,
        layoutsByItemId: {},
        isFullscreen: false
    }
    
    componentDidMount() {
        window.addEventListener('beforeunload', this.onBeforeUnload)
        
        const menuToggle = document.getElementById('main-menu-toggle')
        menuToggle && menuToggle.addEventListener('click', this.onMenuToggle)
    }
    
    componentWillReceiveProps(nextProps) {
        if (this.props.dashboard.id !== nextProps.dashboard.id) {
            this.props.history.push(`/${nextProps.dashboard.id || ''}`)
        }
    }
    
    onMenuToggle = () => {
        const menuIsOpen = document.body && document.body.classList && document.body.classList.contains('mmc')
        if (menuIsOpen) {
            this.props.unlockEditing(this.props.dashboard.id)
        } else {
            this.props.lockEditing(this.props.dashboard.id)
        }
    }
    
    onLayoutChange = (layout, allLayouts) => {
        this.onResize(layout)
        this.props.updateDashboardLayout(this.props.dashboard.id, allLayouts)
    }
    
    onFullscreenToggle = (value?: boolean) => {
        this.setState({
            isFullscreen: value !== undefined ? value : !this.state.isFullscreen
        })
    }
    
    generateLayout = () => {
        const db = this.props.dashboard
        return _.zipObject(config.layout.sizes, _.map(config.layout.sizes, size => {
            return db.items.map(item => {
                const id = Editor.generateItemId(item)
                const layout = db.layout && db.layout[size] && db.layout[size].find(layout => layout.i === id)
                return {
                    ...config.layout.layoutsBySizeAndModule[size][item.webcomponent],
                    ...(layout || {}),
                    i: id
                }
            })
        }))
    }
    
    onResize = (layout: Layout) => {
        this.setState({
            layoutsByItemId: layout.reduce((result, item) => {
                result[item.i] = item
                return result
            }, {})
        })
    }
    
    onBeforeUnload = (e: BeforeUnloadEvent) => {
        if (!this.props.dashboard.saved) {
            const message = 'You have unsaved changes in your Dashboard. Are you sure you want to leave?'
            e.returnValue = message
            return message
        }
    }
    
    static generateItemId(item: DashboardItem) {
        return `${item.canvas}-${item.module}`
    }
    
    render() {
        const {dashboard} = this.props
        const layout = dashboard.items && this.generateLayout()
        const items = dashboard.items ? _.sortBy(dashboard.items, ['canvas', 'module']) : []
        const dragCancelClassName = 'cancelDragging' + Date.now()
        const locked = this.props.editorLocked || this.state.isFullscreen
        return (
            <div id="content-wrapper" className="scrollable" style={{
                height: '100%'
            }}>
                <Fullscreen
                    enabled={this.state.isFullscreen}
                    onChange={(value: boolean) => this.onFullscreenToggle(value)}
                >
                    <div style={{
                        backgroundColor: '#f6f6f6',
                        height: '100%'
                    }}>
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
                                <StreamrBreadcrumbToolbarButton
                                    iconName="expand"
                                    onClick={() => this.onFullscreenToggle()}
                                />
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
                                isDraggable={!locked}
                                isResizable={!locked}
                                containerPadding={[18, 0]}
                            >
                                {items.map(dbItem => {
                                    const id = Editor.generateItemId(dbItem)
                                    return (
                                        <div key={id}>
                                            <DashboardItem
                                                item={dbItem}
                                                currentLayout={this.state.layoutsByItemId[id]}
                                                dragCancelClassName={dragCancelClassName}
                                                isLocked={locked}
                                            />
                                        </div>
                                    )
                                })}
                            </ResponsiveReactGridLayout>
                        </StreamrClientProvider>
                    </div>
                </Fullscreen>
            </div>
        )
    }
}

const mapStateToProps = (state) => {
    const baseState = parseDashboard(state)
    return {
        ...baseState,
        editorLocked: baseState.dashboard.editingLocked || (!baseState.dashboard.new && !baseState.canWrite)
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

export default connect(mapStateToProps, mapDispatchToProps)(withRouter(Editor))