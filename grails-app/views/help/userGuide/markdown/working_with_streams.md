<a name="working-with-streams"></a>
#Working with streams

All data in Streamr is stored in [streams](#what-is-a-stream). A stream is a timestamped sequence of events.  A stream is capable of receiving and saving new data points, and it will return data in the correct sequence when needed.

You can use a stream as a pub/sub-device, push data into it, and subscribe to the data elsewhere. However, the raison d'être for a stream is its capability to provide real-time inputs to a streaming service, and act as a recipient of real-time output from a service.

In this section, we’ll show how to do the following:

- Create or delete streams.
- Edit stream details.
- Upload historical data.
- Push events to a stream.
- Subscribe to a stream.

<hr style="width: 50%; border-top: #E9570F solid 1px;  margin-top: 20px; margin-bottom: 20px">

<center>Discuss database (MongoDb) poller streams</center>

<hr style="width: 50%; border-top: #E9570F solid 1px;  margin-top: 20px; margin-bottom: 20px">

##Creating or deleting streams

You can create new streams either through the user interface or by using the [stream API](#stream-API-reference).  If you want to create a stream manually, go to the Streams section.  There’s a button which looks like this:

<g:img dir="images/user-guide" file="create-stream-button.png" class="img-responsive center-block" />

A click on the button takes you to a dialog where you’ll fill in the stream name and an optional description.

<g:img dir="images/user-guide" file="create-stream-dialog.png" class="img-responsive center-block" />

A new stream is created when you press the **Next** button.  You’ll be shown a stream view that includes the stream details (the name and description), API credentials, configured fields (there won’t be any yet), and a summary of stream history (there will be none yet). 

<g:img dir="images/user-guide" file="my-first-stream-view.png" class="img-responsive center-block" />

If you want to delete a stream, click on the **Delete stream** button. You’ll be asked to confirm that you really want to go ahead.

<hr style="width: 50%; border-top: #E9570F solid 1px;  margin-top: 20px; margin-bottom: 20px">

##Editing stream details

The stream details can be edited by clicking on the **Edit info** button. This is where you rename a stream or modify its description.

<g:img dir="images/user-guide" file="edit-stream-dialog.png" class="img-responsive center-block" />

The stream view also includes the option of configuring the data fields.  If you’ll load real-time data through the API, you’ll need to configure the data fields now.  If you’ll first load historical data from a text file, you can skip this step.  We’ll be in many cases able to to autodetect the field types from the input.

If you want to configure the data fields manually, the Configure Fields button takes you to a setup dialog.  To add a new data field, click on the **+ Add Field** button, give the field a name and change the field type as appropriate.  Remember to save the changes when done.

<g:img dir="images/user-guide" file="configure-fields-dialog.png" class="img-responsive center-block" />

You can also add data fields and specify the field types using the [stream API](#stream-API-reference).

<hr style="width: 50%; border-top: #E9570F solid 1px;  margin-top: 20px; margin-bottom: 20px">

##Uploading historical data

Batches of historical events can be loaded into a stream by importing a CSV file.  If you click on an existing stream, you’ll see a History panel and a data drop.  This is where you can drop a text file with a batch of event history.

<g:img dir="images/user-guide" file="csv-data-drop.png" class="img-responsive center-block" />

You can also pick a local file for import by manually by clicking on the data drop.  Either way, Streamr will parse the given CSV file and load the events into the stream.

As to the format, the CSV file should have the column names on the first row, and use either a comma or a semicolon as the separator.  One of the columns should be a timestamp, where the recommended format is either `"yyyy-MM-dd HH:mm:ss"` or `"yyyy-MM-dd HH:mm:ss.SSS”`.  Timestamps must be in a chronological order with earlier events first and the recent events last.

If Streamr cannot find the event timestamps or doesn’t understand the timestamp format, you will see a dialog box like the one below.  This is where you can manually select the timestamp column and specify the format.

<g:img dir="images/user-guide" file="csv-field-dialog.png" class="img-responsive center-block" />

We’ll do our best to make sense of the data columns in the CSV file, but the autodetection of field types will not always work.  For instance, a column of telephone numbers may be interpreted as numbers even if you’d probably want to import them as strings.  In such cases, you’ll need to configure the fields manually as shown above.  Mind you, making changes that don’t make sense will cause runtime exceptions due to incompatible data types.

Let’s now go ahead and upload some sample data.  We’ll import a text file which contains a collection of recent tweets found with the keywords `“augmented intelligence”`.  This is what the sample tweet data looks like, as at the time of writing, with only four columns and a subset of rows shown:

<g:img dir="images/user-guide" file="sample-twitter-data.png" class="img-responsive center-block" />

The data file is called `“SampleTweets.csv”`, and you can download the latest version to your desktop from this [link](“SampleTweets.csv”). 

If you drag the the sample file to the data drop, the events are uploaded to the stream.  Once the process is complete, the stream view is updated to show the extent of the archived history. 

<g:img dir="images/user-guide" file="twitter-stream-view.png" class="img-responsive center-block" />

<hr style="width: 50%; border-top: #E9570F solid 1px;  margin-top: 20px; margin-bottom: 20px">

##Pushing events to a stream

Streamr has a simple API which allows you to push events in JSON format to a stream.  The events are immediately processed by any canvas which subscribes to the stream.  The events are also available for historical playback as soon as they are received by Streamr.

You can push events to a stream from any programming language.  This is the HTTP endpoint for the JSON API:

    http://data.streamr.com/json

If you want to send the data via an encrypted connection, use HTTPS instead of HTTP.  

The authentication information is in the request headers, and the actual data payload is in the request body.  These are the possible headers:

Header    | Required | Description
:-------- | :------- | :----------
Stream    | Yes      | The stream ID
Auth      | Yes      | The stream authorisation key
Timestamp | No       | Java/Javascript time

The event timestamp is optional.  If omitted, the current timestamp on the receiving server is used.  If you want to include an explicit timestamp, it should be given in milliseconds since January 1, 1970 UTC.  In any case, any explicit timestamp only affects the playback.  Each event is processed as soon as it is received.

Here’s an example of request headers:

    Stream: -2IwFcsJSzO__9nt0nhc7g
    Auth: cZhdnH7OQpK9ip07rttKSQ
    Timestamp: 1441227869000

And here’s an example of a request body.

    {
        "foo": "hello",
        "bar": 24.5
    }

A fully-formed request example using `jquery` looks like the following:

    var msg = {
        foo: "hello",
        bar: 24.5
    }

    $.ajax({
        type: "POST",
        url: "http://data.streamr.com/json",
        headers: {
            Stream: "-2IwFcsJSzO__9nt0nhc7g",
            Auth: "cZhdnH7OQpK9ip07rttKSQ",
            Timestamp: Date.now()
        },
        data: JSON.stringify(msg)
    });


The same example using `curl` looks like this.

    curl -i -X POST -H "Stream: -2IwFcsJSzO__9nt0nhc7g" -H "Auth: cZhdnH7OQpK9ip07rttKSQ" -d "{\"foo\":\"hello\",\"bar\":24.5}" http://data.streamr.com/json

If the call is successful, the data API returns the code 204 (i.e. “no content”).  These are the possible return codes:

Code | Description
---- | -----------
204  | Success
400  | Invalid request
403  | Authentication failed
404  | Unknown endpoint
500  | Unexpected error

<hr style="width: 50%; border-top: #E9570F solid 1px;  margin-top: 20px; margin-bottom: 20px">

##Subscribing to a stream

<g:img dir="images/user-guide" file="add-twitter-stream.png" class="side-image" />

You’ll need to be a stream subscriber in order to receive events. Streamr makes the subscription process trivially easy: You place a stream module on a digital canvas in Streamr user interface, and the events start flowing downstream for further processing.  You can also subscribe to a stream in external applications with the [Javascript API](#javascript-API-reference).

If you want to subscribe to a stream in the user interface, you can either work with a new canvas or load an existing one in the Canvas section.  Start typing in the name of a stream in a text box labeled **Add Stream / Module**.  We’ll find the stream for you as you type.

When you click on the match, the stream module will be placed on the canvas.  The events in the stream are now available at the output endpoints.  In this case, we’ve got data fields for `TweetText`, `TweetID`, `UserName`, `UserTimeZone`, etc.

You can next add processing modules and start creating intelligence on top of the real-time data that flows from the stream.  Or you can first place other streams in the canvas and combine different data sources.  See the chapter on [**Working with canvases**](#working-with-canvases) for examples of what you can do.

<br style="clear:both;" />

For now, we’ll just add a Table module to visualise the data.  This is what we get:

<g:img dir="images/user-guide" file="twitter-stream-on-canvas.png" class="img-responsive center-block" />

