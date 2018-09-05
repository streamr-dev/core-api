import React from 'react'
import { mount, shallow } from 'enzyme'
import sinon from 'sinon'
import { MemoryRouter as Router } from 'react-router-dom'

import withAuthFlow from '../../../components/AuthPage/shared/withAuthFlow'
import RedirectAuthenticated from '../../../components/AuthPage/shared/RedirectAuthenticated'
import createLink from '../../../utils/createLink'

const Unwrapped = () => null

const Wrapped = withAuthFlow(Unwrapped, 0, {
    field0: '',
    field1: '',
})

describe('withAuthFlow', () => {
    const sandbox = sinon.createSandbox()

    afterEach(() => {
        sandbox.restore()
    })

    it('sets its default state', () => {
        expect(shallow(<Wrapped />).state()).toMatchObject({
            step: 0,
            isProcessing: false,
            form: {
                field0: '',
                field1: '',
            },
            errors: {},
            complete: false,
        })
    })

    describe('methods', () => {
        const el = shallow(<Wrapped />)

        describe('setFieldError', () => {
            it('sets an error', (done) => {
                el.instance().setFieldError('field0', 'Error', () => {
                    expect(el.state('errors')).toMatchObject({
                        field0: 'Error',
                    })
                    done()
                })
            })

            it('unsets redundant fields', (done) => {
                el.instance().setFieldError('field0', '', () => {
                    expect(el.state('errors')).toMatchObject({})
                    done()
                })
            })
        })

        describe('setFormField', () => {
            it('sets field\'s value', (done) => {
                el.instance().setFormField('field0', 'value', () => {
                    expect(el.state('form')).toMatchObject({
                        field0: 'value',
                        field1: '',
                    })
                    done()
                })
            })

            it('resets field\'s error', (done) => {
                el.instance().setFormField('field0', 'value', () => {
                    expect(el.state('errors')).toMatchObject({})
                    done()
                })
            })
        })

        describe('setIsProcessing', () => {
            it('sets given value', (done) => {
                const instance = el.instance()

                instance.setIsProcessing(true, () => {
                    expect(el.state('isProcessing')).toBe(true)
                    instance.setIsProcessing(false, () => {
                        expect(el.state('isProcessing')).toBe(false)
                        done()
                    })
                })
            })
        })

        describe('setStep', () => {
            it('sets given value', (done) => {
                const instance = el.instance()

                instance.setStep(1, () => {
                    expect(el.state('step')).toBe(1)
                    instance.setStep(2, () => {
                        expect(el.state('step')).toBe(2)
                        done()
                    })
                })
            })
        })

        describe('prev', () => {
            it('sets previous step (current - 1)', (done) => {
                el.setState({
                    step: 1,
                }, () => {
                    expect(el.state('step')).toBe(1)
                    el.instance().prev(() => {
                        expect(el.state('step')).toBe(0)
                        done()
                    })
                })
            })

            it('does not set a negative step value', (done) => {
                el.setState({
                    step: 0,
                }, () => {
                    expect(el.state('step')).toBe(0)
                    el.instance().prev(() => {
                        expect(el.state('step')).toBe(0)
                        done()
                    })
                })
            })
        })

        describe('next', () => {
            it('sets next step (current + 1)', (done) => {
                el.setState({
                    step: 0,
                }, () => {
                    expect(el.state('step')).toBe(0)
                    el.instance().next(() => {
                        expect(el.state('step')).toBe(1)
                        done()
                    })
                })
            })
        })

        describe('markAsComplete', () => {
            it('sets complete to true', (done) => {
                el.instance().markAsComplete(() => {
                    expect(el.state('complete')).toBe(true)
                    done()
                })
            })
        })
    })

    describe('passing props to the wrapped component', () => {
        it('passes props', () => {
            const el = shallow(<Wrapped />)
            const { step, isProcessing, errors, form } = el.state()
            const instance = el.instance()
            const { next, prev, setFieldError, setFormField, setIsProcessing, markAsComplete: redirect } = instance

            expect(el.find(Unwrapped).props()).toMatchObject({
                next,
                prev,
                setFormField,
                setFieldError,
                setIsProcessing,
                step,
                isProcessing,
                errors,
                form,
                redirect,
            })
        })
    })

    describe('passing props to RedirectAuthenticated', () => {
        beforeEach(() => {
            window.Streamr = {
                createLink: ({ uri }) => uri,
            }
        })

        afterEach(() => {
            delete window.Streamr
        })

        it('passes props', () => {
            expect(
                mount(<Router><Wrapped /></Router>)
                    .find(RedirectAuthenticated)
                    .props()
            ).toMatchObject({
                blindly: false,
            })
        })
    })
})
