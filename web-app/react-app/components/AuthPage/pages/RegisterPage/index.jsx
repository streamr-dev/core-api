// @flow

import React from 'react'

import AuthPanel from '../../shared/AuthPanel'
import Input from '../../shared/Input'
import Actions from '../../shared/Actions'
import Button from '../../shared/Button'
import AuthStep from '../../shared/AuthStep'

const RegisterPage = () => (
    <AuthPanel>
        <AuthStep title="Sign Up" showEth showSignup>
            <Input placeholder="Your Name" />
            <Actions>
                <Button proceed>Next</Button>
            </Actions>
        </AuthStep>
        <AuthStep title="Sign Up">
            <Input placeholder="Create a Password" type="password" />
            <Actions>
                <Button proceed>Next</Button>
            </Actions>
        </AuthStep>
        <AuthStep title="Sign Up" showBack>
            <Input placeholder="Confirm your password" type="password" />
            <Actions>
                <Button proceed>Next</Button>
            </Actions>
        </AuthStep>
        <AuthStep title="Timezone" showBack>
            Timezone
            <Actions>
                <Button proceed>Next</Button>
            </Actions>
        </AuthStep>
        <AuthStep title="Terms">
            Terms
            <Actions>
                <Button proceed>Finish</Button>
            </Actions>
        </AuthStep>
        <AuthStep title="Done" showSignin>
            Yep.
        </AuthStep>
    </AuthPanel>
)

export default RegisterPage
