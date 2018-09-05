import React from 'react'
import { shallow } from 'enzyme'

import Actions from '../../../../components/AuthPage/shared/Actions'

describe(Actions.name, () => {
    describe('with no children', () => {
        it('is empty', () => {
            expect(shallow(<Actions />).children().length).toBe(0)
        })
    })

    describe('with 1 child', () => {
        const root = shallow(
            <Actions>
                <span>Anything</span>
            </Actions>
        )

        it('gets prepended with an extra span child', () => {
            expect(root.children().length).toBe(2)
            expect(root.childAt(0).matchesElement(<span />)).toBe(true)
            expect(root.childAt(1).text()).toBe('Anything')
        })
    })

    describe('with multiple children', () => {
        const root = shallow(
            <Actions>
                <span>Anything #0</span>
                <span>Anything #1</span>
                <span>Anything #2</span>
            </Actions>
        )

        it('renders children', () => {
            expect(root.children().length).toBe(3)

            root.children().forEach((child, index) => {
                expect(child.text()).toBe(`Anything #${index}`)
            })
        })
    })
})
