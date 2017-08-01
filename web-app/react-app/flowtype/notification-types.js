
export type Notification = {
    id: ?(string | number),
    title: string,
    message: string,
    delay: number,
    type?: 'success' | 'info' | 'error'
}

export type State = {
    byId: {
        [Notification.id]: Notification
    }
}

export type Action = {
    type: string,
    id: string,
    notification?: Notification
}