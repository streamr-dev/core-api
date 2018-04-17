// @flow

import type {Permission} from './permission-types'

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
