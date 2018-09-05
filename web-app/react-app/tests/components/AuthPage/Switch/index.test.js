import React from 'react'
import { mount } from 'enzyme'

import Switch from '../../../../components/AuthPage/shared/Switch'

describe(Switch.name, () => {
    const el = mount(
        <Switch>
            <span>#1</span>
            <span>#2</span>
        </Switch>
    )

    it('renders single child', () => {
        expect(el.text()).toBe('#1')
    })

    it('switches to another element', (done) => {
        el.setProps({
            current: 1,
        }, () => {
            setTimeout(() => {
                expect(el.text()).toBe('#2')
                done()
            }, 500)
        })
    })
})
