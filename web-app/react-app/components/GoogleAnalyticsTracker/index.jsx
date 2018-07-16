// @flow

import { Component } from 'react'
import { withRouter, type Location } from 'react-router-dom'
import ReactGA from 'react-ga'
import { googleAnalyticsId } from '../../config'

type Props = {
    location: Location,
}

class GoogleAnalyticsTracker extends Component<Props> {
    constructor(props: Props) {
        super(props)
        ReactGA.initialize(googleAnalyticsId)
        this.logPageview(this.props.location.pathname)
    }

    componentWillReceiveProps(newProps: Props) {
        if (newProps.location && (!this.props.location || newProps.location.pathname !== this.props.location.pathname)) {
            this.logPageview(newProps.location.pathname)
        }
    }

    logPageview = (page) => {
        ReactGA.pageview(page)
    }

    render = () => null
}

export default withRouter(GoogleAnalyticsTracker)
