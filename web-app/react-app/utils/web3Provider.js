// @flow

import Web3 from 'web3'

declare var web3: Web3

export class StreamrWeb3 extends Web3 {
    getDefaultAccount = (): Promise<string> => new Promise((resolve, reject) => {
        this.eth.getAccounts()
            .then((accounts) => {
                (Array.isArray(accounts) && accounts.length > 0) ? resolve(accounts[0]) : reject(new Error('MetaMask browser extension is locked'))
            })
            .catch((e) => reject(e))
    })
    isEnabled = (): boolean => !!this.currentProvider
    ethereumBalance = (address: string): Promise<number> => {
        return this.eth.getBalance(address)
            .then((balance) => {
                return this.utils.fromWei(balance)
            })
    }
    DATABalance = (address: string): Promise<number> => {
        const tokenABI = [{
            'constant': true,
            'inputs': [{
                'name': '_owner',
                'type': 'address'
            }],
            'name': 'balanceOf',
            'outputs': [{
                'name': 'balance',
                'type': 'uint256'
            }],
            'payable': false,
            'type': 'function'
        }]
        const tokenAddress = '0x0cf0ee63788a0849fe5297f3407f701e122cc023'
        const tokenContract = new this.eth.Contract(tokenABI, tokenAddress)
        return tokenContract.methods.balanceOf(address).call()
            .then((balance) => {
                return Number(this.utils.fromWei(balance))
            })
    }
}

const sharedWeb3 = new StreamrWeb3(typeof web3 !== 'undefined' && web3.currentProvider)

export const getWeb3 = (): StreamrWeb3 => sharedWeb3

export default getWeb3
