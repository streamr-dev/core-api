// @flow

import {
    CREATE_NOTIFICATION,
    REMOVE_NOTIFICATION
} from '../actions/notification.js'

import type {State, Action} from '../flowtype/notification-types.js'

const initialState = {
    byId: {}
}

export default function(state: State = initialState, action: Action) : State {
    
    switch (action.type) {
        case CREATE_NOTIFICATION:
            return {
                ...state,
                byId: {
                    ...state.byId,
                    [action.notification.id]: action.notification
                }
            }
        case REMOVE_NOTIFICATION:
            return {
                ...state,
                byId: {
                    ...state.byId,
                    [action.id]: undefined
                }
            }
        default:
            return state
    }
}