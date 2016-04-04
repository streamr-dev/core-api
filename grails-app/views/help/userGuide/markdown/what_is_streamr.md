#Introduction

Streamr is a cloud-based platform for automatically reacting to events in real-time data streams.  It abstracts the complexity of the underlying stream processing technology and provides a user-friendly front end, saving you time and money in the process.

There’s many things you can do with Streamr. For instance, you’ll be able to monitor real-time industrial processes, visualise sensory data from IoT devices, calculate complex streaming analytics, take automatic action when abnormal patterns are detected, extract real-time social media analytics, track customer payments, orders, or deliveries, and automate business processes. If you have a source of real-time data and you want to do useful things with the data, Streamr is for you.

Streamr makes building real-time automation easy. You can automate complicated sequences of events or calculate streaming analytics. There is a browser-based interface where you build up a digital canvas by visual programming. You can also code processing modules in Java if that's your preference. Or you can mix and match visual programming elements with Java modules.

The platform is flexible and fast. Data streams can contain any kind of data in any format, and the technology can handle millions of events per second. The underlying software runs in the cloud and additional computing capacity is deployed automatically as required.

There is also a playback mode where you can simulate your processing logic over days, weeks, months, or years of data over billions of events. When you’re happy, you can go live at a click of a button. In live mode, Streamr listens to real-time data arriving in your data streams, processes the events as instructed, and takes the specified actions.

The secret sauce of Streamr is the hidden technology which makes real-time automation easy. Apart from the browser-based user interface and visual programming tools, Streamr takes care of the data flow, event propagation, and message queues. It also manages the cloud computing services and stores the event data on your behalf.

##Data live in streams

All real-time data in Streamr is stored in a [stream](“What is a stream?”). A stream is essentially a timestamped sequence of events. Each event may contain more than one field. You can create and work on dozens, hundreds, or thousands of streams. A stream is persistent, identified by a unique ID, and stored in the cloud.

Any kind of real-time data can be stored in a stream. The incoming data could consist of sensory readings of speed, geolocation, orientation, ambient temperature, humidity, and so forth. Or the data might consist of social media messages, stock market events, mobile ad impressions, and so on.

There is no limit on the kind, format, or quantity of the data you can feed into Streamr. The data may originate from machines in a factory, from devices you sell to your clients, or from commercial streaming data feeds.

There are a few different ways to get data into a stream. Streamr has a simple HTTP API which allows you to push events in JSON format to a stream automatically from any programming language. You can also batch load historical events using Streamr’s CSV loader. If the events originate in a database or in a commercial data feed, talk to us. We'll figure out what kind of adapter is needed.

Streams implement a publish-subscribe paradigm, or pub/sub for short. A stream can receive data from many sources (or publishers), and there can be several listeners who subscribe to data from a stream. There are several variations on the possible pub/sub topologies, such as many-to-one, one-to-many, or many-to-many.  Streamr supports all of these.

##Turn the data into action

You can build many kinds of automatic functionality on the Streamr platform. You can refine the incoming real-time data by calculating streaming analytics. You can visualise the data or refined versions of the data as the events flow by. You can communicate with the outside world by sending messages or alerts. You can save the results of real-time computations in new streams. Or you can control external applications and devices. And getting data out a stream is easy, too. If you subscribe to a stream in external web pages and applications, you’ll receive new events or instructions in real-time as soon as they appear in a stream.

To help you do all of the above, there is a built-in library which contains an extensive range of modules. All of ›these modules do useful things on their own, but the real power of Streamr comes for combining simple operations in a sequence as the data flows through the event processing canvas.

Here's a few examples of specific things you can do.

- Refine the data by passing it through different operations. The built-in module library includes arithmetic and logical operations. There are also functions for smoothing, sampling, and aggregating streaming data.
- Chain modules together so that the output from one operation flows as an input to another. This is one way to build arbitrarily complex streaming analytics (and don't worry, you can encapsulate the complexity and reuse the abstracted result).
- Visualise the data by directing it to a charting module on a canvas. The chart shows the new data in real-time as new events arrive or streaming analytics are computed.
- Embed visualisation modules in external web pages by inserting a single line of HTML code. Such embedded visualisations will show live data as long as the underlying canvas remains in live mode.
- Communicate with the outside world by sending text messages or emails when specific conditions are satisfied. You can embed real-time data, refined data, or natural language in the messages.
- Save refined data in another stream. When you do that, the saved data are instantly accessible as new events. The refined data can also be streamed from the platform to external data consumers.
- Control external devices. As an example, you could override manual controls and make a remotely operated drone return to base when it's about to go out of range or running low on battery. Because the control interface is likely to be machine specific, this is one case where you'd be looking at coding a custom module in Java.
- Receive real-time events in external applications. You can subscribe to a stream in web pages and applications, and you’ll receive every event as soon as it’s available. It’s up to you what you do with the event; the possibilities are endless.

##Who is Streamr for?

The short answer is that Streamr is for anyone who wants to create new, interesting, and valuable things on top of real-time data, and wants to do it quickly and with minimum fuss.

Streamr offers value to many different user groups. Domain experts and R&D teams can use Streamr for quick prototyping and exploration before building production level offerings. Streamr gives students, hobbyists, and enthusiasts a fully functional but affordable event processing platform in the cloud.

For corporations and organisations, Streamr is a tool for sharing and teamwork. The real-time data that you collect may be a treasure trove for innovation, but nothing will happen unless the data is available and usable. Streamr can be the spark which makes innovation happen for you: It brings the data into the open, and makes it easy to experiment with ideas for new products and services.

------

