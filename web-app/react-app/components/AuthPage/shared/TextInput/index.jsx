// @flow

import TextField from './TextField'
import FormControl from '../FormControl'

export default FormControl(TextField, ({ target }: SyntheticInputEvent<EventTarget>) => target.value)
