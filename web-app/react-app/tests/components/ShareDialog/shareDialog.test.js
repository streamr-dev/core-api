
import React from 'react'
import {shallow} from 'enzyme'
import assert from 'assert-diff'
import sinon from 'sinon'
import * as permissionActions from '../../../actions/permission.js'
import {ShareDialog, mapStateToProps, mapDispatchToProps} from '../../../components/ShareDialog'

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
    
    describe('mapStateToProps', () => {
        it('should return right kind of object', () => {
            const reduxState = {
                permission: {
                    byTypeAndId: {
                        correctType: {
                            correctId: [0],
                            wrongId: [1]
                        },
                        wrongType: {
                            correctId: [2],
                            wrongId: [3]
                        }
                    }
                }
            }
            const existingProps = {
                resourceType: 'correctType',
                resourceId: 'correctId'
            }
            const outcome = {
                permissions: [0]
            }
            assert.deepStrictEqual(mapStateToProps(reduxState, existingProps), outcome)
        })
        it('should use empty array as permissions if no permissions found with the resourceType', () => {
            const reduxState = {
                permission: {
                    byTypeAndId: {
                        resourceType: {
                            resourceId: [0]
                        }
                    }
                }
            }
            const existingProps = {
                resourceType: 'notCorrectType',
                resourceId: 'correctId'
            }
            const outcome = {
                permissions: []
            }
            assert.deepStrictEqual(mapStateToProps(reduxState, existingProps), outcome)
        })
        it('should use empty array as permissions if no permissions found with the resourceId', () => {
            const reduxState = {
                permission: {
                    byTypeAndId: {
                        resourceType: {
                            resourceId: [0]
                        }
                    }
                }
            }
            const existingProps = {
                resourceType: 'resourceType',
                resourceId: 'notCorrectId'
            }
            const outcome = {
                permissions: []
            }
            assert.deepStrictEqual(mapStateToProps(reduxState, existingProps), outcome)
        })
    })
    
    describe('mapDispatchToProps', () => {
        it('should return an object with the right kind of props', () => {
            assert.deepStrictEqual(typeof mapDispatchToProps(), 'object')
            assert.deepStrictEqual(typeof mapDispatchToProps().save, 'function')
        })
        describe('save', () => {
            it('should return saveUpdatedResourcePermissions and call it with right attrs', () => {
                const dispatchSpy = sinon.spy()
                const saveStub = sinon.stub(permissionActions, 'saveUpdatedResourcePermissions', (type, id) => {
                    return `${type}-${id}`
                })
                mapDispatchToProps(dispatchSpy, {
                    resourceType: 'myType',
                    resourceId: 'myId'
                }).save()
                assert(dispatchSpy.calledOnce)
                assert(dispatchSpy.calledWith('myType-myId'))
                assert(saveStub.calledOnce)
            })
        })
    })
})
