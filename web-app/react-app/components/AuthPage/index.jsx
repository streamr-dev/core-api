// @flow

import React from 'react'
import { BrowserRouter, Route, Redirect, Switch } from 'react-router-dom'
import createLink from '../../utils/createLink'

import 'bootstrap/dist/css/bootstrap.min.css'
import '@streamr/streamr-layout/css'
import '@streamr/streamr-layout/pcss'

import Nav from './AuthPageNav'
import Footer from './AuthPageFooter'
import LoginPage from './LoginPage'
import RegisterPage from './RegisterPage'

const basename = createLink('/').replace(window.location.origin, '')

import styles from './authPage.pcss'

const AuthPage = () => (
    <BrowserRouter basename={basename}>
        <div className={styles.authPage}>
            <Nav />
            <section className={styles.content}>
                <Switch>
                    <Route exact path="/login/auth" component={LoginPage} />
                    <Route exact path="/register/signup" component={RegisterPage} />
                    <Redirect from="/register" to="/register/signup" />
                    <Redirect from="/login" to="/login/auth" />
                    <Route path="/" component={() => '404'} />
                </Switch>
            </section>
            <Footer />
        </div>
    </BrowserRouter>
)

export default AuthPage
