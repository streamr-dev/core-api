import React from 'react'
import { shallow } from 'enzyme'
import sinon from 'sinon'
import * as yup from 'yup'

import AuthStep from '../../../../components/AuthPage/shared/AuthStep'

describe(AuthStep.name, () => {
    describe('#validate', () => {
        const validationSchema = {
            validate: sinon.spy(),
        }
        const form = {
            key: 'value',
        }

        beforeEach(() => {
            validationSchema.validate.resetHistory()
        })

        it('gets called with a given form on a given validation schema', () => {
            const el = shallow(
                <AuthStep
                    validationSchema={validationSchema}
                    form={form}
                />
            )
            el.instance().validate()
            expect(validationSchema.validate.calledOnceWithExactly(form)).toBe(true)
        })

        it('handles defaults gently', () => {
            const el = shallow(
                <AuthStep />
            )
            const validate = sinon.stub()
            const defaultSchema = sinon.stub(yup, 'object').returns({
                validate,
            })

            el.instance().validate()
            expect(defaultSchema.calledOnce).toBe(true)
            expect(validate.calledOnceWith({})).toBe(true)
        })
    })

    describe('#submit', () => {
        describe('after successful validation', () => {
            describe('after successful submit', () => {
                const authStep = (props) => shallow(
                    <AuthStep
                        step={0}
                        totalSteps={1}
                        onSubmit={() => Promise.resolve()}
                        validationSchema={{
                            validate: () => Promise.resolve(),
                        }}
                        {...props}
                    />
                ).instance()

                it('unsets processing flag', async () => {
                    const instance = authStep({})
                    sinon.spy(instance, 'setProcessing')
                    await instance.submit()
                    expect(instance.setProcessing.calledOnceWithExactly(false, sinon.match.func)).toBe(true)
                    instance.setProcessing.restore()
                })

                it('calls onSuccess callback', async () => {
                    const onSuccess = sinon.spy()
                    const instance = authStep({
                        onSuccess,
                    })
                    await instance.submit()
                    expect(onSuccess.calledOnce).toBe(true)
                })

                it('does not call onFailure callback', async () => {
                    const onFailure = sinon.spy()
                    const instance = authStep({
                        onFailure,
                    })
                    await instance.submit()
                    expect(onFailure.called).toBe(false)
                })

                describe('#next', () => {
                    it('proceeds to the next step', async () => {
                        const next = sinon.spy()
                        const instance = authStep({
                            totalSteps: 2,
                            next,
                        })
                        await instance.submit()
                        expect(next.calledOnce).toBe(true)
                    })

                    it('does not get called when there\'s no step to proceed to', async () => {
                        const next = sinon.spy()
                        const instance = authStep({
                            next,
                        })
                        await instance.submit()
                        expect(next.calledOnce).toBe(false)
                    })
                })
            })

            describe('unsuccessful submit', () => {
                const error = new Error
                const authStep = (props) => shallow(
                    <AuthStep
                        {...props}
                        onSubmit={() => Promise.reject(error)}
                        validationSchema={{
                            validate: () => Promise.resolve(),
                        }}
                    />
                ).instance()

                it('unsets processing flag', async () => {
                    const instance = authStep({})
                    sinon.spy(instance, 'setProcessing')
                    await instance.submit()
                    expect(instance.setProcessing.calledOnceWithExactly(false)).toBe(true)
                    instance.setProcessing.restore()
                })

                it('calls given onFailure callback', async () => {
                    const onFailure = sinon.spy()
                    const instance = authStep({
                        onFailure,
                    })
                    await instance.submit()
                    expect(onFailure.calledOnceWithExactly(error))
                })

                it('does not call onSuccess callback', async () => {
                    const onSuccess = sinon.spy()
                    const instance = authStep({
                        onSuccess,
                    })
                    await instance.submit()
                    expect(onSuccess.called).toBe(false)
                })

                it('does not proceed to the next step', async () => {
                    const next = sinon.spy()
                    const instance = authStep({
                        totalSteps: 2,
                        next,
                    })
                    await instance.submit()
                    expect(next.called).toBe(false)
                })
            })
        })

        describe('unsuccessful validation', () => {
            const schema = yup.object().shape({
                field: yup.string().required('message'),
            })
            const authStep = (props) => shallow(
                <AuthStep
                    {...props}
                    validationSchema={schema}
                />
            ).instance()

            it('does not let onSubmit to get called', async () => {
                const onSubmit = sinon.spy()
                const instance = authStep({
                    onSubmit,
                })
                expect.assertions(3)
                try {
                    await instance.validate()
                } catch (e) {
                    expect(e instanceof yup.ValidationError).toBe(true)
                }
                try {
                    await instance.submit()
                } catch (e) {
                    expect(e instanceof yup.ValidationError).toBe(true)
                }
                expect(onSubmit.called).toBe(false)
            })

            it('unsets processing flag', async () => {
                const instance = authStep({})
                sinon.spy(instance, 'setProcessing')
                try {
                    await instance.submit()
                } catch (e) {}
                expect(instance.setProcessing.calledOnceWithExactly(false)).toBe(true)
                instance.setProcessing.restore()
            })

            it('calls onValidationError instead of raising an exception', async () => {
                const onValidationError = sinon.spy()
                const instance = authStep({
                    onValidationError,
                })
                await instance.submit()
                expect(onValidationError.calledOnceWithExactly('field', 'message')).toBe(true)
            })
        })
    })

    describe('#onSubmit', () => {
        const el = shallow(
            <AuthStep />
        )
        const event = {
            preventDefault: sinon.spy(),
        }

        beforeEach(() => {
            event.preventDefault.resetHistory()
        })

        it('prevents the form from a regular submit', () => {
            el.instance().onSubmit(event)
            expect(event.preventDefault.calledOnce).toBe(true)
        })

        it('starts the processing', () => {
            const instance = sinon.mock(el.instance())
            instance.expects('setProcessing').once()
                .withArgs(true)

            el.instance().onSubmit(event)

            instance.verify()
        })

        it('cancels debounced submits', () => {
            const clock = sinon.useFakeTimers()
            const instance = el.instance()
            const debounceWindow = 500

            sinon.spy(instance, 'setProcessing')

            instance.debouncedScheduleSubmit()
            instance.onSubmit(event)
            expect(instance.setProcessing.calledOnce).toBe(true)
            clock.tick(debounceWindow - 1)
            expect(instance.setProcessing.calledOnce).toBe(true)
            clock.tick(1)
            expect(instance.setProcessing.calledOnce).toBe(true)

            instance.setProcessing.restore()
            clock.restore()
        })
    })
})
