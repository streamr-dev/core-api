const Ajv = require('ajv')

const categorySchema = require('./schemas/category.json')
const permissionSchema = require('./schemas/permission.json')
const productSchema = require('./schemas/product.json')
const streamSchema = require('./schemas/stream.json')
const subscriptionSchema = require('./schemas/subscription.json')

class SchemaValidator {
    constructor() {
        this.ajv = new Ajv({
            allErrors: true,
            schemas: {
                'Category': categorySchema,
                'Permission': permissionSchema,
                'Product': productSchema,
                'Stream': streamSchema,
                'Subscription': subscriptionSchema
            }
        })
    }

    validateCategory(data) {
        return this.ajv.validate('Category', data) ? [] : [... this.ajv.errors]
    }

    validatePermission(data) {
        return this.ajv.validate('Permission', data) ? [] : [... this.ajv.errors]
    }

    validateProduct(data) {
        return this.ajv.validate('Product', data) ? [] : [... this.ajv.errors]
    }

    validateStream(data) {
        return this.ajv.validate('Stream', data) ? [] : [... this.ajv.errors]
    }

    validateSubscription(data) {
        return this.ajv.validate('Subscription', data) ? [] : [... this.ajv.errors]
    }

    toMessages(errors) {
        return this.ajv.errorsText(errors)
    }
}

module.exports = SchemaValidator
