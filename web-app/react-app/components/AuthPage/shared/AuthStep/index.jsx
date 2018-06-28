// @flow

import * as React from 'react'

type PanelProps = {
    title: string,
}

type Props = PanelProps & {
    children: React.Node,
}

const AuthStep = ({ children }: Props) => (
    <div>{children}</div>
)

export default AuthStep
