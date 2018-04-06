import { configure } from 'enzyme'
import Adapter from 'enzyme-adapter-react-16'
import moxios from 'moxios'

moxios.promiseWait = () => new Promise(resolve => moxios.wait(resolve))

configure({
    adapter: new Adapter()
})

window.requestAnimationFrame = (callback) => {
    setTimeout(callback, 0)
}