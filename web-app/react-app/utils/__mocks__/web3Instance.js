const ownWeb3 = jest.genMockFromModule('../web3Instance')

ownWeb3.eth = {
    defaultAccount: '0x12345',
    personal: {
        sign: (dataToSign, address) => {
            return 'signature'
        }
    }
}

export default ownWeb3
