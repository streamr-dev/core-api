// @flow

import * as React from 'react'
import axios from 'axios'
import { withRouter } from 'react-router-dom'
import qs from 'qs'

import createLink from '../../../../utils/createLink'

type Props = {
    blindly?: boolean,
    location: {
        search: string,
    }
}

class RedirectAuthenticated extends React.Component<Props> {
    redirect = (initial: boolean = false) => {
        const { search } = this.props.location
        const { ignoreSession, redirect } = qs.parse(search, {
            ignoreQueryPrefix: true,
        })
        if (!initial || !ignoreSession || ignoreSession === 'false') {
            this.getIsAuthenticated().then((authenticated) => {
                if (authenticated) {
                    const url = redirect || createLink('/canvas/editor')
                    window.location.assign(url)
                }
            })
        }
    }

    getIsAuthenticated = (): Promise<boolean> => this.props.blindly ? (
        Promise.resolve(true)
    ) : (
        axios
            .get(createLink('/api/v1/users/me'))
            .then(
                () => Promise.resolve(true),
                () => Promise.resolve(false)
            )
    )

    componentDidMount = () => {
        this.redirect(true)
    }

    componentDidUpdate = (prevProps: Props) => {
        if (this.props.blindly !== prevProps.blindly) {
            this.redirect()
        }
    }

    render = () => null
}

export default withRouter(RedirectAuthenticated)
