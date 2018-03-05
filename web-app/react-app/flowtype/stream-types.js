// @flow

import type {Permission} from './permission-types'

export type CSVImporterSchema = {
    headers: Array<string>,
    timeZone: string,
    timestampColumnIndex: ?number
}

export type StreamField = {
    name: string,
    type: string
}

export type Stream = {
    id: string,
    name: string,
    description: string,
    config: {
        fields?: Array<StreamField>
    },
    ownPermissions: Array<$ElementType<Permission, 'operation'>>
}
