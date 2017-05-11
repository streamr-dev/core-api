'use strict';

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _propTypes = require('prop-types');

var _reactDom = require('react-dom');

var _reactDom2 = _interopRequireDefault(_reactDom);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) { arr2[i] = arr[i]; } return arr2; } else { return Array.from(arr); } }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

/* global ConfirmButton */

var unCamelCase = function unCamelCase(str) {
    return str.replace(/([a-z])([A-Z])/g, '$1 $2').replace(/\b([A-Z]+)([A-Z])([a-z])/, '$1 $2$3').replace(/^./, function (s) {
        return s.toUpperCase();
    });
};

var StreamrAccountHandlerInput = function (_React$Component) {
    _inherits(StreamrAccountHandlerInput, _React$Component);

    function StreamrAccountHandlerInput() {
        _classCallCheck(this, StreamrAccountHandlerInput);

        var _this = _possibleConstructorReturn(this, (StreamrAccountHandlerInput.__proto__ || Object.getPrototypeOf(StreamrAccountHandlerInput)).call(this));

        _this.inputs = {};
        return _this;
    }

    _createClass(StreamrAccountHandlerInput, [{
        key: 'render',
        value: function render() {
            var _this2 = this;

            return _react2.default.createElement(
                'form',
                { className: 'input-group form-inline new-account-item-form', ref: function ref(i) {
                        return _this2.form = i;
                    }, style: {
                        display: 'flex',
                        width: '100%'
                    } },
                ['name'].concat(_toConsumableArray(this.props.fields)).map(function (field) {
                    return _react2.default.createElement('input', {
                        key: field,
                        type: 'text',
                        ref: function ref(i) {
                            return _this2.inputs[field] = i;
                        },
                        name: field,
                        className: 'form-control',
                        placeholder: unCamelCase(field),
                        style: {
                            flex: '1'
                        }
                    });
                }),
                _react2.default.createElement(
                    'span',
                    { className: 'input-group-btn', onClick: function onClick(e) {
                            e.preventDefault();
                            var data = {};
                            for (var input in _this2.inputs) {
                                if (!_this2.inputs[input].value) {
                                    return;
                                }
                                data[input] = _this2.inputs[input].value;
                            }
                            _this2.form.reset();
                            _this2.props.onNew(data, e);
                        }, style: {
                            width: 'auto',
                            flex: '0',
                            display: 'inline-block'
                        } },
                    _react2.default.createElement(
                        'button',
                        { className: 'new-account-item-button btn btn-default', type: 'button', style: {
                                height: '100%'
                            } },
                        _react2.default.createElement('span', { className: 'icon fa fa-plus' })
                    )
                )
            );
        }
    }]);

    return StreamrAccountHandlerInput;
}(_react2.default.Component);

StreamrAccountHandlerInput.propTypes = {
    fields: _propTypes.array,
    onNew: _propTypes.func
};

var StreamrAccountHandlerTable = function (_React$Component2) {
    _inherits(StreamrAccountHandlerTable, _React$Component2);

    function StreamrAccountHandlerTable() {
        _classCallCheck(this, StreamrAccountHandlerTable);

        return _possibleConstructorReturn(this, (StreamrAccountHandlerTable.__proto__ || Object.getPrototypeOf(StreamrAccountHandlerTable)).apply(this, arguments));
    }

    _createClass(StreamrAccountHandlerTable, [{
        key: 'render',
        value: function render() {
            var _this4 = this;

            var items = this.props.items || [];
            return _react2.default.createElement(
                'table',
                { className: 'table' },
                _react2.default.createElement(
                    'thead',
                    null,
                    _react2.default.createElement(
                        'tr',
                        null,
                        _react2.default.createElement(
                            'th',
                            null,
                            'Name'
                        ),
                        this.props.fields.map(function (f) {
                            return _react2.default.createElement(
                                'th',
                                { key: f },
                                unCamelCase(f)
                            );
                        }),
                        _react2.default.createElement('th', null)
                    )
                ),
                _react2.default.createElement(
                    'tbody',
                    null,
                    items.map(function (item) {
                        return _react2.default.createElement(
                            'tr',
                            { key: item[_this4.props.idField] },
                            _react2.default.createElement(
                                'td',
                                null,
                                item.name
                            ),
                            _this4.props.fields.map(function (f) {
                                return _react2.default.createElement(
                                    'td',
                                    { key: f },
                                    item[f]
                                );
                            }),
                            _react2.default.createElement(
                                'td',
                                null,
                                _react2.default.createElement(
                                    'button',
                                    {
                                        ref: function ref(el) {
                                            new ConfirmButton(el, {}, function (res) {
                                                if (res) {
                                                    _this4.props.onDelete(item[_this4.props.idField]);
                                                }
                                            });
                                        },
                                        type: 'button',
                                        className: 'form-group account-item-delete-button btn btn-danger pull-right',
                                        title: 'Delete key' },
                                    _react2.default.createElement('span', { className: 'icon fa fa-trash-o' })
                                )
                            )
                        );
                    })
                )
            );
        }
    }]);

    return StreamrAccountHandlerTable;
}(_react2.default.Component);

