// @flow

import React, {Component} from 'react'
import {Panel, Row, Alert} from 'react-bootstrap'
import IntegrationKeyHandlerSegment from '../IntegrationKeyHandler/IntegrationKeyHandlerSegment'
import type {IntegrationKey} from '../../../flowtype/integration-key-types'
import {createIdentity} from '../../../actions/integrationKey'
import {connect} from 'react-redux'
import type {ErrorInUi} from '../../../flowtype/common-types'
import type {IntegrationKeyState} from '../../../flowtype/states/integration-key-state'
import {deleteIntegrationKey, getIntegrationKeysByService} from '../../../actions/integrationKey'

type StateProps = {
    error: ?ErrorInUi,
    integrationKeys: Array<IntegrationKey>
}

type DispatchProps = {
    createIdentity: (integrationKey: IntegrationKey) => void,
    deleteIntegrationKey: (id: $ElementType<IntegrationKey, 'id'>) => void,
    getIntegrationKeysByService: (service: $ElementType<IntegrationKey, 'service'>) => void
}

type Props = StateProps & DispatchProps

const service = 'ETHEREUM_ID'

type State = {
    hasWeb3: boolean,
}
export class IdentityHandler extends Component<Props, State> {
    state = {
        hasWeb3: false,
    }

    componentDidMount() {
        // TODO: Move to (yet non-existent) router
        this.props.getIntegrationKeysByService(service)
        this.requestMetamaskAccess()
    }

    onNew = (integrationKey: IntegrationKey) => {
        const name = integrationKey.name
        delete integrationKey.name
        this.props.createIdentity({
            name,
            service,
            json: integrationKey,
        })
    }

    onDelete = (id: $ElementType<IntegrationKey, 'id'>) => {
        this.props.deleteIntegrationKey(id)
    }

    requestMetamaskAccess(askPermission: boolean = false): Promise<void> {
        // Checks for legacy access. Asks to unlock if possible.
        return Promise.resolve()
            .then(() => {
                if (!window.web3 && !window.ethereum) {
                    throw new Error('Metamask not detected')
                }
                return window.web3.eth.defaultAccount || (askPermission ? window.ethereum.enable() : undefined)
            })
            .then((account) => {
                if (account) {
                    this.setState({
                        hasWeb3: true,
                    })
                }
            }, (err) => {
                console.warn(err)
                this.setState({
                    hasWeb3: false,
                })
            })
    }

    render() {
        const {hasWeb3} = this.state
        return (
            <Panel>
                <Panel.Heading>
                    Ethereum Identities
                </Panel.Heading>
                <Panel.Body>
                    <p>
                        These Ethereum accounts are bound to your Streamr user. You can use them to authenticate and to participate on the Streamr Marketplace.
                    </p>
                    <Row>
                        <IntegrationKeyHandlerSegment
                            onNew={this.onNew}
                            onDelete={this.onDelete}
                            service={service}
                            integrationKeys={this.props.integrationKeys}
                            copy="address"
                            showInput={hasWeb3}
                            tableFields={[
                                ['address', (add) => add && (typeof add === 'string') && `${add.substring(0, 15)}...` || add]
                            ]}
                        />
                    </Row>
                    {!hasWeb3 && (
                        <Alert bsStyle="danger">
                            To bind Ethereum addresses to your Streamr account, you need an Ethereum-enabled browser.
                            Try the <a href="https://metamask.io">MetaMask plugin for Chrome</a> or the <a href="https://github.com/ethereum/mist/releases">Mist browser</a>.
                            If you have Metamask installed already, please <a href="#" onClick={() => this.requestMetamaskAccess(true)} >click here</a> to grant permission to read your account information.
                        </Alert>
                    )}
                </Panel.Body>
            </Panel>
        )
    }
}

export const mapStateToProps = ({integrationKey: {listsByService, error}}: {integrationKey: IntegrationKeyState}): StateProps => ({
    integrationKeys: listsByService[service] || [],
    error
})

export const mapDispatchToProps = (dispatch: Function): DispatchProps => ({
    deleteIntegrationKey(id: $ElementType<IntegrationKey, 'id'>) {
        dispatch(deleteIntegrationKey(id))
    },
    createIdentity(integrationKey: IntegrationKey) {
        dispatch(createIdentity(integrationKey))
    },
    getIntegrationKeysByService(service: $ElementType<IntegrationKey, 'service'>) {
        dispatch(getIntegrationKeysByService(service))
    }
})

export default connect(mapStateToProps, mapDispatchToProps)(IdentityHandler)
