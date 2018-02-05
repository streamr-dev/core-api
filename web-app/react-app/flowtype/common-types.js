// @flow

import type {Webcomponent} from './webcomponent-types'

export type ErrorFromApi = {
    error: string,
    code?: string
}

export type ErrorInUi = {
    message: string,
    statusCode?: number,
    code?: string
}

export type UiChannel = {
    id: string,
    webcomponent: $ElementType<Webcomponent, 'type'>,
    name: string
}

export type OnSubmitEvent = {
    target: HTMLFormElement
} & Event
