
import React from 'react'
import {shallow, mount} from 'enzyme'
import assert from 'assert-diff'

import ShareDialogHeader from '../../../../components/ShareDialog/ShareDialogHeader'

describe('ShareDialogHeader', () => {
    let header
    describe('render', () => {
        it('is Modal.Header', () => {
            header = shallow(
                <ShareDialogHeader resourceTitle=""/>
            )
            assert(header.is('ModalHeader'))
        })
        it('contains Modal.Title', () => {
            header = shallow(
                <ShareDialogHeader resourceTitle=""/>
            )
            assert.equal(header.children().length, 1)
            assert(header.childAt(0).is('ModalTitle'))
        })
        it('renders with correct text', () => {
            header = mount(
                <ShareDialogHeader resourceTitle="test"/>
            )
            assert.equal(header.find('ModalTitle').text(), 'Share test')
        })
    })
})
