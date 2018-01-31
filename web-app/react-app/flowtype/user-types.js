
export type User = {
    id?: number,
    email: string,
    name: string,
    username: string,
    timezone: string
}



export type Action = {
    type: string,
    user: User,
    error?: string,
    timezone?: string,
    name?: string,
    id: string
}