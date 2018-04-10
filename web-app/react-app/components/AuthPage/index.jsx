// @flow

import React from 'react'
import { BrowserRouter, Route, Switch } from 'react-router-dom'
import createLink from '../../helpers/createLink'

import Nav from '/AuthPageNav'.
import LoginPage from './LoginPage'
import RegisterPage from './RegisterPage'

const basename = createLink('/').replace(window.location.origin, '')

import styles from './authPage.pcss'

const AuthPage = () => (
    <BrowserRouter basename={basename}>
        <div className={styles.authPage}>
            <Nav />
            <section>
                <Switch>
                    <Route exact path="/login" component={LoginPage} />
                    <Route exact path="/register" component={RegisterPage} />
                    <Route path="/" component={() => '404'} />
                </Switch>
            </section>
        </div>
    </BrowserRouter>
)

export default AuthPage
