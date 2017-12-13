// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import { ShortcutManager, Shortcuts } from 'react-shortcuts'
import store from '../../../stores/dashboardPageStore'

import {updateAndSaveCurrentDashboard} from '../../../actions/dashboard'

import styles from './shortcutHandler.pcss'

import {object} from 'prop-types'
import type {Node} from 'react'

const keymap = {
    'MAIN': {
        'SAVE': ['ctrl+s', 'command+s']
    }
}

const shortcutManager = new ShortcutManager(keymap)

type Props = {
    children: Node
}

export class ShortcutHandler extends Component<Props> {
    static childContextTypes = {
        shortcuts: object
    }
    
    getChildContext() {
        return {
            shortcuts: shortcutManager
        }
    }
    
    _handleShortcuts = (action, event) => {
        switch (action) {
            case 'SAVE': {
                event.preventDefault()
                store.dispatch(updateAndSaveCurrentDashboard())
                break
            }
        }
    }
    
    render() {
        return (
            <Shortcuts name="MAIN" handler={this._handleShortcuts} className={styles.shortcutHandler}>
                {this.props.children}
            </Shortcuts>
        )
    }
}

export const mapDispatchToProps = (dispatch) => ({
    updateAndSaveCurrentDashboard() {
        dispatch(updateAndSaveCurrentDashboard())
    }
})

export default connect(null, mapDispatchToProps)(ShortcutHandler)