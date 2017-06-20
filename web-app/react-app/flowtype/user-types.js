
export type User = {
    id?: number,
    email: string
}

export type State = {
    currentUser: ?User
}

export type Action = {
    type: string,
    user: User
}