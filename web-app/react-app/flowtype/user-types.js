
export type User = {
    id?: number,
    email: string,
    name: string,
    username: string,
    timezone: string
}

type State = {
    currentUser: User,
    error?: ?string,
    fetching?: boolean
}

type Action = {
    type: string,
    user: User,
    error?: string,
    timezone?: string,
    name?: string,
    id: string
}