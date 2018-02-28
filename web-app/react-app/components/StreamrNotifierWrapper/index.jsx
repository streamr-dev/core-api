// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'

import Notifications from 'react-notification-system-redux'

type StateProps = {
    notifications: any
}

type Props = StateProps

const defaultColorOverrides = {
    success: '#468847',
    error: '#a94442',
    info: '#31708f'
}

const style = {
    NotificationItem: { // Override the notification item
        DefaultStyle: { // Applied to every notification, regardless of the notification level
            padding: '15px',
            minHeight: '73px',
            height: 'auto',
            fontSize: '14px',
            boxShadow: '0px 2px 10px rgba(50, 50, 50, 0.5)',
            borderRadius: '4px',
            borderTop: 'none'
        },
        success: {
            color: defaultColorOverrides.success
        },
        info: {
            color: defaultColorOverrides.info
        },
        error: {
            color: defaultColorOverrides.error
        }
    },
    Title: {
        DefaultStyle: {
            fontSize: '18px'
        },
        success: {
            color: defaultColorOverrides.success
        },
        info: {
            color: defaultColorOverrides.info
        },
        error: {
            color: defaultColorOverrides.error
        }
    },
    Dismiss: {
        DefaultStyle: {
            backgroundColor: 'none',
            fontSize: '14px'
        },
        success: {
            color: defaultColorOverrides.success
        },
        info: {
            color: defaultColorOverrides.info
        },
        error: {
            color: defaultColorOverrides.error
        }
    }
}

export class StreamrNotifierWrapper extends Component<Props> {
    render() {
        return (
            <Notifications
                notifications={this.props.notifications}
                style={style}
            />
        )
    }
}

export const mapStateToProps = ({notifications}: {notifications: any}): StateProps => ({
    notifications
})

export default connect(mapStateToProps)(StreamrNotifierWrapper)
