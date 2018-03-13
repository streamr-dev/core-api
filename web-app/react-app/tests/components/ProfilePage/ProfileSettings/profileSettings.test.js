import React from 'react'
import {shallow} from 'enzyme'
import assert from 'assert-diff'
import sinon from 'sinon'
import moment from 'moment-timezone'

const momentStub = sinon.stub(moment.tz, 'names')
    .callsFake(() => ['a', 'b', 'c'])

import * as userActions from '../../../../actions/user'
import * as helpers from '../../../../helpers/createLink'

import {ProfileSettings, mapStateToProps, mapDispatchToProps} from '../../../../components/ProfilePage/ProfileSettings'

sinon.stub(helpers, 'default')
    .callsFake((uri) => uri)

describe('ProfileSettings', () => {
    let sandbox

    beforeEach(() => {
        sandbox = sinon.sandbox.create()
    })

    afterEach(() => {
        sandbox.restore()
    })

    describe('componentDidMount', () => {
        it('must call props.getCurrentUser', () => {
            const spy = sinon.spy()
            shallow(<ProfileSettings
                user={{}}
                getCurrentUser={spy}
                updateCurrentUserName={() => {}}
                updateCurrentUserTimezone={() => {}}
                saveCurrentUser={() => {}}
            />)
            assert(spy.calledOnce)
        })
    })

    describe('onNameChange', () => {
        it('must call props.updateCurrentUserName', () => {
            const spy = sinon.spy()
            const el = shallow(<ProfileSettings
                user={{}}
                getCurrentUser={() => {}}
                updateCurrentUserName={spy}
                updateCurrentUserTimezone={() => {}}
                saveCurrentUser={() => {}}
            />)
            el.instance().onNameChange({
                target: {
                    value: 'testtest'
                }
            })
            assert(spy.calledOnce)
            assert(spy.calledWith('testtest'))
        })
    })

    describe('onTimezoneChange', () => {
        it('must call props.updateCurrentUserTimezone', () => {
            const spy = sinon.spy()
            const el = shallow(<ProfileSettings
                user={{}}
                getCurrentUser={() => {}}
                updateCurrentUserName={() => {}}
                updateCurrentUserTimezone={spy}
                saveCurrentUser={() => {}}
            />)
            el.instance().onTimezoneChange({
                value: 'testtest'
            })
            assert(spy.calledOnce)
            assert(spy.calledWith('testtest'))
        })
    })

    describe('onSubmit', () => {
        it('must call e.preventDefault', () => {
            const spy = sinon.spy()
            const el = shallow(<ProfileSettings
                user={{}}
                getCurrentUser={() => {}}
                updateCurrentUserName={() => {}}
                updateCurrentUserTimezone={() => {}}
                saveCurrentUser={() => {}}
            />)
            el.instance().onSubmit({
                preventDefault: spy,
                target: {}
            })
            assert(spy.calledOnce)
        })
        it('must call props.saveCurrentUser with the value', () => {
            const spy = sinon.spy()
            const user = {
                moi: 'hei'
            }
            const el = shallow(<ProfileSettings
                user={user}
                getCurrentUser={() => {}}
                updateCurrentUserName={() => {}}
                updateCurrentUserTimezone={() => {}}
                saveCurrentUser={spy}
            />)
            el.instance().onSubmit({
                preventDefault: () => {},
                target: {}
            })
            assert(spy.calledOnce)
            assert(spy.calledWith(user))
        })
    })

    describe('render', () => {
        it('must be a Panel with correct header', () => {
            const el = shallow(<ProfileSettings
                user={{}}
                getCurrentUser={() => {}}
                updateCurrentUserName={() => {}}
                updateCurrentUserTimezone={() => {}}
                saveCurrentUser={() => {}}
            />)
            assert(el.is('Panel'))
            assert.equal(el.props().header, 'Profile Settings')
        })
        it('must have a Form with correct onSubmit as a child', () => {
            const el = shallow(<ProfileSettings
                user={{}}
                getCurrentUser={() => {}}
                updateCurrentUserName={() => {}}
                updateCurrentUserTimezone={() => {}}
                saveCurrentUser={() => {}}
            />)
            const form = el.childAt(0)
            assert(form.is('Form'))
            assert.equal(form.props().onSubmit, el.instance().onSubmit)
        })

        describe('inputs in form', () => {
            let el
            let form
            beforeEach(() => {
                el = shallow(<ProfileSettings
                    user={{
                        name: 'testName',
                        username: 'testUsername',
                        timezone: 'testTimezone'
                    }}
                    getCurrentUser={() => {}}
                    updateCurrentUserName={() => {}}
                    updateCurrentUserTimezone={() => {}}
                    saveCurrentUser={() => {}}
                />)
                form = el.childAt(0)
                form.children().forEach((c) => assert(c.is('FormGroup')))
            })
            it('must have an email field', () => {
                const formGroup = form.childAt(0)

                const cl = formGroup.find('ControlLabel')
                assert.equal(cl.childAt(0).text(), 'Email')

                const div = formGroup.find('div')
                assert.equal(div.childAt(0).text(), 'testUsername')
            })
            it('must have a password field', () => {
                const formGroup = form.childAt(1)

                const cl = formGroup.find('ControlLabel')
                assert.equal(cl.childAt(0).text(), 'Password')

                const div = formGroup.find('div')
                const a = div.childAt(0)
                assert.equal(a.childAt(0).text(), 'Change Password')
                assert.equal(a.props().href, 'profile/changePwd')
            })
            it('must have a name field', () => {
                const formGroup = form.childAt(2)

                const cl = formGroup.find('ControlLabel')
                assert.equal(cl.childAt(0).text(), 'Full Name')

                const fc = formGroup.find('FormControl')
                assert.equal(fc.props().name, 'name')
                assert.equal(fc.props().value, 'testName')
                assert.equal(fc.props().onChange, el.instance().onNameChange)
                assert(fc.props().required)
            })
            it('must have a timezone selector', () => {

                const formGroup = form.childAt(3)

                const cl = formGroup.find('ControlLabel')
                assert.equal(cl.childAt(0).text(), 'Timezone')

                const select = formGroup.find('Select')
                assert.equal(select.props().placeholder, 'Select timezone')
                assert.deepStrictEqual(select.props().options, [{
                    value: 'a',
                    label: 'a'
                }, {
                    value: 'b',
                    label: 'b'
                }, {
                    value: 'c',
                    label: 'c'
                }])
                assert.equal(select.props().value, 'testTimezone')
                assert.equal(select.props().name, 'timezone')
                assert.equal(select.props().onChange, el.instance().onTimezoneChange)
                assert(select.props().required)
                assert(!select.props().clearable)
            })
            it('must have a submit button', () => {
                const formGroup = form.childAt(4)

                const inputGroup = formGroup.find('InputGroup')
                const button = inputGroup.find('Button')

                assert.equal(button.childAt(0).text(), 'Save')
                assert.equal(button.props().type, 'submit')
                assert.equal(button.props().name, 'submit')
                assert.equal(button.props().bsStyle, 'primary')
                assert.equal(button.props().bsSize, 'lg')
            })
        })
    })

    describe('mapStateToProps', () => {
        it('must return right kind of object', () => {
            const user = {
                moi: 'moimoi'
            }
            assert.deepStrictEqual(mapStateToProps({
                user: {
                    currentUser: user
                }
            }), {
                user
            })
        })
    })

    describe('mapDispatchToProps', () => {
        it('must return right kind of object with right type of attrs', () => {
            assert.equal(typeof mapDispatchToProps(), 'object')
            assert.equal(typeof mapDispatchToProps().getCurrentUser, 'function')
            assert.equal(typeof mapDispatchToProps().updateCurrentUserName, 'function')
            assert.equal(typeof mapDispatchToProps().updateCurrentUserTimezone, 'function')
            assert.equal(typeof mapDispatchToProps().saveCurrentUser, 'function')
        })

        describe('getCurrentUser', () => {
            it('must dispatch getCurrentUser', () => {
                const dispatchSpy = sinon.spy()
                const deleteStub = sandbox.spy(userActions, 'getCurrentUser')
                mapDispatchToProps(dispatchSpy).getCurrentUser()
                assert(dispatchSpy.calledOnce)
                assert(deleteStub.calledOnce)
            })
        })
    })
})
