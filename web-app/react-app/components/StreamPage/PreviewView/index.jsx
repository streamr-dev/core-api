// @flow

declare var keyId: string

import React, {Component} from 'react'
import {connect} from 'react-redux'
import StreamrClient from 'streamr-client'
import {Panel, Table, Modal, Button} from 'react-bootstrap'
import FontAwesome from 'react-fontawesome'
import moment from 'moment-timezone'
import stringifyObject from 'stringify-object'

const config = require('../../../config')

import type {Stream} from '../../../flowtype/stream-types'
import type {User} from '../../../flowtype/user-types'
import type {StreamState} from '../../../flowtype/states/stream-state'
import type {UserState} from '../../../flowtype/states/user-state'

import styles from './previewView.pcss'

type DataPoint = {
    data: {},
    metadata: {
        timestamp: number
    }
}

type StateProps = {
    stream: ?Stream,
    currentUser: ?User
}

type Props = StateProps

type State = {
    visibleData: Array<DataPoint>,
    visibleDataLimit: number,
    paused: boolean,
    infoScreenMessage: ?DataPoint
}

export class PreviewView extends Component<Props, State> {
    client: StreamrClient
    subscription: any
    state = {
        visibleData: [],
        visibleDataLimit: 10,
        paused: false,
        infoScreenMessage: null
    }

    constructor() {
        super()
        this.client = new StreamrClient({
            url: config.wsUrl,
            authKey: keyId,
            autoconnect: true,
            autoDisconnect: false
        })
    }

    componentWillReceiveProps(newProps: Props) {
        if (newProps.stream && newProps.stream.id && !this.subscription) {
            this.subscription = this.client.subscribe({
                stream: newProps.stream.id,
                resend_last: this.state.visibleDataLimit
            }, (data, metadata) => this.onData({
                data,
                metadata
            }))
        }
    }

    onData = (dataPoint: DataPoint) => {
        this.setState({
            visibleData: [
                dataPoint,
                ...this.state.visibleData
            ].slice(0, this.state.visibleDataLimit)
        })
    }

    openInfoScreen = (d: DataPoint) => {
        this.setState({
            infoScreenMessage: d
        })
    }

    closeInfoScreen = () => {
        this.setState({
            infoScreenMessage: null
        })
    }

    pause = () => {
        this.client.pause()
        this.setState({
            paused: true
        })
    }

    unpause = () => {
        this.client.connect()
        this.setState({
            paused: false
        })
    }

    static prettyPrintData = (data: ?{}, compact: boolean = false) => {
        return stringifyObject(data, {
            indent: '  ',
            inlineCharacterLimit: compact ? Infinity : 5
        })
    }

    static prettyPrintDate = (timestamp: ?number, timezone: ?string) => timestamp && moment.tz(timestamp, timezone).format()

    render() {
        const tz = this.props.currentUser && this.props.currentUser.timezone || moment.tz.guess()
        return (
            <Panel>
                <Panel.Heading>
                    Realtime Data Preview
                    <div className="panel-heading-controls">
                        {this.state.paused ? (
                            <Button
                                bsSize="sm"
                                bsStyle="primary"
                                onClick={this.unpause}
                                title="Continue"
                            >
                                <FontAwesome name="play"/>
                            </Button>
                        ) : (
                            <Button
                                bsSize="sm"
                                onClick={this.pause}
                                title="Pause"
                            >
                                <FontAwesome name="pause"/>
                            </Button>
                        )}
                    </div>
                </Panel.Heading>
                <Panel.Body>
                    <Table className={styles.dataTable} striped condensed hover>
                        <thead>
                            <tr>
                                <th>Timestamp</th>
                                <th>Message JSON</th>
                                <th/>
                            </tr>
                        </thead>
                        <tbody>
                            {this.state.visibleData.map(d => (
                                <tr key={JSON.stringify(d.metadata)}>
                                    <td className={styles.timestampColumn}>
                                        {PreviewView.prettyPrintDate(d.metadata && d.metadata.timestamp, tz)}
                                    </td>
                                    <td className={styles.messageColumn}>
                                        <div className={styles.messagePreview}>
                                            {PreviewView.prettyPrintData(d.data, true)}
                                        </div>
                                    </td>
                                    <td>
                                        <a href="#" onClick={() => this.openInfoScreen(d)}>
                                            <FontAwesome name="question-circle"/>
                                        </a>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </Table>
                </Panel.Body>
                <Modal
                    show={this.state.infoScreenMessage != null}
                    onHide={this.closeInfoScreen}
                >
                    <Modal.Header closeButton>
                        Info about data point
                    </Modal.Header>
                    <Modal.Body>
                        <Table className={styles.infoScreenModalTable}>
                            <tbody>
                                <tr>
                                    <th>Stream id</th>
                                    <td>{this.props.stream && this.props.stream.id}</td>
                                </tr>
                                <tr>
                                    <th>Message Timestamp</th>
                                    <td>{PreviewView.prettyPrintDate(this.state.infoScreenMessage && this.state.infoScreenMessage.metadata && this.state.infoScreenMessage.metadata.timestamp, tz)}</td>
                                </tr>
                                <tr>
                                    <th>Data</th>
                                    <td className={styles.dataColumn}>
                                        <code>
                                            {PreviewView.prettyPrintData(this.state.infoScreenMessage && this.state.infoScreenMessage.data)}
                                        </code>
                                    </td>
                                </tr>
                            </tbody>
                        </Table>
                    </Modal.Body>
                </Modal>
            </Panel>
        )
    }
}

const mapStateToProps = ({stream, user}: {stream: StreamState, user: UserState}): StateProps  => ({
    stream: stream.openStream.id ? stream.byId[stream.openStream.id] : null,
    currentUser: user.currentUser
})

export default connect(mapStateToProps)(PreviewView)
