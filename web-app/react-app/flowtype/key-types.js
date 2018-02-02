// @flow

export type ResourceType = 'STREAM' | 'USER'
export type ResourceId = string | 'me'

export type Key = {
    id: string,
    name: string,
    user: ?string,
    permission?: 'read' | 'write' | 'share'
}
