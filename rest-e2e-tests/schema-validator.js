const Ajv = require('ajv')

const categorySchema = require('./schemas/category.json')
const productSchema = require('./schemas/product.json')

class SchemaValidator {
    constructor() {
        this.ajv = new Ajv({
            allErrors: true,
            schemas: {
                'Category': categorySchema,
                'Product': productSchema
            }
        })
    }

    validateCategory(data) {
        return this.ajv.validate('Category', data) ? [] : [... this.ajv.errors]
    }

    validateProduct(data) {
        return this.ajv.validate('Product', data) ? [] : [... this.ajv.errors]
    }

    toMessages(errors) {
        return this.ajv.errorsText(errors)
    }
}

module.exports = SchemaValidator