// @flow

import * as React from 'react'
import axios from 'axios'
import { withRouter } from 'react-router-dom'
import qs from 'qs'

import createLink from '../../../../utils/createLink'

const getIsAuthenticated = (): Promise<boolean> => new Promise((resolve) => {
    axios
        .get(createLink('/api/v1/users/me'))
        .then(() => {
            resolve(true)
        }, () => {
            resolve(false)
        })
})

const getRedirectionUrl = (paramString: string) => (
    qs.parse(paramString, {
        ignoreQueryPrefix: true,
    }).redirect || createLink('/canvas/editor')
)

type Props = {
    location: {
        search: string,
    }
}

class RedirectAuthenticated extends React.Component<Props> {
    componentDidMount = () => {
        getIsAuthenticated().then((authenticated) => {
            if (authenticated) {
                window.location.href = getRedirectionUrl(this.props.location.search)
            }
        })
    }

    render = () => null
}

export default withRouter(RedirectAuthenticated)
