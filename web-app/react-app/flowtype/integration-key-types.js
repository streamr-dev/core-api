
export type IntegrationKey = {
    name: string,
    service: string,
    json: {}
}

export type Action = {
    type: string,
    service?: string,
    integrationKey?: IntegrationKey,
    integrationKeys?: Array<IntegrationKey>,
    error?: string,
    id: string
}
