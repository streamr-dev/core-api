
export type IntegrationKey = {
    name: string,
    type: string,
    json: {}
}

export type State = {
    listsByService?: {
        [string]: Array<IntegrationKey>
    },
    error?: ?string,
    fetching?: boolean
}

export type Action = {
    type: string,
    service?: string,
    integrationKey?: IntegrationKey,
    integrationKeys?: Array<IntegrationKey>,
    error?: string,
    id: string
}
