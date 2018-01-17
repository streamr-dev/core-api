
import React from 'react'
import {shallow, mount} from 'enzyme'
import assert from 'assert-diff'

import ShareDialogFooter from '../../../../components/ShareDialog/ShareDialogFooter'

describe('ShareDialogFooter', () => {
    describe('render', () => {
        it('is Modal.Footer', () => {
            const footer = shallow(
                <ShareDialogFooter save={() => {}} closeModal={() => {}} />
            )
            assert(footer.is('ModalFooter'))
        })
        it('renders buttons as children', () => {
            const footer = shallow(
                <ShareDialogFooter save={() => {}} closeModal={() => {}} />
            )
            assert.equal(footer.children().length, 2)
            assert(footer.childAt(0).is('Button'))
            assert(footer.childAt(1).is('Button'))
        })
        it('contains save button', () => {
            const footer = mount(
                <ShareDialogFooter save={() => {}} closeModal={() => {}} />
            )
            const saveButton = footer.find('.btn[children="Save"]')
            assert(saveButton.props().onClick === footer.props().save)
            assert(saveButton.props().className.indexOf('primary') >= 0)
        })
        it('contains cancel button', () => {
            const footer = mount(
                <ShareDialogFooter save={() => {}} closeModal={() => {}} />
            )
            const cancelButton = footer.find('.btn[children="Cancel"]')
            assert(cancelButton.props().onClick === footer.props().closeModal)
            assert(cancelButton.props().className.indexOf('default') >= 0)
        })
    })
})
