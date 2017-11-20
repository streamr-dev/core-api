// @flow

import {Component} from 'react'
import {connect} from 'react-redux'

import type {Notification, State} from '../../flowtype/notification-types.js'

declare var Streamr: {
    showSuccess: (message: ?string, title: string, delay: number) => void,
    showError: (message: ?string, title: string, delay: number) => void
}

export class Notifier extends Component {
    props: {
        notifications: {
            [Notification.id]: Notification
        }
    }
    
    createNotification(notification: Notification) {
        const {title, message, delay, type}: {title: string, message: string, delay: number, type: string} = notification
        switch (type) {
            case 'success':
                return Streamr.showSuccess(message, title, delay)
            case 'info':
                return Streamr.showSuccess(message, title, delay)
            case 'error':
                return Streamr.showError(message, title, delay)
        }
    }
    
    componentWillReceiveProps({notifications}: {notifications: State.byId}) {
        if (notifications) {
            for (const notification: Notification of notifications) {
                if (notification && !this.props.notifications[notification.id]) {
                    this.createNotification(notification)
                }
            }
        }
    }
    
    render() {
        return null
    }
}

export const mapStateToProps = ({notifications: {byId}}: State) => ({
    notifications: byId
})

export default connect(mapStateToProps)(Notifier)