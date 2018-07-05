// @flow

import React from 'react'
import { BrowserRouter, Route, Redirect, Switch } from 'react-router-dom'
import createLink from '../../utils/createLink'

import 'bootstrap/dist/css/bootstrap.min.css'
import '@streamr/streamr-layout/css'
import '@streamr/streamr-layout/pcss'

import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import SignupPage from './pages/SignupPage'
import ForgotPasswordPage from './pages/ForgotPasswordPage'
import ResetPasswordPage from './pages/ResetPasswordPage'

const basename = createLink('/').replace(window.location.origin, '')

import styles from './authPage.pcss'

const AuthPage = () => (
    <BrowserRouter basename={basename}>
        <div className={styles.authPage}>
            <section className={styles.content}>
                <a href="https://www.streamr.com" className={styles.logo}>
                    <img src={createLink('static/images/streamr-logo.svg')} />
                </a>
                <div className={styles.panel}>
                    <Switch>
                        <Route exact path="/login/auth" component={LoginPage} />
                        <Route exact path="/register/register" component={RegisterPage} />
                        <Route exact path="/register/signup" component={SignupPage} />
                        <Route exact path="/register/forgotPassword" component={ForgotPasswordPage} />
                        <Route exact path="/register/resetPassword" component={ResetPasswordPage} />
                        <Redirect from="/register" to="/register/signup" />
                        <Redirect from="/login" to="/login/auth" />
                        <Route path="/" component={() => '404'} />
                    </Switch>
                    <div className={styles.footer}>
                        Made with ❤️ & ☕️ by Streamr Network AG in 2018
                    </div>
                </div>
            </section>
        </div>
    </BrowserRouter>
)

export default AuthPage
