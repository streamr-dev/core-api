import * as React from 'react'
import { Link } from 'react-router-dom'

import AuthPanel from '../AuthPanel'
import Input from '../Input'
import Actions from '../Actions'
import Button from '../Button'
import Checkbox from '../Checkbox'

const LoginPage = () => (
    <AuthPanel title="Sign In" signupLink onUseEthClick={() => {}}>
        <Input placeholder="Email" />
        <Actions>
            <Checkbox>Remember me</Checkbox>
            <Link to="#">Forgot your password?</Link>
            <Button>Next</Button>
        </Actions>
    </AuthPanel>
)

export default LoginPage
