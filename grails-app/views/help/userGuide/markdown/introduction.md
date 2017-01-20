# Introduction

Streamr is a cloud-based platform for creating and managing microservices that process events in real-time data streams.  It abstracts the complexity of the underlying stream processing technology and provides a user-friendly front end, saving you time and money along the way.

There’s many things you can do with Streamr. For instance, you’ll be able to monitor real-time industrial processes, ingest and visualise sensory data from IoT devices, calculate streaming analytics, follow and act on high-frequency financial market data, take automatic action when abnormal patterns are detected, extract information from social media traffic, track customer payments, orders, or deliveries, and automate various business processes. If you have a source of real-time data and you want to do useful things with it, Streamr is for you.

Streamr makes it easy to set up digital microservices for handling real-time data. In Streamr, such microservices are called **canvases**. There is a browser-based visual programming interface where you can quickly put a canvas together using modular building blocks. You can also code all or part of a canvas in Java if that's what you prefer. Streamr supports abstraction, and you'll be able to re-use  code and custom modules as your code base grows.

The platform is flexible and fast. Event streams can contain any kind of data in any format. The technology can handle millions of events per second and it copes with a practically unlimited number of live services in parallel. The underlying software runs (by default) in the cloud. Additional computing capacity is deployed automatically when required.

The development environment includes a playback mode where you can simulate the processing logic over days, weeks, months, or years of data over billions of events if needed. When you’re happy, you can launch your canvas live at a click of a button. In the live mode, a Streamr canvas listens to real-time events, processes the data, and takes the required action.

The secret sauce of Streamr is the hidden technology which makes creating and managing real-time microservices easy. Apart from the browser-based user interface and visual programming tools, Streamr takes care of the data flow, event propagation, message queues, load balancing, and other technical issues under the hood. It also manages your live canvases and stores the full event history on your behalf.

## Data live in streams

All real-time data in Streamr is stored in [streams](#streams). A stream is essentially a timestamped sequence of events. Each event may contain more than one field. You can create and work on dozens, hundreds, or thousands of streams. A stream is persistent, identified by a unique ID, and stored in the cloud.

Any kind of real-time data can be stored in a stream. The incoming data may consist of sensory readings of speed, geolocation, orientation, ambient temperature, humidity, and so forth. Or the data may contain social media messages, stock market events, mobile ad impressions, and so on.

There is no limit on the kind, format, or quantity of the data you feed into Streamr. The data may originate from machines on the factory floor, sensors in a smart city, in-house databases or systems, or from commercial streaming data feeds.

There are a few different ways to get data into a stream. Streamr has a simple HTTP API which allows you to push events in JSON format to a stream from any programming language. You can also batch load historical events using Streamr’s CSV loader. If the events originate in a database or in a third-party data feed, talk to us. We'll figure out what kind of adapter is needed.

Streams implement a publish-subscribe paradigm, or pub/sub for short. A stream can receive data from many sources (or publishers), and there can be several listeners who subscribe to a stream. There are several variations on the possible pub/sub topologies, such as many-to-one, one-to-many, or many-to-many.  Streamr supports all of these.

## Turn the data into action

You can build any kind of automatic real-time functionality with Streamr [canvases](#canvases) (i.e. microservices). We make it easy to create and manage your digital workforce. There is an extensive collection of built-in [modules](#modules) as a source of building blocks. You can abstract modules and re-use them later, and you can code custom modules in Java.

Here's a few examples of specific things you can do.

- Refine real-time data by routing it through different modules. The built-in module library includes arithmetic and logical operations. There are also functions for smoothing, sampling, and aggregating streaming data.
- Chain modules together so that the output from one operation flows as an input to another. This is one way to build arbitrarily complex streaming analytics. If you want to keep things tidy, you can encapsulate the complexity and reuse the abstracted result.
- Visualise the data by directing it to a charting module. The chart shows the new data in real-time as new events arrive or streaming analytics are computed.
- Communicate with the outside world by sending text messages or emails when specific conditions are satisfied. You can embed real-time data, refined data, or natural language in the messages.
- Save refined data in another stream. When you do that, the saved data are instantly accessible as new events. The refined data can be streamed from the platform to external data consumers.
- Post events in HTTP format to external RESTful services. There's a wide range of popular and useful services out there, and you can use any one of them easily from Streamr. 
- Control external devices. As an example, you can override manual controls and make a remotely operated drone return to base when it's about to go out of range or running low on battery. Because the control interface is likely to be machine specific, this is one case where you'd be looking at coding a custom module in Java.

Although we sometimes find it hard to admit, there's life outside Streamr. We're not jealous, though, and indeed make it easy to spread the word. Here's what you can do:

- Create dashboards which display live data sourced from Streamr.
- Embed live visualisation elements in external web pages by inserting a single line of HTML code.
- Subscribe to a stream in external web pages and applications. You'll receive every event as soon as it’s available.

## Who is Streamr for?

The short answer is that Streamr is for anyone who wants to create new, interesting, and valuable things on top of real-time data, and wants to do it quickly and with minimum fuss.

Streamr offers value to many different user groups. Domain experts and R&D teams can use Streamr for quick prototyping and exploration before building a production level offering. Streamr gives students, hobbyists, and enthusiasts a fully functional but affordable event processing platform in the cloud.

For corporations and organisations, Streamr is a tool for sharing and teamwork. The real-time data that you collect may be a treasure trove for innovation, but nothing will happen unless the data is available and usable. Streamr can be the spark which makes innovation happen: It brings the data into the open, and makes it easy to experiment with ideas for new products and services.

