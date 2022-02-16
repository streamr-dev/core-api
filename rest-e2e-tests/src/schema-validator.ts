import Ajv from 'ajv'
import categorySchema from '../schemas/category.json'
import permissionSchema from '../schemas/permission.json'
import productSchema from '../schemas/product.json'
import subscriptionSchema from '../schemas/subscription.json'

export class SchemaValidator {

    ajv: Ajv.Ajv

    constructor() {
        this.ajv = new Ajv({
            allErrors: true,
            schemas: {
                'Category': categorySchema,
                'Permission': permissionSchema,
                'Product': productSchema,
                'Subscription': subscriptionSchema
            }
        })
    }

    validateCategory(data: any): Array<Ajv.ErrorObject> {
        const schemaKeyRef = 'Category';
        return this.validate(schemaKeyRef, data);
    }

    validatePermission(data: any): Array<Ajv.ErrorObject> {
        const schemaKeyRef = 'Permission';
        return this.validate(schemaKeyRef, data);
    }

    validateProduct(data: any): Array<Ajv.ErrorObject> {
        const schemaKeyRef = 'Product';
        return this.validate(schemaKeyRef, data);
    }

    validateSubscription(data: any): Array<Ajv.ErrorObject> {
        const schemaKeyRef = 'Subscription';
        return this.validate(schemaKeyRef, data);
    }

    private validate(schemaKeyRef: string, data: any): Array<Ajv.ErrorObject> {
        if (this.ajv.validate(schemaKeyRef, data)) {
            return []
        }
        if (this.ajv.errors) {
            return this.ajv.errors
        }
        return []
    }

    toMessages(errors: any): string {
        return this.ajv.errorsText(errors)
    }
}
