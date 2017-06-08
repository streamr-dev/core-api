// @flux

import React, {Component} from 'react'
import {connect} from 'react-redux'
import {Breadcrumb, BreadcrumbItem} from '../../Breadcrumb'

import {Responsive, WidthProvider} from 'react-grid-layout'
const ResponsiveReactGridLayout = WidthProvider(Responsive)
import 'react-grid-layout/css/styles.css'
import 'react-resizable/css/styles.css'

import DashboardItem from './DashboardItem'

import {updateDashboard} from '../../../actions/dashboard'

import styles from './editor.pcss'

declare var Streamr: {
    createLink: Function
}

declare var _: any

import type {Dashboard} from '../../../flowtype/dashboard-types'

class Editor extends Component {
    
    onLayoutChange: Function
    generateLayout: Function
    props: {
        dashboard: Dashboard,
        dispatch: Function
    }
    state: {
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
                return layout || {
                    i: item.id,
                    x: 0,
                    y: 0,
                    h: 2,
                    w: 3
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
    
    render() {
        const {dashboard} = this.props
        const layout = dashboard.items && this.generateLayout()
        const items = dashboard.items || []
        const dragCancelClassName = 'cancelDragging' + Date.now()
        return (
            <div id="content-wrapper" className="scrollable" style={{
                height: '100%'
            }}>
                <Breadcrumb>
                    <BreadcrumbItem href={Streamr.createLink('dashboard', 'list')}>
                        Dashboards
                    </BreadcrumbItem>
                    <BreadcrumbItem href={Streamr.createLink('dashboard', 'editor', 1)} active={true}>
                        {dashboard && dashboard.name || 'New Dashboard'}
                    </BreadcrumbItem>
                </Breadcrumb>
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
                            <DashboardItem currentLayout={this.state.layoutsByItemId[dbItem.id]} item={dbItem} dashboard={dashboard} dragCancelClassName={dragCancelClassName}/>
                        </div>
                    ))}
                </ResponsiveReactGridLayout>
            </div>
        )
    }
}

export default connect()(Editor)