#Streams

All data in Streamr is stored in a **stream**. A stream is simply a sequence of events in time. You can add new data to the end of a stream, and a stream will give the data back to you in the correct order. 

You can store different kinds of data in the same stream.  The data may be numeric, but it can equally well consist of strings, collections of elementary data types, or associative arrays. Each event contains at least one data field, but you can have as many fields per event as required. The data are persistent and stored in the cloud.

You can use a stream as a pub/sub-device, push data into it, and subscribe to the data elsewhere. However, the raison d'être for a stream is its capability to provide real-time inputs to a Streamr service, and act as a recipient of real-time output from such a service.

In this chapter, we’ll show some examples and describe the built-in data types. We'll then discuss how to do the following:

- Create a stream.
- Edit a stream.
- Delete a stream.
- Upload historical data.
- Push events to a stream.
- Subscribe to a stream.

**Discuss database (MongoDb) poller streams somewhere**

##Stream examples

Here’s an example of what a small part of a stream could look like. Each row shows one event, and the columns correspond to the timestamp followed by two data fields: A measurement of the operating temperature and the number of rotations per minute (RPM).

Timestamp               | Temperature | RPM
:---------------------- |:------------|:----
2016-02-01 11:30:01.012 | 312.56      | 3550
2016-02-01 11:30:02.239 | 312.49      | 3549
2016-02-01 11:30:04.105 | 312.42      | 3543
2016-02-01 11:30:08.122 | 313.21      | 3565
2016-02-01 11:30:11.882 | 317.45      | 3602
...                     |             |

As an example of a more complicated event, here’s a data point in a stock market stream.

    {
      "Symbol": "PFFT",
      "EventType": 1,
      "OrderId": 6454321,
      "Direction": "Up",
      "Trade": {"Price": 118.55, "Size": 100},
      "Ask": [
              {"Price": 118.6, "Size": 22500},
              {"Price": 118.65, "Size": 18000},
              {"Price": 118.7, "Size": 13000},
              {"Price": 118.8, "Size": 8000},
              {"Price": 119, "Size": 45000}
              ],
      "Bid": [
              {"Price": 118.5, "Size": 16500},
              {"Price": 118.45, "Size": 11000},
              {"Price": 118.4, "Size": 14200},
              {"Price": 118.2, "Size": 19000},
              {"Price": 118, "Size": 50000}
    ]}

##Built-in data types

There’s a number of built-in data types that can be used in a stream. These are the following:

Number
:   A numeric data type internally stored as a double precision (64-bit) float.

Boolean
:   A logical data type with two possible values, True and False. In Streamr, a numeric value exactly equal to one represents logical truth. Anything else is interpreted as a logical falsehood.

String
:   A sequence of zero or more alphabetical characters.

Map
:   A collection of key-value pairs. Each key is a string, and the value can be of any built-in data type (even a Map again). Map is the same as a dictionary or an associative array found in a number of programming languages.

List
:   An ordered collection of zero or more elements.

Data types can be freely mixed in one event. And you can freely add new fields to an existing stream; you don’t have to know what fields you might eventually need. A single event can be of any size within reason, and a stream can grow indefinitely when extended by new events. 

There is no theoretical limitation as to the format or type of data in Streamr. Anything which can be expressed in digital form is fair game. It is perfectly possible to create streams which contain digital images, streaming video, or other domain-specific data. If your use case takes you beyond the built-in data types, come and talk to us about what you have in mind.

##Creating a stream

You can create new streams either through the user interface or by using the <g:link controller="help" action="api">stream API</g:link>. Each stream is identified by a unique ID. There’s no technical limit on the overall number of streams.

If you want to create a stream manually, go to the <kbd>Streams</kbd> tab.  There’s a button which looks like this:

<g:img dir="images/user-guide" file="create-stream-button.png" class="img-responsive" />

A click takes you to a dialog where you’ll fill in the stream name and an optional description.

<g:img dir="images/user-guide" file="create-stream-dialog.png" class="img-responsive center-block" />

A new stream is created when you click the **Next** button.  You’ll be shown a stream view that includes the stream details (the name and description), API credentials, configured fields (there won’t be any yet), and a summary of stream history (there will be none yet). 

<g:img dir="images/user-guide" file="my-first-stream-view.png" class="img-responsive center-block" />

##Editing a stream

If you want to edit stream details, you need to be in the <kbd>Streams</kbd> tab. Click on the stream name, and then click on the **Edit info** button. You'll see a dialog where you can rename a stream or modify its description.

<g:img dir="images/user-guide" file="edit-stream-dialog.png" class="img-responsive center-block" />

The stream view also includes the option of configuring the data fields.  If you’ll load real-time data through the API, you’ll need to configure the data fields now.  If you’ll first load historical data from a text file, you can skip this step.  We’ll be in many cases able to to autodetect the field types from the input.

