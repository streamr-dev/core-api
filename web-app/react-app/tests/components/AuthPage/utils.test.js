import axios from 'axios'
import sinon from 'sinon'

import * as utils from '../../../components/AuthPage/shared/utils'

describe('utils', () => {
    describe('onInputChange', () => {
        it('calls the callback with name and value', () => {
            utils.onInputChange((name, value) => {
                expect(name).toBe('myName')
                expect(value).toBe('myValue')
            })({
                target: {
                    name: 'myName',
                    value: 'myValue',
                },
            })
        })

        it('calls the callback with name and a boolean if the target type is checkbox', () => {
            utils.onInputChange((name, value) => {
                expect(name).toBe('myName')
                expect(value).toBe(true)
            })({
                target: {
                    type: 'checkbox',
                    name: 'myName',
                    checked: true,
                },
            })
        })

        it('lets you override the name', () => {
            utils.onInputChange((name) => {
                expect(name).toBe('myCustomName')
            }, 'myCustomName')({
                target: {
                    name: 'myName',
                },
            })
        })
    })

    describe('post', () => {
        const sandbox = sinon.createSandbox()

        beforeEach(() => {
            sandbox.restore()
        })

        it('posts with given params', async () => {
            sandbox.stub(axios, 'post').callsFake(() => Promise.resolve({}))

            await utils.post('url', {
                param: 'value',
            }, false, false)

            expect(axios.post.calledOnceWithExactly('url', 'param=value', {
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
            }))
        })

        it('posts via xhr if asked to', async () => {
            sandbox.stub(axios, 'post').callsFake(() => Promise.resolve({}))

            await utils.post('url', {
                param: 'value',
            }, false, true)

            expect(axios.post.calledOnceWithExactly('url', 'param=value', {
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'X-Requested-With': 'XMLHttpRequest',
                },
            }))
        })

        it('resolves (to nothing) on success', async () => {
            sandbox.stub(axios, 'post').callsFake(() => Promise.resolve({}))

            await expect(utils.post('url', {
                param: 'value',
            }, false, true)).resolves.toBe(undefined)
        })

        it('rejectes and raises an error on failure', async () => {
            sandbox.stub(axios, 'post').callsFake(() => Promise.reject({
                response: {
                    data: {
                        error: 'Oh, it errored.',
                    },
                },
            }))

            await expect(utils.post('url', {
                param: 'value',
            }, false, false)).rejects.toThrow('Oh, it errored.')
        })

        describe('2XX failures', () => {
            it('raises errors attached to a successful response', async () => {
                const error = 'My error message.'

                sandbox.stub(axios, 'post').callsFake(() => Promise.resolve({
                    data: {
                        error,
                    },
                }))

                expect.assertions(1)

                try {
                    await utils.post('url', {
                        param: 'value',
                    }, true, false)
                } catch (e) {
                    expect(e.message).toBe(error)
                }
            })
        })
    })

    describe('getDisplayName', () => {
        it('defaults to "Component"', () => {
            expect(utils.getDisplayName({
                name: null,
                displayName: null,
            })).toBe('Component')
        })

        it('gives name if available', () => {
            expect(utils.getDisplayName({
                name: 'Name',
                displayName: null,
            })).toBe('Name')
        })

        it('gives displayName if available', () => {
            expect(utils.getDisplayName({
                name: null,
                displayName: 'DisplayName',
            })).toBe('DisplayName')
        })
    })
})
