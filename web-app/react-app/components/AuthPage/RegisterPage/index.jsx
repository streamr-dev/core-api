// @flow

import React from 'react'

import AuthPanel from '../AuthPanel'
import Input from '../Input'
import Actions from '../Actions'
import Button from '../Button'

const RegisterPage = () => (
    <AuthPanel title="Sign Up" signinLink onUseEthClick={() => {}}>
        <Input placeholder="Email" />
        <Actions>
            <Button>Next</Button>
        </Actions>
    </AuthPanel>
)

export default RegisterPage
