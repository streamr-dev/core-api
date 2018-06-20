// @flow

import React from 'react'

import AuthPanel from '../../shared/AuthPanel'
import Input from '../../shared/Input'
import Actions from '../../shared/Actions'
import Button from '../../shared/Button'
import AuthStep from '../../shared/AuthStep'

const RegisterPage = () => (
    <AuthPanel title="Sign Up">
        <AuthStep showEth showSignup>
            <Input placeholder="Your Name" />
            <Actions>
                <Button proceed>Next</Button>
            </Actions>
        </AuthStep>
        <AuthStep>
            <Input placeholder="Create a Password" type="password" />
            <Actions>
                <Button proceed>Next</Button>
            </Actions>
        </AuthStep>
        <AuthStep showBack>
            <Input placeholder="Confirm your password" type="password" />
            <Actions>
                <Button proceed>Next</Button>
            </Actions>
        </AuthStep>
        <AuthStep showBack>
            Timezone
            <Actions>
                <Button proceed>Next</Button>
            </Actions>
        </AuthStep>
        <AuthStep>
            Terms
            <Actions>
                <Button proceed>Finish</Button>
            </Actions>
        </AuthStep>
        <AuthStep showSignin>
            Done.
        </AuthStep>
    </AuthPanel>
)

export default RegisterPage
