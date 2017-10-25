// @flow

import React, {Component} from 'react'
import {object} from 'prop-types'
import {connect} from 'react-redux'
import {Helmet} from 'react-helmet'
import { ShortcutManager, Shortcuts } from 'react-shortcuts'
import Notifier from '../Notifier'
import Sidebar from './Sidebar/index'
import Editor from './Editor/index'
import {updateAndSaveDashboard} from '../../actions/dashboard'

import store from '../../stores/dashboardPageStore'

import type { Dashboard } from '../../flowtype/dashboard-types'
import type { Canvas } from '../../flowtype/canvas-types'
import type {ReactChildren} from 'react-flow-types'

import styles from './dashboardPage.pcss'

const keymap = {
    'MAIN': {
        'SAVE': ['ctrl+s', 'command+s']
    }
}

const shortcutManager = new ShortcutManager(keymap)

class DashboardPage extends Component {
    _handleShortcuts = (action) => {
        switch (action) {
            case 'SAVE': {
                store.dispatch(updateAndSaveDashboard(this.props.dashboard))
                break
            }
        }
    }
    
    static childContextTypes = {
        shortcuts: object
    }
    
    getChildContext() {
        return {
            shortcuts: shortcutManager
        }
    }
    
    props: {
        dashboard: Dashboard,
        canvases: Array<Canvas>,
        children: ReactChildren
    }
    
    render() {
        return (
            <Shortcuts name="MAIN" handler={this._handleShortcuts} className={styles.shortcutHandler} preventDefaultgp>
                <div style={{
                    height: '100%'
                }}>
                    <Helmet>
                        <title>{this.props.dashboard && this.props.dashboard.name || 'New Dashboard'}</title>
                    </Helmet>
                    <Notifier/>
                    <Sidebar/>
                    <Editor/>
                    {this.props.children}
                </div>
            </Shortcuts>
        )
    }
}

const mapStateToProps = ({dashboard: {dashboardsById, openDashboard}}) => ({
    dashboard: dashboardsById[openDashboard.id]
})

export default connect(mapStateToProps, null)(DashboardPage)