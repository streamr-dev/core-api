
import type {User} from './user-types'

export type Permission = {
    resourceType: 'DASHBOARD' | 'CANVAS' | 'STREAM',
    resourceId: string,
    userId: User.id,
    operations: Array<('read' | 'write' | 'share')>
}