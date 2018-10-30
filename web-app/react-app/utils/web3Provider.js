// @flow

import Web3 from 'web3'

declare var web3: Web3
declare var ethereum: Web3

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

export const getWeb3 = (): StreamrWeb3 => {
    if (typeof ethereum !== 'undefined') {
        return new StreamrWeb3(ethereum)
    } else if (typeof web3 !== 'undefined') {
        return new StreamrWeb3(web3.currentProvider)
    }
    return new StreamrWeb3(false)
}

export const requestMetamaskPermission = () => {
    window.postMessage({
        type: 'ETHEREUM_PROVIDER_REQUEST',
    }, '*')
}

export default getWeb3

