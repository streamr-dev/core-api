// @flow

import * as React from 'react'
import axios from 'axios'

import createLink from '../../../../utils/createLink'

type Props = {
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

    componentDidMount = () => {
        this.getIsAuthenticated().then((authenticated) => {
            this.setState({
                authenticated,
            })

            if (authenticated) {
                window.location.href = createLink('/canvas/editor')
            }
        })
    }

    render = () => {
        const { children } = this.props
        const { authenticated } = this.state

        return authenticated !== null && authenticated !== true && React.Children.only(children)
    }
}

export default OnlyUnauthenticated
