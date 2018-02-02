// @flow

// These are not complete, but I don't need more right now. When we do, we'll fill these

import type {UiChannel} from './common-types.js'

export type Canvas = {
    id: string,
    name: string,
    created: string,
    updated: string,
    adhoc: boolean,
    state: 'RUNNING' | 'STOPPED',
    modules: Array<CanvasModule>,
    settings: {},
    uiChannel: UiChannel
}

export type CanvasModule = {
    id: number,
    name: string,
    type: string,
    hash: number,
    params: Array<CanvasModuleParam>,
    inputs: Array<CanvasModuleInput>,
    outputs: Array<CanvasModuleOutput>,
    uiChannel: UiChannel,
    checked?: boolean
}

type CanvasModuleParam = {
    id: string,
    name: string,
    longName: string,
    value: any,
    defaultValue: any,
    acceptedTypes: Array<string>,
    canConnect: boolean,
    export: boolean,
    connected: boolean,
    isTextArea: boolean,
    type: string,
    requiresConnection: boolean,
    canToggleDrivingInput: boolean,
    drivingInput: boolean
}

type CanvasModuleInput = {

}

type CanvasModuleOutput = {

}
