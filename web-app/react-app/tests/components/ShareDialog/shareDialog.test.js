
import React from 'react'
import {shallow, mount} from 'enzyme'
import assert from 'assert-diff'
import sinon from 'sinon'

import {ShareDialog} from '../../../components/ShareDialog'

describe('ShareDialog', () => {

    describe('save', () => {
        it('should call props.save and the props.onClose', () => {
            const propSave = sinon.spy()
            const propClose = sinon.spy()
            const dialog = shallow(
                <ShareDialog
                    resourceId="testId"
                    resourceType="testType"
                    resourceTitle="testTitle"
                    save={() => new Promise(resolve => {
                        propSave()
                        resolve()
                    })}
                    onClose={() => new Promise(resolve => {
                        propClose()
                        resolve()
                    })}
                >
                    <div>test</div>
                </ShareDialog>
            )
            const instance = dialog.instance()
            
            instance.save()
            
            assert(propSave.called)
            process.nextTick(() => {
                assert(propClose.called)
            })
        })
    })
    
    describe('render', () => {
    
        describe('initial rendering', () => {
            it('should render correct children with correct props', () => {
                const onClose = () => {}
                let isOpen = true
                const dialog = shallow(
                    <ShareDialog onClose={onClose} resourceId="resourceId" resourceType="resourceType" resourceTitle="resourceTitle" isOpen={true}/>
                )
                
                assert.equal(dialog.props().show, true)
    
                const dialog2 = shallow(
                    <ShareDialog onClose={onClose} resourceId="resourceId" resourceType="resourceType" resourceTitle="resourceTitle" isOpen={false}/>
                )
                assert.equal(dialog2.props().show, false)
                
                const header = dialog.childAt(0)
                const content = dialog.childAt(1)
                const footer = dialog.childAt(2)
                
                assert.equal(header.props().resourceTitle, 'resourceTitle')
                
                assert.deepStrictEqual(content.props().resourceTitle, 'resourceTitle')
                assert.deepStrictEqual(content.props().resourceType, 'resourceType')
                assert.deepStrictEqual(content.props().resourceId, 'resourceId')
                
                assert.deepStrictEqual(footer.props().save, dialog.instance().save)
                assert.deepStrictEqual(footer.props().closeModal, onClose)
            })
        })
    })
    
})
