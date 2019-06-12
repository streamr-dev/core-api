// @flow

import axios from 'axios'
import qs from 'qs'
import type { FormFields } from './types'
import type { ComponentType } from 'react'

export const onInputChange = (callback: (string, any) => void, inputName: ?string) => (e: SyntheticInputEvent<EventTarget>) => {
    const value = e.target.type === 'checkbox' ? e.target.checked : e.target.value
    callback(inputName || e.target.name, value)
}

export const post = (url: string, data: FormFields, successWithError: boolean, xhr: boolean): Promise<*> => new Promise((resolve, reject) => {
    axios
        .post(url, qs.stringify(data), {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                ...(xhr ? {
                    'X-Requested-With': 'XMLHttpRequest',
                } : {}),
            },
        })
        .then(({ data }) => {
            if (successWithError && data.error) {
                reject(new Error(data.error))
            } else {
                resolve(data)
            }
        }, ({ response: { data } }) => {
            reject(new Error(data.error || 'Something went wrong'))
        })
})

export const getDisplayName = (WrappedComponent: ComponentType<any>) => (
    WrappedComponent.displayName || WrappedComponent.name || 'Component'
)

export const noop = () => {}
