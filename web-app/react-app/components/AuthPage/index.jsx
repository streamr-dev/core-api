// @flow

import React from 'react'
import { BrowserRouter, Route, Redirect, Switch } from 'react-router-dom'
import createLink from '../../utils/createLink'

import 'bootstrap/dist/css/bootstrap.min.css'
import '@streamr/streamr-layout/css'
import '@streamr/streamr-layout/pcss'
import 'react-select/dist/react-select.css'

import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import SignupPage from './pages/SignupPage'
import ForgotPasswordPage from './pages/ForgotPasswordPage'
import ResetPasswordPage from './pages/ResetPasswordPage'
import GoogleAnalyticsTracker from '../../components/GoogleAnalyticsTracker'
import Footer from './shared/Footer'
import Logo from './shared/Logo'
import styles from './authPage.pcss'
import isProduction from '../../utils/isProduction'

const basename = createLink('/').replace(window.location.origin, '')

const AuthPage = () => (
    <BrowserRouter basename={basename}>
        <div className={styles.root}>
            <div className={styles.outer}>
                <section className={styles.content}>
                    <div className={styles.inner}>
                        <Logo className={styles.logo} />
                        <div className={styles.panel}>
                            <Switch>
                                <Route exact path="/login/auth" component={LoginPage} />
                                <Route exact path="/register/register" component={RegisterPage} />
                                <Route exact path="/register/signup" component={SignupPage} />
                                <Route exact path="/register/forgotPassword" component={ForgotPasswordPage} />
                                <Route exact path="/register/resetPassword" component={ResetPasswordPage} />
                                <Redirect from="/register" to="/register/signup" />
                                <Redirect from="/login" to="/login/auth" />
                                <Redirect from="/" to="/login/auth" />
                            </Switch>
                        </div>
                    </div>
                </section>
                <Footer className={styles.footer} mobile />
            </div>
            {isProduction() && <GoogleAnalyticsTracker />}
        </div>
    </BrowserRouter>
)

export default AuthPage
