
export type ResourceType = 'STREAM' | 'USER'
export type ResourceId = string | 'me'

export type Key = {
    id: string,
    name: string,
    user: ?string,
    permission?: 'read' | 'write' | 'share'
}

export type State = {
    keysByResourceTypeAndId: {
        [ResourceType]: {
            [ResourceId]: Array<Key>
        }
    },
    error?: ?string,
    fetching: boolean
}

export type Action = {
    type: string,
    resourceType?: ResourceType,
    resourceId?: ResourceId,
    keys?: Array<Key>,
    key?: Key,
    keyId?: Key.id,
    error?: string
}