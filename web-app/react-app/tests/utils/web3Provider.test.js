import assert from 'assert-diff'
import getWeb3, {StreamrWeb3} from '../../utils/web3Provider'
import Web3 from 'web3'
import FakeProvider from 'web3-fake-provider'

describe('web3Provider', () => {
    describe('StreamrWeb3', () => {
        it ('must extend Web3', () => {
            assert(StreamrWeb3.prototype instanceof Web3)
        })

        describe('getDefaultAccount', () => {
            let web3
            beforeEach(() => {
                web3 = new StreamrWeb3()
            })
            afterEach(() => {
                web3 = null
            })
            it('must resolve with getAccounts()[0]', async () => {
                web3.eth.getAccounts = () => new Promise(resolve => resolve(['testAccount']))
                const acc = await web3.getDefaultAccount()
                assert.equal(acc, 'testAccount')
            })
            it('must throw error if getAccounts gives undefined/null', async (done) => {
                try {
                    web3.setProvider(new FakeProvider())
                    await web3.getDefaultAccount()
                } catch (e) {
                    assert(e.message.match('is locked'))
                    done()
                }
            })
            it('must throw error if getAccounts gives empty list', async (done) => {
                web3.eth.getAccounts = () => new Promise(resolve => resolve([]))
                try {
                    web3.setProvider(new FakeProvider())
                    await web3.getDefaultAccount()
                } catch (e) {
                    assert(e.message.match('is locked'))
                    done()
                }
            })
        })

        describe('isEnabled', () => {
            it('must return correct value', () => {
                const web3 = new StreamrWeb3()
                assert(!web3.isEnabled())
                web3.setProvider(new FakeProvider())
                assert(web3.isEnabled())
            })
        })
    })
    describe('getWeb3', () => {
        beforeEach(() => {
            global.web3 = undefined
            global.ethereum = undefined
        })
        it('must return the web3 object without a provider when metamask does not provide it', () => {
            const web3 = getWeb3()
            expect(web3.currentProvider).toEqual(null)
        })
        it('must return the web3 object with the window.web3.currentProvider provider if it is available/defined', () => {
            // 'legacy' metamask web3 injection scenario
            global.web3 = Web3
            global.web3.currentProvider = new StreamrWeb3.providers.HttpProvider('http://boop:1337')
            const web3 = getWeb3()
            assert.equal(web3.currentProvider.host, 'http://boop:1337')
        })
        it('must return the web3 object with the window.ethereum provider if it is available/defined', () => {
            // permissioned metamask provider injection scenario
            global.ethereum = new StreamrWeb3.providers.HttpProvider('http://vitalik:300')
            const web3 = getWeb3()
            assert.equal(web3.currentProvider.host, 'http://vitalik:300')
        })
    })
})