If you want to configure the data fields manually, the Configure Fields button takes you to a setup dialog.  To add a new data field, click on the **+ Add Field** button, give the field a name and change the field type as appropriate.  Remember to save the changes when done.

<g:img dir="images/user-guide" file="configure-fields-dialog.png" class="img-responsive center-block" />

You can also rename a stream, edit the description, add data fields and specify the field types using the [stream API](#streamAPIreference).

##Deleting a stream

You need to be in the <kbd>Streams</kbd> tab in order to delete a stream. Click on the stream name, and then click on the **Delete stream** button. You’ll be asked to confirm that you really want to go ahead.

Alternatively, you can delete a stream using the <g:link controller="help" action="api">stream API</g:link>.

##Uploading historical data

Batches of historical events can be loaded into a stream by importing a CSV file. You need to be in the <kbd>Streams</kbd> tab to do this. When you click on an existing stream, you’ll see a History panel and a data drop.  This is where you can drop a text file with a batch of event history.

<g:img dir="images/user-guide" file="csv-data-drop.png" class="img-responsive center-block" />

You can also pick a local file for import by manually by clicking on the data drop.  Either way, Streamr will parse the given CSV file and load the events into the stream.

As to the format, the CSV file should have the column names on the first row, and use either a comma or a semicolon as the separator.  One of the columns should be a timestamp, where the recommended format is either `"yyyy-MM-dd HH:mm:ss"` or `"yyyy-MM-dd HH:mm:ss.SSS”`.  Timestamps must be in a chronological order with earlier events first and the recent events last.

If Streamr cannot find the event timestamps or doesn’t understand the timestamp format, you will see a dialog box like the one below.  This is where you can manually select the timestamp column and specify the format.

<g:img dir="images/user-guide" file="csv-field-dialog.png" class="img-responsive center-block" />

We’ll do our best to make sense of the data columns in the CSV file, but the autodetection of field types will not always work.  For instance, a column of telephone numbers may be interpreted as numbers even if you’d probably want to import them as strings.  In such cases, you’ll need to configure the fields manually as shown above.  Mind you, making changes that don’t make sense will cause runtime exceptions due to incompatible data types.

Let’s go ahead and upload some sample data.  We’ll import a text file which contains a collection of recent tweets found with the keywords `“augmented intelligence”`.  This is what the sample tweet data looks like, as at the time of writing, with only four columns and a subset of rows shown:

<g:img dir="images/user-guide" file="sample-twitter-data.png" class="img-responsive center-block" />

The data file is called `“SampleTweets.csv”`, and you can download the latest version to your desktop from this [link](“SampleTweets.csv”). 

If you drag the the sample file to the data drop, the events are uploaded to the stream.  Once the process is complete, the stream view is updated to show the extent of the archived history. 

<g:img dir="images/user-guide" file="twitter-stream-view.png" class="img-responsive center-block" />

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

    curl -i -X POST -H "Stream: -2IwFcsJSzO__9nt0nhc7g" -H "Auth: cZhdnH7OQpK9ip07rttKSQ" \
    -d "{\"foo\":\"hello\",\"bar\":24.5}" http://data.streamr.com/json

If the call is successful, the data API returns the code 204 (i.e. “no content”).  These are the possible return codes:

Code | Description
---- | -----------
204  | Success
400  | Invalid request
403  | Authentication failed
404  | Unknown endpoint
500  | Unexpected error

##Subscribing to a stream

<g:img dir="images/user-guide" file="add-twitter-stream.png" class="side-image" />

You’ll need to be a stream subscriber in order to receive events. Streamr makes the subscription process trivially easy: You place a stream module on a digital canvas in Streamr user interface, and the events start flowing downstream for further processing.  You can also subscribe to a stream in external applications with the [Javascript API](#javascript-API-reference).

If you want to subscribe to a stream in the user interface, you can either work with a new canvas or load an existing one in the Canvases tab.  Start typing in the name of a stream in a text box labeled **Add Stream / Module**.  We’ll find the stream for you as you type.

When you click on the match, the stream module will be placed on the canvas.  The events in the stream are now available at the output endpoints.  In this case, we’ve got data fields for `TweetText`, `TweetID`, `UserName`, `UserTimeZone`, etc.

You can next add processing modules and start creating intelligence on top of the real-time data that flows from the stream.  Or you can first place other streams in the canvas and combine different data sources.  See the chapter on [services](#services) for examples of what you can do.

<br style="clear:both;" />

For now, we’ll just add a **Table** module to visualise the data.  This is what we get:

<g:img dir="images/user-guide" file="twitter-stream-on-canvas.png" class="img-responsive center-block" />

