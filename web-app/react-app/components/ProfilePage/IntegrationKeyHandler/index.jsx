// @flow

import React, {Component} from 'react'
import {Panel, Row} from 'react-bootstrap'

import IntegrationKeyHandlerSegment from './IntegrationKeyHandlerSegment'
import type {IntegrationKey} from '../../../flowtype/integration-key-types'
import {createIntegrationKey, deleteIntegrationKey, getIntegrationKeysByService} from '../../../actions/integrationKey'
import {connect} from 'react-redux'
import type {IntegrationKeyState} from '../../../flowtype/states/integration-key-state'
import type {ErrorInUi} from '../../../flowtype/common-types'
import getWeb3 from '../../../utils/web3Provider'

type StateProps = {
    integrationKeys: Array<IntegrationKey>,
    error: ?ErrorInUi
}

type DispatchProps = {
    deleteIntegrationKey: (id: $ElementType<IntegrationKey, 'id'>) => void,
    createIntegrationKey: (key: IntegrationKey) => void,
    getIntegrationKeysByService: (service: $ElementType<IntegrationKey, 'service'>) => void
}

type Props = StateProps & DispatchProps

type State = {
    balanceData: {},
}

const service = 'ETHEREUM'

export class IntegrationKeyHandler extends Component<Props, State> {
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
            getWeb3().ethereumBalance('0xFeAACDBBc318EbBF9BB5835D4173C1a7fC24B3b9')
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
        return this.props.createIntegrationKey({
            name,
            service,
            json: integrationKey
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
        return (
            <Panel header="Ethereum Private Keys">
                <p>
                    These Ethereum accounts can be used on Canvases to build data-driven interactions with Ethereum. Even though the private keys are securely stored server-side, we do not recommend having significant amounts of value on these accounts.
                </p>
                <Row>
                    <IntegrationKeyHandlerSegment
                        integrationKeys={this.props.integrationKeys}
                        onNew={this.onNew}
                        onDelete={this.onDelete}
                        service={service}
                        inputFields={['privateKey']}
                        tableFields={[
                            ['address', (address) => {
                                if (address && (typeof address === 'string')) {
                                    return `${address.substring(0, 15)} (${this.formatBalance(this.state.balanceData[address])} ETH)`
                                }
                                return ''
                            }],
                        ]}
                    />
                </Row>
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
    createIntegrationKey(key: IntegrationKey) {
        dispatch(createIntegrationKey(key))
    },
    getIntegrationKeysByService(service: $ElementType<IntegrationKey, 'service'>) {
        dispatch(getIntegrationKeysByService(service))
    }
})

export default connect(mapStateToProps, mapDispatchToProps)(IntegrationKeyHandler)
