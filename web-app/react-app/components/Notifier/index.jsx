// @flow

import {Component} from 'react'
import {connect} from 'react-redux'

import type {Notification, State as NotificationState} from '../../flowtype/notification-types.js'

declare var Streamr: {
    showSuccess: (message: ?string, title: string, delay: number) => void,
    showError: (message: ?string, title: string, delay: number) => void
}

type Props = {
    notifications: {
        [Notification.id]: Notification
    }
}

export class Notifier extends Component<Props> {
    
    createNotification = ({title, message, delay, type}: Notification) => {
        switch (type) {
            case 'success':
                return Streamr.showSuccess(message, title, delay)
            case 'info':
                return Streamr.showSuccess(message, title, delay)
            case 'error':
                return Streamr.showError(message, title, delay)
        }
    }
    
    componentWillReceiveProps({notifications}: Props) {
        if (notifications) {
            Object.values(notifications).forEach((notification: Notification) => {
                if (notification.id && !this.props.notifications[notification.id]) {
                    this.createNotification(notification)
                }
            })
        }
    }
    
    render() {
        return null
    }
}

export const mapStateToProps = ({notifications}: NotificationState) => ({
    notifications: notifications.byId
})

export default connect(mapStateToProps)(Notifier)