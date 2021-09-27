import Ajv from 'ajv'
import categorySchema from '../schemas/category.json'
import permissionSchema from '../schemas/permission.json'
import productSchema from '../schemas/product.json'
import streamSchema from '../schemas/stream.json'
import subscriptionSchema from '../schemas/subscription.json'
import addFormats from 'ajv-formats'

export class SchemaValidator {

    private ajv: Ajv

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
        addFormats(this.ajv)
    }

    validateCategory(data: any) {
        // @ts-expect-error
        return this.ajv.validate('Category', data) ? [] : [...this.ajv.errors]
    }

    validatePermission(data: any) {
        return this.ajv.validate('Permission', data) ? [] : [...this.ajv.errors]
    }

    validateProduct(data: any) {
        return this.ajv.validate('Product', data) ? [] : [...this.ajv.errors]
    }

    validateStream(data: any) {
        return this.ajv.validate('Stream', data) ? [] : [...this.ajv.errors]
    }

    validateSubscription(data: any) {
        return this.ajv.validate('Subscription', data) ? [] : [...this.ajv.errors]
    }

    toMessages(errors: any) {
        return this.ajv.errorsText(errors)
    }
}
