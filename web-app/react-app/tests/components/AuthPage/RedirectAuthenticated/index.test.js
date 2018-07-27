import React from 'react'
import { mount as enzymeMount } from 'enzyme'
import sinon from 'sinon'
import axios from 'axios'

import { UnwrappedRedirectAuthenticated as RedirectAuthenticated } from '../../../../components/AuthPage/shared/RedirectAuthenticated'

const mount = (blindly, search) => enzymeMount(
    <RedirectAuthenticated
        blindly={blindly}
        location={{
            search,
        }}
    />
).instance()

describe(RedirectAuthenticated.name, () => {
    const sandbox = sinon.createSandbox()

    beforeEach(() => {
        window.Streamr = {
            createLink: ({ uri }) => uri,
        }
        sandbox.stub(window.location, 'assign').callsFake(() => undefined)
        sandbox.spy(window.Streamr, 'createLink')
    })

    afterEach(() => {
        sandbox.restore()
        delete window.Streamr
    })

    describe('#redirect', () => {
        beforeEach(() => {
            sandbox.stub(RedirectAuthenticated.prototype, 'componentDidMount').callsFake(() => {})
        })

        it('redirects if authenticated', async () => {
            sandbox.stub(RedirectAuthenticated.prototype, 'getIsAuthenticated').callsFake(() => Promise.resolve(true))
            await mount(false, '').redirect()
            expect(window.location.assign.calledOnceWith('/canvas/editor')).toBe(true)
        })

        it('redirects to given location', async () => {
            sandbox.stub(RedirectAuthenticated.prototype, 'getIsAuthenticated').callsFake(() => Promise.resolve(true))
            await mount(false, '?redirect=givenLocation').redirect()
            expect(window.location.assign.calledOnceWith('givenLocation')).toBe(true)
        })

        it('does not redirect if not authenticated', async () => {
            sandbox.stub(RedirectAuthenticated.prototype, 'getIsAuthenticated').callsFake(() => Promise.resolve(false))
            await mount(false, '').redirect()
            expect(window.location.assign.called).toBe(false)
        })

        describe('skipping api calls', () => {
            beforeEach(() => {
                sandbox.stub(RedirectAuthenticated.prototype, 'getIsAuthenticated').callsFake(() => Promise.resolve(false))
            })

            it('skips when initial flag is set and ignoreSession GET param is set', () => {
                const instance = mount(false, '?ignoreSession=1')
                instance.redirect(true)
                expect(instance.getIsAuthenticated.called).toBe(false)
            })

            it('does not skip if initial flag is not set (ignoreSession set)', async () => {
                const instance = mount(false, '?ignoreSession=1')
                await instance.redirect(false)
                expect(instance.getIsAuthenticated.calledOnce).toBe(true)
            })

            it('does not skip if ignoreSession GET param is not set (initial set)', async () => {
                const instance = mount(false, '')
                await instance.redirect(true)
                expect(instance.getIsAuthenticated.calledOnce).toBe(true)
            })

            it('does not skip if ignoreSession GET param is set to false (initial set)', async () => {
                const instance = mount(false, '?ignoreSession=false')
                await instance.redirect(true)
                expect(instance.getIsAuthenticated.calledOnce).toBe(true)
            })
        })
    })

    describe('#getIsAuthenticated', () => {
        beforeEach(() => {
            sandbox.stub(RedirectAuthenticated.prototype, 'redirect').callsFake(() => undefined)
        })

        it('resolves to true when blindly is set', async () => {
            const authenticated = await mount(true, '').getIsAuthenticated()
            expect(authenticated).toBe(true)
        })

        it('does not touch the api when blindly is set', async () => {
            sandbox.spy(axios, 'get')
            await mount(true, '').getIsAuthenticated()
            expect(axios.get.called).toBe(false)
        })

        it('sends an api request to check the authentication state', async () => {
            sandbox.stub(axios, 'get').callsFake(() => Promise.resolve())
            await mount(false, '').getIsAuthenticated()
            expect(axios.get.calledOnceWith('/api/v1/users/me')).toBe(true)
        })

        it('resolves to true on a successful api response', async () => {
            sandbox.stub(axios, 'get').callsFake(() => Promise.resolve('anything'))
            await expect(mount(false, '').getIsAuthenticated()).resolves.toBe(true)
        })

        it('resolves to false on an unsuccessful api response', async () => {
            sandbox.stub(axios, 'get').callsFake(() => Promise.reject('anything'))
            await expect(mount(false, '').getIsAuthenticated()).resolves.toBe(false)
        })
    })

    it('calls redirect on mount', () => {
        sandbox.stub(RedirectAuthenticated.prototype, 'redirect').callsFake(() => {})
        const instance = mount(false, '')
        expect(instance.redirect.calledOnceWithExactly(true)).toBe(true)
    })

    it('calls redirect on update when blindly changes', (done) => {
        sandbox.stub(RedirectAuthenticated.prototype, 'componentDidMount').callsFake(() => {})
        sandbox.stub(RedirectAuthenticated.prototype, 'redirect').callsFake(() => {})
        const el = enzymeMount(
            <RedirectAuthenticated
                blindly={false}
            />
        )
        const instance = el.instance()
        el.setProps({
            blindly: true,
        }, () => {
            expect(instance.redirect.calledOnceWithExactly()).toBe(true)
            RedirectAuthenticated.prototype.redirect.resetHistory()
            el.setProps({
                blindly: false,
            }, () => {
                expect(instance.redirect.calledOnceWithExactly()).toBe(true)
                RedirectAuthenticated.prototype.redirect.resetHistory()
                el.setProps({
                    blindly: false, // no change!
                }, () => {
                    expect(instance.redirect.called).toBe(false)
                    done()
                })
            })
        })
    })
})
