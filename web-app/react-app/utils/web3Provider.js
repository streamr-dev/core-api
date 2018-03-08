// @flow

import Web3 from 'web3'

declare var web3: Web3

class StreamrWeb3 extends Web3 {
    getDefaultAccount = (): Promise<string> => new Promise((resolve, reject) => {
        this.eth.getAccounts().then(([account]) => {
            account ? resolve(account) : reject(new Error('MetaMask browser extension is locked'))
        })
    })
    isEnabled = (): boolean => !!this.currentProvider
}

const sharedWeb3 = new StreamrWeb3(web3.currentProvider)

export default (): StreamrWeb3 => sharedWeb3
