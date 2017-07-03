
import React from 'react'
import {shallow, mount} from 'enzyme'
import assert from 'assert-diff'
import sinon from 'sinon'

import {ShareDialog} from '../../../components/ShareDialog'

describe('ShareDialog', () => {

    describe('save', () => {
        it('should call props.save and the closeModal', () => {
            const propSave = sinon.spy()
            const dialog = shallow(
                <ShareDialog resourceId="testId" resourceType="testType" resourceTitle="testTitle" save={() => new Promise(resolve => {
                    propSave()
                    resolve()
                })}>
                    <div>test</div>
                </ShareDialog>
            )
            const instance = dialog.instance()
            instance.closeModal = sinon.stub()
            
            instance.save()
            
            assert(propSave.called)
            process.nextTick(() => {
                assert(instance.closeModal.called)
            })
        })
    })
    
    describe('openModal', () => {
        it('sets state.open to true', () => {
            const dialog = shallow(
                <ShareDialog resourceId="testId" resourceType="testType" resourceTitle="testTitle">
                    <div>test</div>
                </ShareDialog>
            )
            assert(!dialog.state().open)
            dialog.instance().openModal()
            assert(dialog.state().open)
        })
    })
    
    describe('closeModal', () => {
        it('sets state.open to true', () => {
            const dialog = shallow(
                <ShareDialog resourceId="testId" resourceType="testType" resourceTitle="testTitle">
                    <div>test</div>
                </ShareDialog>
            )
            const instance = dialog.instance()
            instance.setState({
                open: true
            })
            dialog.instance().closeModal()
            assert(!dialog.state().open)
        })
    })
    
    describe('render', () => {
    
        describe('initial rendering', () => {
            beforeEach(() => {
                jest.mock('../../../components/ShareDialog/ShareDialogContent', () => () => null)
            })
            afterEach(() => {
                jest.unmock('../../../components/ShareDialog/ShareDialogContent')
            })
            it('should not render with more or less than one children', () => {
                assert.throws(() => {
                    mount(
                        <ShareDialog resourceId="" resourceType="" resourceTitle="" save={() => {}}>
                            <div/>
                            <div/>
                        </ShareDialog>
                    )
                })
                assert.throws(() => {
                    mount(
                        <ShareDialog resourceId="" resourceType="" resourceTitle="" save={() => {}} />
                    )
                })
            })
            it('should set the initial state correctly', () => {
                const dialog = shallow(
                    <ShareDialog resourceId="" resourceType="" resourceTitle="" save={() => {}}>
                        <div/>
                    </ShareDialog>
                )
                assert.deepEqual(dialog.state(), {
                    open: false
                })
            })
            it('should render a Modal inside the child element', () => {
                const dialog = shallow(
                    <ShareDialog resourceId="" resourceType="" resourceTitle="" save={() => {}}>
                        <div>test</div>
                    </ShareDialog>
                )
                assert(dialog.is('div'))
                assert.equal(dialog.find('Modal').length, 1)
            })
            it('should render the child as the root', () => {
                const dialog = shallow(
                    <ShareDialog resourceId="" resourceType="" resourceTitle="" save={() => {}}>
                        <div>test</div>
                    </ShareDialog>
                )
                assert.equal(dialog.root.childAt(0).text(), 'test')
            })
        })
        
        describe('opening the modal', () => {
            it('should open the modal when clicked the root child', () => {
                const dialog = shallow(
                    <ShareDialog resourceId="" resourceType="" resourceTitle="" save="">
                        <button>test</button>
                    </ShareDialog>
                )
                assert(!dialog.find('Modal').node.props.show)
                dialog.simulate('click')
                assert(dialog.find('Modal').node.props.show)
            })
        })
        
        describe('children of the modal', () => {
            let dialog
            let modal
            beforeEach(() => {
                dialog = shallow(
                    <ShareDialog resourceId="testId" resourceType="testType" resourceTitle="testTitle" save={() => new Promise(() => {})}>
                        <div>test</div>
                    </ShareDialog>
                )
                modal = dialog.find('Modal')
            })
            it('should have the header', () => {
                const header = modal.childAt(0)
                assert(header.is('ShareDialogHeader'))
                assert.deepStrictEqual(header.props(), {
                    resourceTitle: 'testTitle'
                })
            })
            it('should have the content', () => {
                const content = modal.childAt(1)
                assert(content.is('Connect(ShareDialogContent)'))
                assert.deepStrictEqual(content.props(), {
                    resourceId: 'testId',
                    resourceTitle: 'testTitle',
                    resourceType: 'testType'
                })
            })
            it('should have the footer', () => {
                const footer = modal.childAt(2)
                assert(footer.is('ShareDialogFooter'))
            })
        })
    })
    
})
