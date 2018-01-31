
export type Notification = {
    title: string,
    message: string,
    position?: 'tr' | 'tc' | 'tl' | 'br' | 'bc' | 'bl',
    autoDismiss?: number,
    action?: {
        label: string,
        callback: () => void
    }
}