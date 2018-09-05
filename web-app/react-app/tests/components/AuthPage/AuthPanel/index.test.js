import React from 'react'
import { shallow } from 'enzyme'

import AuthPanel from '../../../../components/AuthPage/shared/AuthPanel'
import AuthStep from '../../../../components/AuthPage/shared/AuthStep'
import AuthPanelNav from '../../../../components/AuthPage/shared/AuthPanelNav'
import { noop } from '../../../../components/AuthPage/shared/utils'
import * as yup from 'yup'

describe(AuthPanel.name, () => {
    const onPrev = () => {}
    const validationSchema = yup.object()
    const currentStep = 0
    const onValidationError = () => {}
    const setIsProcessing = () => {}
    const onNext = () => {}
    const form = {}
    const isProcessing = false

    const el = shallow(
        <AuthPanel
            currentStep={currentStep}
            validationSchemas={[
                validationSchema,
            ]}
            onPrev={onPrev}
            onValidationError={onValidationError}
            setIsProcessing={setIsProcessing}
            onNext={onNext}
            form={form}
            isProcessing={isProcessing}
        >
            <AuthStep
                showSignin
                showSignup
                showEth
                showBack
                title="Step #0"
            >#0</AuthStep>
            <AuthStep
                title="Step #1"
            >#1</AuthStep>
        </AuthPanel>
    )

    it('renders children', () => {
        expect(el.children().length).toBeGreaterThan(0)
    })

    describe('navigation', () => {
        it('renders a nav for each step', () => {
            expect(el.find(AuthPanelNav).length).toBe(el.children().length)
        })

        describe('passing default step\'s props to nav', () => {
            const step = el.find(AuthStep).at(1)
            const nav = el.find(AuthPanelNav).at(1)

            it('sets nav#signin if step#showSignin is set', () => {
                expect(step.prop('showSignin')).not.toBeDefined()
                expect(nav.prop('signin')).toBe(false)
            })

            it('sets nav#signup if step#showSignup is set', () => {
                expect(step.prop('showSignup')).not.toBeDefined()
                expect(nav.prop('signup')).toBe(false)
            })

            it('passes a noop to nav#onUseEth if step#showEth is set', () => {
                expect(step.prop('showEth')).not.toBeDefined()
                expect(nav.prop('onUseEth')).toBeNull()
            })

            it('passes step\'s onPrev prop as to nav#onGoBack if step#showBack is set', () => {
                expect(step.prop('showBack')).not.toBeDefined()
                expect(nav.prop('onGoBack')).toBeNull()
            })
        })

        describe('passing custom step\'s props to nav', () => {
            const step = el.find(AuthStep).at(0)
            const nav = el.find(AuthPanelNav).at(0)

            it('sets nav#signin if step#showSignin is set', () => {
                expect(step.prop('showSignin')).toBe(true)
                expect(nav.prop('signin')).toBe(true)
            })

            it('sets nav#signup if step#showSignup is set', () => {
                expect(step.prop('showSignup')).toBe(true)
                expect(nav.prop('signup')).toBe(true)
            })

            it('passes a noop to nav#onUseEth if step#showEth is set', () => {
                expect(step.prop('showEth')).toBe(true)
                expect(nav.prop('onUseEth')).toBe(noop)
            })

            it('passes step\'s onPrev prop as to nav#onGoBack if step#showBack is set', () => {
                expect(step.prop('showBack')).toBe(true)
                expect(nav.prop('onGoBack')).toBe(onPrev)
            })
        })
    })

    describe('titles', () => {
        const titles = el.find(AuthPanel.Title)
        const steps = el.find(AuthStep)

        it('renders a title element for each step', () => {
            expect(titles.length).toBe(steps.length)
        })

        describe('mapping', () => {
            steps.forEach((step, index) => {
                const title = step.prop('title')

                it(`step ${index} gives the panel title "${title}"`, () => {
                    expect(titles.at(index).children()
                        .text()
                    ).toBe(title)
                })
            })
        })
    })

    describe('passing props to steps', () => {
        const step = el.find(AuthStep).first()

        it('passes current validation schema', () => {
            expect(step.prop('validationSchema')).toBe(validationSchema)
        })

        it('passes step number', () => {
            expect(step.prop('step')).toBe(0)
        })

        it('passes how many steps there are (totalSteps)', () => {
            expect(step.prop('totalSteps')).toBe(2)
        })

        it('passes onValidationError callback', () => {
            expect(step.prop('onValidationError')).toBe(onValidationError)
        })

        it('passes setIsProcessing setter', () => {
            expect(step.prop('setIsProcessing')).toBe(setIsProcessing)
        })

        it('passes isProcessing flag', () => {
            expect(step.prop('isProcessing')).toBe(isProcessing)
        })

        it('passes onNext (next) callback', () => {
            expect(step.prop('next')).toBe(onNext)
        })

        it('passes the form', () => {
            expect(step.prop('form')).toBe(form)
        })

        it('tells a step if it is the current one', () => {
            expect(step.prop('current')).toBe(true)
        })
    })
})
