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

import type {Dashboard} from '../../../types/dashboard-types'

class Editor extends Component {
    
    onLayoutChange: Function
    props: {
        dashboard: Dashboard,
        dispatch: Function
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
            breakpoints:{
                lg: 1200,
                md: 996,
                sm: 768,
                xs: 480,
                xxs: 0
            },
            cols: {
                lg: 12,
                md: 10,
                sm: 6,
                xs: 4,
                xxs: 2
            }
        }
        this.onLayoutChange = this.onLayoutChange.bind(this)
    }
    
    componentDidMount() {
    
    }
    
    onLayoutChange(layout, allLayouts) {
        this.props.dispatch(updateDashboard({
            ...this.props.dashboard,
            layout: allLayouts
        }))
    }
    
    render() {
        const {dashboard} = this.props
        const items = dashboard.items.map(item => ({
            ...item,
            id: item.id || item.tempId
        }))
        let layout = dashboard.layout
        
        if (!layout) {
            const base = item => ({
                i: item.id.toString(),
                x: 0,
                y: 0,
                h: 2
            })
            layout = {
                lg: items.map(item => ({
                    ...base(item),
                    w: 3,
                })),
                md: items.map(item => ({
                    ...base(item),
                    w: 2,
                })),
                sm: items.map(item => ({
                    ...base(item),
                    w: 2,
                })),
                xs: items.map(item => ({
                    ...base(item),
                    w: 1,
                }))
            }
        }
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
                        {dashboard.name}
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
                >
                    {items.map(dbItem => (
                        <div key={dbItem.id.toString()} data-grid={dbItem.layout || {}}>
                            <DashboardItem item={dbItem} dashboard={dashboard} dragCancelClassName={dragCancelClassName}/>
                        </div>
                    ))}
                </ResponsiveReactGridLayout>
            </div>
        )
    }
}

export default connect()(Editor)