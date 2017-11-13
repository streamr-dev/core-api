// @flow

import {Component} from 'react'
import {connect} from 'react-redux'

import type {Notification} from '../../flowtype/notification-types.js'

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
    
    createNotification({title, message, delay, type}) {
        switch (type) {
            case 'success':
                return Streamr.showSuccess(message, title, delay)
            case 'info':
                return Streamr.showSuccess(message, title, delay)
            case 'error':
                return Streamr.showError(message, title, delay)
        }
    }
    
    componentWillReceiveProps({notifications}) {
        if (notifications) {
            for (const notification of Object.values(notifications)) {
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

export const mapStateToProps = ({notifications: {byId}}) => ({
    notifications: byId
})

export default connect(mapStateToProps)(Notifier)