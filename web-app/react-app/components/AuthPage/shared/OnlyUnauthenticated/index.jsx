// @flow

import * as React from 'react'
import axios from 'axios'
import { withRouter } from 'react-router-dom'
import qs from 'qs'

import createLink from '../../../../utils/createLink'

type RouterProps = {
    location: {
        search: string,
    }
}

type Props = RouterProps & {
    children: React.Node,
}

type State = {
    authenticated: ?boolean,
}

class OnlyUnauthenticated extends React.Component<Props, State> {
    state = {
        authenticated: null,
    }

    getIsAuthenticated = (): Promise<boolean> => new Promise((resolve) => {
        axios
            .get(createLink('/api/v1/users/me'))
            .then(() => {
                resolve(true)
            }, () => {
                resolve(false)
            })
    })

    getNewLocation = () => {
        const { redirect } = qs.parse(this.props.location.search, {
            ignoreQueryPrefix: true,
        })
        return redirect || createLink('/canvas/editor')
    }

    componentDidMount = () => {
        this.getIsAuthenticated().then((authenticated) => {
            this.setState({
                authenticated,
            })

            if (authenticated) {
                window.location.href = this.getNewLocation()
            }
        })
    }

    render = () => {
        const { children } = this.props
        const { authenticated } = this.state

        return authenticated !== null && authenticated !== true && React.Children.only(children)
    }
}

export default withRouter(OnlyUnauthenticated)
