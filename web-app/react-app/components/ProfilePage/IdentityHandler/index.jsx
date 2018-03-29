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
import getWeb3 from '../../../utils/web3Provider'

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

type State = {
    balanceData: {},
}

const service = 'ETHEREUM_ID'

export class IdentityHandler extends Component<Props, State> {
    state = {
        balanceData: {},
    }
    componentDidMount() {
        // TODO: Move to (yet non-existent) router
        this.props.getIntegrationKeysByService(service)
    }
    componentWillReceiveProps(nextProps: Props) {
        nextProps.integrationKeys.forEach((key) => {
            const address: string = (Object.entries(key.json)[0][1]: any)
            getWeb3().DATABalance(address)
                .then((balance: number) => {
                    this.setState({
                        balanceData: Object.assign({}, this.state.balanceData, {
                            [address]: balance
                        })
                    })
                })
                .catch(() => {
                    this.setState({
                        balanceData: Object.assign({}, this.state.balanceData, {
                            [address]: 0.0
                        })
                    })
                })
        })
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

    formatBalance = (balance: number) => {
        if (balance === undefined) {
            return '0.0'
        }
        return balance > 999 ? balance.toFixed(0) : balance.toLocaleString('en-US', {
            minimumSignificantDigits: 3,
            maximumSignificantDigits: 5,
        })
    }

    render() {
        const hasWeb3 = getWeb3().isEnabled()
        return (
            <Panel header="Ethereum Identities">
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
                            ['address', (address) => {
                                if (address && (typeof address === 'string')) {
                                    return `${address.substring(0, 15)}... (${this.formatBalance(this.state.balanceData[address])} DATA)`
                                }
                                return address
                            } ]
                        ]}
                    />
                </Row>
                {!hasWeb3 && (
                    <Alert bsStyle="danger">
                        To bind Ethereum addresses to your Streamr account, you need an Ethereum-enabled browser.
                        Try the <a href="https://metamask.io">MetaMask plugin for Chrome</a> or the <a href="https://github.com/ethereum/mist/releases">Mist browser</a>.
                    </Alert>
                )}
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
