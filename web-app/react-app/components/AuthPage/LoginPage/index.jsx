// @flow

import React, {Component, Fragment} from 'react'
import classnames from 'classnames'
import {Link} from 'react-router-dom'
import {Button} from '@streamr/streamr-layout'
import Input from '../Input'
import AuthPanel from '../AuthPanel'

import styles from './loginPage.pcss'

type Props = {}

type State = {
    stage: 0,
    username: string,
}

export default class LoginPage extends Component<Props, State> {
    state = {
        stage: 0,
        username: '',
        error: null,
    }

    onNext = () => {
        if (!this.state.username) {
            this.setState({
                error: 'Oops! Username cannot be empty'
            })
        } else {
            this.setState({
                error: null,
                stage: this.state.stage + 1,
            })
        }
    }

    onUsernameChange = (e: Event) => {
        this.setState({
            username: e.target.value,
        })
    }

    render() {
        const {stage, error, username} = this.state
        return (
            <AuthPanel
                title={'Sign In'}
                leftUtil={'Sign in with ethereum'}
                rightUtil={<Link to="/register">Sign up</Link>}
            >
                <div className={classnames(styles.loginPage, {
                    [styles.usernameStage]: stage === 0,
                    [styles.passwordStage]: stage === 1,
                })}>
                    <Fragment>
                        <Input type="email" placeholder="Email" block onChange={this.onUsernameChange} value={username} />
                        <div className={styles.errorContainer}>
                            {error}
                        </div>
                        <div className={styles.buttonContainer}>
                            <Button color="primary" onClick={this.onNext}>
                                Next
                            </Button>
                        </div>
                    </Fragment>
                </div>
            </AuthPanel>
        )
    }
}
