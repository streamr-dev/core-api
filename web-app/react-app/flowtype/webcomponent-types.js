// @flow

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
