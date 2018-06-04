# Jupiter: Event Search and Recommendation System
## Overview

Many of us may wish to participate fun events on weekends, such as watching a soccer game or going to a concert. However, we may not know which events are available in the near future, their locations or where to purchase tickets.
Jupiter is a full stack system that aims to use personalization to improve event search and recommendation.

**Front end**: Interactive web page for users to search events and purchase tickets utilizing AJAX technology

**Back end**: RESTful web service applying Apache Tomcat Web Server with MySQL RDMS and live on Amazon EC2

**The Framework**:  
![framework](https://user-images.githubusercontent.com/31113955/40943470-166d7324-6806-11e8-84dd-4e16f31614f0.png)

**The Architecture**:
![architecture](https://user-images.githubusercontent.com/31113955/40943643-c04315c0-6806-11e8-80bc-ba6bc700f0cc.png)

## How does it Work
The system outputs *nearby events* based on user's id and geo location. If a user has marked an event as *My favorite*, the system would *recommend events* (similar events as the user's favorite) to the user. This content-based recommendation makes the website personalized to each user. If the user is no longer interested in a favorite event, he or she can simply mark it off, and the recommendations from the system would change accordingly.
Should a user finds an interesting event nearby, he or she may directly click the URL link on Jupiter web-page to *purchase tickets and pick seats*.

**The Frontend Web-page**:
![frontend](https://user-images.githubusercontent.com/31113955/40945762-2760dc30-680f-11e8-836a-91ff9354ad07.png)

At the backend, since it's a RESTful web service, rpc operations are directly based on HTTP methods. The Tomcat server uses HTTP URL to indicate which service (corresponding Java servlet) and data (corresponding table/content from MySQL RDMS) a client requests/posts. TicketMaster API connecting to the web server is used as a "pool" for event searching and tickets purchasing.
