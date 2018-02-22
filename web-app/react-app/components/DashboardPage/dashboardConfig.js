import _ from 'lodash'

const sizes = ['lg', 'md', 'sm', 'xs']
const modules = [
    'streamr-button',
    'streamr-chart',
    'streamr-heatmap',
    'streamr-input',
    'streamr-label',
    'streamr-map',
    'streamr-switcher',
    'streamr-table',
    'streamr-text-field'
]

import StreamrLabel from '../WebComponents/StreamrLabel'
import StreamrButton from '../WebComponents/StreamrButton'
import StreamrTextField from '../WebComponents/StreamrTextField'
import StreamrSwitcher from '../WebComponents/StreamrSwitcher'
import StreamrMap from '../WebComponents/StreamrMap'
import StreamrHeatmap from '../WebComponents/StreamrHeatmap'
import StreamrChart from '../WebComponents/StreamrChart'
import StreamrTable from '../WebComponents/StreamrTable'

const defaultLayout = {
    x: 0,
    y: 0,
    h: 2,
    w: 4,
    minH: 2,
    minW: 2
}
const overridesBySize = {}
const overridesByModule = {
    'streamr-button': {
        w: 2
    },
    'streamr-switcher': {
        w: 2
    },
    'streamr-label': {
        w: 2
    },
    'streamr-text-field': {
        w: 2,
        h: 3
    },
    'streamr-map': {
        h: 6
    },
    'streamr-heatmap': {
        h: 6
    },
    'streamr-chart': {
        h: 6
    },
    'streamr-table': {
        h: 6
    }
}
const overridesBySizeAndModule = {}

module.exports = {
    layout: {
        sizes: sizes,
        breakpoints: {
            lg: 1200,
            md: 996,
            sm: 768,
            xs: 480
        },
        cols: {
            lg: 16,
            md: 10,
            sm: 4,
            xs: 2,
        },
        defaultLayout,
        layoutsBySizeAndModule: _.zipObject(sizes, _.map(sizes, size => _.zipObject(modules, _.map(modules, module => ({
            ...(size && overridesBySize[size] || {}),
            ...(module && overridesByModule[module] || {}),
            ...(size && module && overridesBySizeAndModule[size] && overridesBySizeAndModule[size][module] || {})
        })))))
    },
    components: {
        'streamr-button': {
            component: StreamrButton,
            props: {}
        },
        'streamr-heatmap': {
            component: StreamrHeatmap
        },
        'streamr-label': {
            component: StreamrLabel,
        },
        'streamr-map': {
            component: StreamrMap
        },
        'streamr-switcher': {
            component: StreamrSwitcher
        },
        'streamr-text-field': {
            component: StreamrTextField
        },
        'streamr-chart': {
            component: StreamrChart
        },
        'streamr-table': {
            component: StreamrTable
        }
    }
}
