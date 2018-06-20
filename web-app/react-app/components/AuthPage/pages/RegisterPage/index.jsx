// @flow

import React from 'react'

import AuthPanel from '../../shared/AuthPanel'
import Input from '../../shared/Input'
import Actions from '../../shared/Actions'
import Button from '../../shared/Button'

const RegisterPage = () => (
    <AuthPanel title="Sign Up" signinLink onUseEthClick={() => {}}>
        <Input placeholder="Email" />
        <Actions>
            <Button>Next</Button>
        </Actions>
    </AuthPanel>
)

export default RegisterPage
