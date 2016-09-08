#What is a service?

A Streamr service is a digital agent which processes streaming real-time data for you.  A service contains one or more modules (these do the processing) and one or more [streams](#whatisastream) (these provide the data).  Streams and modules are connected in the configuration you'll design.  Those connections define how the data flows through the service.

There's a wide variety of built-in modules in Streamr.  Some of those perform basic arithmetic and logical operations, filtering, sampling, aggregation, and so on. Others transform the data in some fashion and feed it to the next stage.  Yet other modules interact with the outside world and with external systems.

Computation in a Streamr service is entirely event-based. Any module will execute immediately when activated by incoming events. When new events arrive in the input stream, the data automatically flows through the service. This inherently asynchronous process allows for fast and continuous in-memory processing of large volumes of data.

**Add a paragraph to make the case that the service topology is more than a sequential workflow?**

As a simple example, here's a service consisting of one stream and a chart module connected together.  When you run the service, the events flow from the stream to the chart, and the chart draws the data points as they arrive.

<r:img plugin="unifina-core" dir="images/user-guide" file="my-first-stream-on-canvas.png" class="img-responsive center-block" />

You can run a service with either historical or real-time data.

- In the *historical mode*, running a service is a playback of what would have happened in the past. The playback mode can be extremely useful when you’re testing, refining, or demonstrating the functionality of a service.
- The *real-time mode* is used in production where you want to react to events as they arrive. There's no need to modify the service if you decide to use it in the real-time mode after testing with historical data.  One click is all it takes to activate a service and take it live.