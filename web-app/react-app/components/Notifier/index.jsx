// @flow

import {Component} from 'react'
import {connect} from 'react-redux'

declare var Streamr: any

import type {Notification} from '../../flowtype/notification-types.js'

class Notifier extends Component {
    props: {
        notifications: {
            [Notification.id]: Notification
        }
    }
    
    createNotification({title, message, delay, type}) {
        switch (type) {
            case 'success':
                return Streamr.showSuccess(title, message, delay)
            case 'info':
                return Streamr.showSuccess(title, message, delay)
            case 'error':
                return Streamr.showError(title, message, delay)
        }
    }
    
    componentWillReceiveProps({notifications}) {
        if (notifications) {
            for (const id in notifications) {
                const notification = notifications[id]
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

const mapStateToProps = ({notifications: {byId}}) => ({
    notifications: byId
})

export default connect(mapStateToProps)(Notifier)