StreamrAccountHandlerTable.propTypes = {
    fields: _propTypes.array,
    items: _propTypes.array,
    onDelete: _propTypes.func,
    idField: _propTypes.string
};

var StreamrAccountHandlerSegment = function (_React$Component3) {
    _inherits(StreamrAccountHandlerSegment, _React$Component3);

    function StreamrAccountHandlerSegment(props) {
        _classCallCheck(this, StreamrAccountHandlerSegment);

        var _this5 = _possibleConstructorReturn(this, (StreamrAccountHandlerSegment.__proto__ || Object.getPrototypeOf(StreamrAccountHandlerSegment)).call(this, props));

        _this5.state = {
            items: []
        };
        _this5.onNew = _this5.onNew.bind(_this5);
        _this5.onDelete = _this5.onDelete.bind(_this5);
        return _this5;
    }

    _createClass(StreamrAccountHandlerSegment, [{
        key: 'onNew',
        value: function onNew(data) {
            this.setState({
                items: [].concat(_toConsumableArray(this.state.items), [data])
            });
        }
    }, {
        key: 'onDelete',
        value: function onDelete(id) {
            if (id === undefined) {
                return;
            }
            var items = this.state.items;
            var j = void 0;
            for (var i = 0; i < this.state.items.length; i++) {
                if (this.state.items[i][this.props.idField] === id) {
                    j = i;
                }
            }
            delete items[j];
            this.setState({
                items: items
            });
        }
    }, {
        key: 'render',
        value: function render() {
            return _react2.default.createElement(
                'div',
                { className: 'col-xs-12' },
                _react2.default.createElement(
                    'label',
                    null,
                    this.props.name
                ),
                _react2.default.createElement(StreamrAccountHandlerTable, { fields: this.props.fields, items: this.state.items,
                    idField: this.props.idField, onDelete: this.onDelete }),
                _react2.default.createElement(StreamrAccountHandlerInput, { fields: this.props.fields, onNew: this.onNew })
            );
        }
    }]);

    return StreamrAccountHandlerSegment;
}(_react2.default.Component);

StreamrAccountHandlerSegment.propTypes = {
    fields: _propTypes.array,
    items: _propTypes.array,
    name: _propTypes.string,
    idField: _propTypes.string
};

var StreamrAccountHandler = function (_React$Component4) {
    _inherits(StreamrAccountHandler, _React$Component4);

    function StreamrAccountHandler() {
        _classCallCheck(this, StreamrAccountHandler);

        return _possibleConstructorReturn(this, (StreamrAccountHandler.__proto__ || Object.getPrototypeOf(StreamrAccountHandler)).apply(this, arguments));
    }

    _createClass(StreamrAccountHandler, [{
        key: 'render',
        value: function render() {
            return _react2.default.createElement(
                'div',
                { className: 'streamr-account-handler row' },
                _react2.default.createElement(StreamrAccountHandlerSegment, { name: 'Ethereum', fields: ['privateKey'], idField: 'privateKey' })
            );
        }
    }]);

    return StreamrAccountHandler;
}(_react2.default.Component);

_reactDom2.default.render(_react2.default.createElement(StreamrAccountHandler, {}, null), document.getElementById('streamrAccountHandler'));
//# sourceMappingURL=StreamrAccountHandler.js.map