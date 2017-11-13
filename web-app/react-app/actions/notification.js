// @flow

import uuid from 'uuid'

export const CREATE_NOTIFICATION = 'CREATE_NOTIFICATION'
export const REMOVE_NOTIFICATION = 'REMOVE_NOTIFICATION'

import type {Notification} from '../flowtype/notification-types.js'

export const showNotification = (notif: Notification) => (dispatch: Function) => {
    const id = notif.id || uuid.v4()
    if (!notif.delay && notif.delay !== 0) {
        notif.delay = 4000
    }
    dispatch({
        type: CREATE_NOTIFICATION,
        notification: {
            ...notif,
            id
        }
    })
    if (notif.delay) {
        setTimeout(() => dispatch(removeNotification(id)), notif.delay)
    }
}

export const showSuccess = (notif: Notification) => showNotification({
    ...notif,
    type: 'success'
})

export const showInfo = (notif: Notification) => showNotification({
    ...notif,
    type: 'info'
})

export const showError = (notif: Notification) => showNotification({
    ...notif,
    type: 'error'
})

export const removeNotification = (id: Notification.id) => ({
    type: REMOVE_NOTIFICATION,
    id
})