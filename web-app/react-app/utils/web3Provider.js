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
}

const sharedWeb3 = new StreamrWeb3(typeof web3 !== 'undefined' && web3.currentProvider)

export const getWeb3 = (): StreamrWeb3 => sharedWeb3

export default getWeb3
