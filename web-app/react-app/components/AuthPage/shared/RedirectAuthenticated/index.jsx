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
    redirect = () => {
        this.getIsAuthenticated().then((authenticated) => {
            if (authenticated) {
                const { search } = this.props.location
                const url = qs.parse(search, {
                    ignoreQueryPrefix: true,
                }).redirect || createLink('/canvas/editor')

                window.location.assign(url)
            }
        })
    }

    getIsAuthenticated = (): Promise<boolean> => this.props.blindly ? (
        Promise.resolve(true)
    ) : (
        new Promise((resolve) => {
            axios
                .get(createLink('/api/v1/users/me'))
                .then(() => {
                    resolve(true)
                }, () => {
                    resolve(false)
                })
        })
    )

    componentDidMount = () => {
        this.redirect()
    }

    componentDidUpdate = (prevProps: Props) => {
        if (this.props.blindly !== prevProps.blindly) {
            this.redirect()
        }
    }

    render = () => null
}

export default withRouter(RedirectAuthenticated)
