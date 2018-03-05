// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import {withRouter} from 'react-router-dom'
import {
    StreamrBreadcrumb,
    StreamrBreadcrumbItem,
    StreamrBreadcrumbDropdownButton,
    StreamrBreadcrumbToolbar,
    StreamrBreadcrumbToolbarButton,
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

const dashboardConfig = require('../dashboardConfig')
const config = require('../../../config')

import {
    updateDashboardChanges,
    lockDashboardEditing,
    unlockDashboardEditing,
    updateDashboardLayout,
} from '../../../actions/dashboard'

import type {DashboardState} from '../../../flowtype/states/dashboard-state'
import type {Dashboard, Layout, LayoutItem} from '../../../flowtype/dashboard-types'

const ResponsiveReactGridLayout = WidthProvider(Responsive)

declare var keyId: string

import styles from './editor.pcss'

type StateProps = {
    dashboard: ?Dashboard,
    canShare: boolean,
    canWrite: boolean,
    editorLocked: boolean
}

type DispatchProps = {
    update: (id: $ElementType<Dashboard, 'id'>, changes: {}) => Promise<Dashboard>,
    lockEditing: (id: $ElementType<Dashboard, 'id'>) => void,
    unlockEditing: (id: $ElementType<Dashboard, 'id'>) => void,
    updateDashboardLayout: (id: $ElementType<Dashboard, 'id'>, layout: Layout) => void
}

type GivenProps = {}

type RouterProps = {
    history: {
        push: Function
    }
}

type Props = StateProps & DispatchProps & GivenProps & RouterProps

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
    isFullscreen: boolean,
    sharingDialogIsOpen: boolean
}

export class Editor extends Component<Props, State> {
    client: StreamrClient

    static defaultProps = {
        dashboard: {
            name: '',
            items: [],
        },
    }

    state = {
        breakpoints: dashboardConfig.layout.breakpoints,
        cols: dashboardConfig.layout.cols,
        layoutsByItemId: {},
        isFullscreen: false,
        sharingDialogIsOpen: false,
    }

    constructor() {
        super()
        this.client = new StreamrClient({
            url: config.wsUrl,
            authKey: keyId,
            autoconnect: true,
            autoDisconnect: false,
        })
    }

    componentDidMount() {
        window.addEventListener('beforeunload', this.onBeforeUnload)

        const menuToggle = document.getElementById('main-menu-toggle')
        menuToggle && menuToggle.addEventListener('click', this.onMenuToggle)
    }

    componentWillReceiveProps(nextProps: Props) {
        if (this.props.dashboard && nextProps.dashboard && this.props.dashboard.id !== nextProps.dashboard.id) {
            this.props.history.push(`/${nextProps.dashboard.id || ''}`)
        }
    }

    onMenuToggle = () => {
        const menuIsOpen = document.body && document.body.classList && document.body.classList.contains('mmc')
        if (menuIsOpen) {
            this.props.dashboard && this.props.unlockEditing(this.props.dashboard.id)
        } else {
            this.props.dashboard && this.props.lockEditing(this.props.dashboard.id)
        }
    }

    onDragStop = () => {

    }

    onLayoutChange = (layout: DashboardItem.layout, allLayouts: Layout) => {
        this.onResize(layout)
        this.props.dashboard && this.props.updateDashboardLayout(this.props.dashboard.id, allLayouts)
    }

    onFullscreenToggle = (value?: boolean) => {
        this.setState({
            isFullscreen: value !== undefined ? value : !this.state.isFullscreen,
        })
    }

    generateLayout = (): ?Layout => {
        const db = this.props.dashboard
        const layout = db && _.zipObject(dashboardConfig.layout.sizes, _.map(dashboardConfig.layout.sizes, (size: 'lg' | 'md' | 'sm' | 'xs') => {
            return db.items.map(item => {
                const id = Editor.generateItemId(item)
                const layout = db.layout && db.layout[size] && db.layout[size].find(layout => layout.i === id)
                return item.webcomponent ? {
                    ...dashboardConfig.layout.defaultLayout,
                    ...dashboardConfig.layout.layoutsBySizeAndModule[size][item.webcomponent],
                    ...(layout || {}),
                    i: id,
                } : {}
            })
        }))
        return layout
    }

    onResize = (layout: Array<LayoutItem>) => {
        this.setState({
            layoutsByItemId: layout.reduce((result, item) => {
                result[item.i] = item
                return result
            }, {}),
        })
    }

    onBeforeUnload = (e: Event & { returnValue: ?string }): ?string => {
        if (this.props.dashboard && this.props.dashboard.id && !this.props.dashboard.saved) {
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
        const layout = dashboard && dashboard.items && this.generateLayout()
        const items = dashboard && dashboard.items ? _.sortBy(dashboard.items, ['canvas', 'module']) : []
        const dragCancelClassName = 'cancelDragging' + Date.now()
        const locked = this.props.editorLocked || this.state.isFullscreen
        return dashboard ? (
            <div
                id="content-wrapper"
                className={`scrollable ${styles.editor}`}
            >
                <Fullscreen
                    enabled={this.state.isFullscreen}
                    onChange={this.onFullscreenToggle}
                >
                    <div style={{
                        backgroundColor: '#f6f6f6',
                        height: '100%',
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
                                        <MenuItem
                                            onClick={() => this.setState({
                                                sharingDialogIsOpen: true,
                                            })}
                                            className={styles.dropdownShareButton}
                                        >
                                            <FontAwesome name="user"/> Share
                                        </MenuItem>
                                    )}
                                    {this.props.canWrite && (
                                        <DeleteButton
                                            buttonProps={{
                                                componentClass: MenuItem,
                                                bsClass: styles.dropdownDeleteButton,
                                            }}
                                        >
                                            <FontAwesome name="trash-o"/> Delete
                                        </DeleteButton>
                                    )}
                                    <ShareDialog
                                        isOpen={this.state.sharingDialogIsOpen}
                                        onClose={() => this.setState({
                                            sharingDialogIsOpen: false,
                                        })}
                                        resourceType="DASHBOARD"
                                        resourceId={this.props.dashboard && this.props.dashboard.id}
                                        resourceTitle={`Dashboard ${this.props.dashboard ? this.props.dashboard.name : ''}`}
                                    />
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
                                onDragStop={this.onDragStop}
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
        ) : null
    }
}

export const mapStateToProps = (state: { dashboard: DashboardState }): StateProps => {
    const baseState = parseDashboard(state)
    const {dashboard} = baseState
    return {
        ...baseState,
        editorLocked: !!dashboard && (dashboard.editingLocked || (!dashboard.new && !baseState.canWrite)),
    }
}

export const mapDispatchToProps = (dispatch: Function): DispatchProps => ({
    update(id: $ElementType<Dashboard, 'id'>, changes: {}) {
        return dispatch(updateDashboardChanges(id, changes))
    },
    lockEditing(id: $ElementType<Dashboard, 'id'>) {
        dispatch(lockDashboardEditing(id))
    },
    unlockEditing(id: $ElementType<Dashboard, 'id'>) {
        dispatch(unlockDashboardEditing(id))
    },
    updateDashboardLayout(id: $ElementType<Dashboard, 'id'>, layout: Layout) {
        dispatch(updateDashboardLayout(id, layout))
    },
})

export default connect(mapStateToProps, mapDispatchToProps)(withRouter(Editor))
