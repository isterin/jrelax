JRelax was born out of necessity of building a scalable and flexible persistence model on top of CouchDB for a startup company.  I looked around and didn't really see what I liked.  Other frameworks have various pieces that are good, but overall my experience wasn't very pleasant.  To top that off, some frameworks didn't keep up with the pace CouchDB was moving at, though giving you a choice of either sticking with an older CouchDB version or digging into the source and patching it.  Because this library powers a real applications which is growing and maturing, this library will be up do date with the latest and greatest.  

Also, the API was build from real patterns which were extracted from real use cases.  I actually created the tests first with stubs in the higher abstraction module to see which type of an API I'd really appreciate using and JRelax was extracted.  The API is based on Java 5 features (mostly lots of generics), so you need at least JSE 5.0 to use/run it.

In the near future I'll set up maven publishing, sorry for now, the releases either have to be downloaded from the downloads section and/or built from source.

Enjoy.


YourKit is kindly supporting open source projects with its full-featured Java Profiler.
YourKit, LLC is the creator of innovative and intelligent tools for profiling
Java and .NET applications. Take a look at YourKit's leading software products:
[YourKit Java Profiler](http://www.yourkit.com/java/profiler/index.jsp)
[YourKit .NET Profiler](http://www.yourkit.com/.net/profiler/index.jsp)