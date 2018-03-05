// @flow

import type {CSVImporterSchema, Stream} from '../stream-types'
import type {ErrorInUi} from '../common-types'

export type StreamState = {
    byId: {
        [$ElementType<Stream, 'id'>]: Stream
    },
    openStream: {
        id: ?$ElementType<Stream, 'id'>
    },
    fetching: boolean,
    error?: ?ErrorInUi,
    savingStreamFields: boolean,
    csvUpload: ?{
        id: $ElementType<Stream, 'id'>,
        fetching: boolean,
        fileUrl?: string,
        schema?: CSVImporterSchema
    }
}
