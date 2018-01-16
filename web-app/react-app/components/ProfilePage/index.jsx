// @flow

import React, {Component} from 'react'

import { Row, Col } from 'react-bootstrap'

import ProfileSettings from './ProfileSettings'
import APICredentials from './APICredentials'
import IntegrationKeyHandler from './IntegrationKeyHandler'
import Notifier from '../Notifier'

export default class ProfilePage extends Component<{}> {
    render() {
        return (
            <Row>
                <Notifier/>
                <Col xs={12} sm={6}>
                    <ProfileSettings />
                </Col>
                <Col xs={12} sm={6}>
                    <APICredentials />
                </Col>
                <Col xs={12} sm={6}>
                    <IntegrationKeyHandler />
                </Col>
                <Notifier/>
            </Row>
        )
    }
}