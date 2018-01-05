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

import type {Dashboard, DashboardReducerState as DashboardState, Layout, LayoutItem} from '../../../flowtype/dashboard-types'

const ResponsiveReactGridLayout = WidthProvider(Responsive)

declare var keyId: string

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
    breakpoints: {
        lg: number,
        md: number,
        sm: number,
        xs: number
    },
    cols: {
        lg: number,
        md: number,
        sm: number,
        xs: number
    },
    layoutsByItemId: {
        [DashboardItem.id]: DashboardItem.layout
    },
    isFullscreen: boolean
}

export class Editor extends Component<Props, State> {
    client: StreamrClient
    
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
    
    constructor() {
        super()
        this.client = new StreamrClient({
            url: 'ws://127.0.0.1:8890/api/v1/ws',
            authKey: keyId,
            autoconnect: true,
            autoDisconnect: false
        })
    }
    
    componentDidMount() {
        window.addEventListener('beforeunload', this.onBeforeUnload)
        
        const menuToggle = document.getElementById('main-menu-toggle')
        menuToggle && menuToggle.addEventListener('click', this.onMenuToggle)
    }
    
    componentWillReceiveProps(nextProps: Props) {
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
    
    onLayoutChange = (layout: DashboardItem.layout, allLayouts: Layout) => {
        this.onResize(layout)
        this.props.updateDashboardLayout(this.props.dashboard.id, allLayouts)
    }
    
    onFullscreenToggle = (value?: boolean) => {
        this.setState({
            isFullscreen: value !== undefined ? value : !this.state.isFullscreen
        })
    }
    
    generateLayout = (): Layout => {
        const db = this.props.dashboard
        const layout = _.zipObject(config.layout.sizes, _.map(config.layout.sizes, (size: 'lg' | 'md' | 'sm' | 'xs') => {
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
        return layout
    }
    
    onResize = (layout: Array<LayoutItem>) => {
        this.setState({
            layoutsByItemId: layout.reduce((result, item) => {
                result[item.i] = item
                return result
            }, {})
        })
    }
    
    onBeforeUnload = (e: Event & { returnValue: ?string }): ?string => {
        if (this.props.dashboard.id && !this.props.dashboard.saved) {
            const message = 'You have unsaved changes in your Dashboard. Are you sure you want to leave?'
            e.returnValue = message
            return message
        }
    }
    
    static generateItemId(item: DashboardItem): string {
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
                    onChange={this.onFullscreenToggle}
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
                        <StreamrClientProvider client={this.client}>
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

export const mapStateToProps = (state: {dashboard: DashboardState}) => {
    const baseState = parseDashboard(state)
    return {
        ...baseState,
        editorLocked: baseState.dashboard.editingLocked || (!baseState.dashboard.new && !baseState.canWrite)
    }
}

export const mapDispatchToProps = (dispatch: Function) => ({
    update(id: Dashboard.id, changes: {}) {
        return dispatch(updateDashboardChanges(id, changes))
    },
    lockEditing(id: Dashboard.id) {
        return dispatch(lockDashboardEditing(id))
    },
    unlockEditing(id: Dashboard.id) {
        return dispatch(unlockDashboardEditing(id))
    },
    updateDashboardLayout(id: Dashboard.id, layout: Layout) {
        return dispatch(updateDashboardLayout(id, layout))
    }
})

export default connect(mapStateToProps, mapDispatchToProps)(withRouter(Editor))