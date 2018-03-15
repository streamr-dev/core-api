import React from 'react'
import {shallow} from 'enzyme'
import assert from 'assert-diff'
import sinon from 'sinon'

import IntegrationKeyHandlerTableRow from '../../../../../../../components/ProfilePage/IntegrationKeyHandler/IntegrationKeyHandlerSegment/IntegrationKeyHandlerTable/IntegrationKeyHandlerTableRow'

describe('IntegrationKeyHandlerTableRow', () => {

    describe('render', () => {
        let el
        let onDeleteSpy = sinon.spy()
        beforeEach(() => {
            el = shallow(<IntegrationKeyHandlerTableRow
                fields={['a', 'b']}
                onDelete={onDeleteSpy}
                item={{
                    id: 'testId',
                    name: 'testName',
                    json: {
                        a: 'field',
                        b: 'fieldAgain'
                    }
                }}
            />)
        })
        afterEach(() => {
            onDeleteSpy.reset()
        })

        it('must be a tr', () => {
            assert(el.is('tr'))
        })
        it('must have correct amount of tds', () => {
            const tds = el.find('td')
            assert.equal(tds.length, 4)

            assert.equal(tds.at(0).text(), 'testName')

            const td = tds.at(1)
            const span = td.childAt(0)
            assert.equal(span.props().className, 'publicKey')
            assert.equal(span.text(), 'field')

            const td2 = tds.at(2)
            assert(td2.is('td'))
            const span2 = td2.childAt(0)
            assert.equal(span2.props().className, 'publicKey')
            assert.equal(span2.text(), 'fieldAgain')

            const td3 = tds.at(3)
            assert(td3.is('td'))
        })
        describe('last td', () => {
            it('must have actionButtonContainer', () => {
                el.setProps({
                    fields: [1,2,3,4,5,6,7,8,9,0]
                })
                const lastTd = el.find('td').last()
                const actionButtonContainer = lastTd.find('div.actionButtonContainer')
                assert.equal(actionButtonContainer.length, 1)
            })
            it('must have a ConfirmButton with correct props', () => {
                const lastTd = el.find('td').last()
                const formGroup = lastTd.childAt(0)
                const confirmButton = formGroup.childAt(0)
                assert(confirmButton.is('ConfirmButton'))
                assert.equal(confirmButton.props().buttonProps.bsStyle, 'danger')
                assert.equal(confirmButton.props().buttonProps.type, 'button')
                assert.equal(confirmButton.props().buttonProps.title, 'Delete key')
                assert.equal(confirmButton.props().confirmTitle, 'Are you sure?')
                assert.equal(confirmButton.props().confirmMessage, 'Are you sure you want to remove integration key testName?')
                assert.equal(confirmButton.props().className, 'deleteButton')
            })
            it('must have right kind of icon in confirmButton', () => {
                const lastTd = el.find('td').last()
                const formGroup = lastTd.childAt(0)
                const confirmButton = formGroup.childAt(0)
                const fa = confirmButton.find('FontAwesome')
                assert.equal(fa.props().name, 'trash-o')
                assert.equal(fa.props().className, 'icon')
            })
            it('must have confirmCallback that calls onDelete in confirmButton', () => {
                const spy = sinon.spy()
                el.setProps({
                    onDelete: spy
                })
                const lastTd = el.find('td').last()
                const formGroup = lastTd.childAt(0)
                const confirmButton = formGroup.childAt(0)
                confirmButton.props().confirmCallback()

                assert(spy.calledOnce)
                assert(spy.calledWith('testId'))
            })
        })
    })
})
