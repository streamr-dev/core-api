// @flow

import React from 'react'
import AuthPanel from '../AuthPanel'
import Input from '../Input'

const RegisterPage = () => (
    <AuthPanel title="Sign Up" ethLink signinLink>
        <Input placeholder="Email" />
    </AuthPanel>
)

export default RegisterPage
