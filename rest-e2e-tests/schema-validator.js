const Ajv = require('ajv')

const categorySchema = require('./schemas/category.json')
const productSchema = require('./schemas/product.json')
const streamSchema = require('./schemas/stream.json')

class SchemaValidator {
    constructor() {
        this.ajv = new Ajv({
            allErrors: true,
            schemas: {
                'Category': categorySchema,
                'Product': productSchema,
                'Stream': streamSchema
            }
        })
    }

    validateCategory(data) {
        return this.ajv.validate('Category', data) ? [] : [... this.ajv.errors]
    }

    validateProduct(data) {
        return this.ajv.validate('Product', data) ? [] : [... this.ajv.errors]
    }

    validateStream(data) {
        return this.ajv.validate('Stream', data) ? [] : [... this.ajv.errors]
    }

    toMessages(errors) {
        return this.ajv.errorsText(errors)
    }
}

module.exports = SchemaValidator