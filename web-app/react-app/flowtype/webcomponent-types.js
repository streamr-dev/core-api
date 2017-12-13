
import type {StreamId} from './streamr-client-types'

export type WebcomponentProps = {
    url: string,
    stream?: StreamId,
    height: ?number,
    width: ?number,
    onError: ?Function
}

export type Webcomponent = {
    type: 'streamr-button' |
        'streamr-chart' |
        'streamr-heatmap' |
        'streamr-label' |
        'streamr-map' |
        'streamr-switcher' |
        'streamr-table' |
        'streamr-text-field',
    url: string
}