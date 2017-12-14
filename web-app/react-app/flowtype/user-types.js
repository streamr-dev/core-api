
export type User = {
    id?: number,
    email: string,
    name: string,
    username: string,
    timezone: string
}

export type State = {
    currentUser: User,
    error?: ?string,
    fetching?: boolean
}

export type Action = {
    type: string,
    user: User,
    error?: string,
    timezone?: string,
    name?: string,
    id: string
}