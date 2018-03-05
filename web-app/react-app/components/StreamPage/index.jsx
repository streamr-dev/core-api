// @flow

declare var streamId: string

import React, {Component} from 'react'
import {connect} from 'react-redux'
import type {Stream} from '../../flowtype/stream-types'
import {getCurrentUser} from '../../actions/user'
import {getMyStreamPermissions, getStream, openStream} from '../../actions/stream'
import PreviewView from './PreviewView'

import type {Node} from 'react'
import type {StreamState} from '../../flowtype/states/stream-state.js'

type GivenProps = {
    children: Node
}

type StateProps = {
    stream: ?Stream
}

type DispatchProps = {
    getStream: (id: $ElementType<Stream, 'id'>) => void,
    openStream: (id: $ElementType<Stream, 'id'>) => void,
    getMyStreamPermissions: (id: $ElementType<Stream, 'id'>) => void,
    getCurrentUser: () => void
}

type Props = GivenProps & StateProps & DispatchProps
type State = {}

export class StreamPage extends Component<Props, State> {

    componentWillMount() {
        const id = streamId
        if (!this.props.stream || id !== this.props.stream.id) {
            this.props.getStream(id)
            this.props.openStream(id)
            this.props.getMyStreamPermissions(id)
            this.props.getCurrentUser()
        }
    }

    render() {
        return (
            <PreviewView />
        )
    }
}

const mapStateToProps = ({stream}: {stream: StreamState}): StateProps => ({
    stream: stream.openStream.id ? stream.byId[stream.openStream.id] : null
})

const mapDispatchToProps = (dispatch: Function): DispatchProps => ({
    getStream(id: $ElementType<Stream, 'id'>) {
        dispatch(getStream(id))
    },
    openStream(id: $ElementType<Stream, 'id'>) {
        dispatch(openStream(id))
    },
    getMyStreamPermissions(id: $ElementType<Stream, 'id'>) {
        dispatch(getMyStreamPermissions(id))
    },
    getCurrentUser() {
        dispatch(getCurrentUser())
    }
})

export default connect(mapStateToProps, mapDispatchToProps)(StreamPage)
