#What is a stream?

All data in Streamr is stored in a stream. A stream is simply a sequence of events in time. You can add new data to the end of a stream, and a stream will give the data back to you in the correct order. Each stream is identified by a unique ID. There’s no technical limit on the overall number of streams. 

You can store different kinds of data in the same stream.  The data may be numeric, but it can equally well consist of strings, collections of elementary data types, or associative arrays. Each event contains at least one data field, but you can have as many fields per event as required. The data are persistent and stored in the cloud.

The Streamr platform includes a number of tools for [working with streams](#workingwithstreams). You can manage streams, upload batches of historical data, add real-time data, and subscribe to a stream within the platform or from external applications.

<hr style="width: 50%; border-top: #E9570F solid 1px;  margin-top: 20px; margin-bottom: 20px">

##Examples

Here’s an example of what a small part of a stream could look like. Each row shows one event, and the columns correspond to the timestamp followed by two data fields, a measurement of the operating temperature and the number of rotations per minute (RPM).

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

<hr style="width: 50%; border-top: #E9570F solid 1px;  margin-top: 20px; margin-bottom: 20px">

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
