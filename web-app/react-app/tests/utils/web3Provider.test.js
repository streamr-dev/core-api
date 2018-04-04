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
        it('must return the same instance every time', () => {
            assert(getWeb3() === getWeb3())
        })
    })
})
