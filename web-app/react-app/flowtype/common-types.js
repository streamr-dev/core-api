
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
    webcomponent: string,
    name: string
}

