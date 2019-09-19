# UrEvent: Event Search and Recommendation
## Overview

Many of us may wish to participate fun events on weekends, such as watching a soccer game or going to a concert. However, we may not know which events are available in the near future, their locations or where to purchase tickets.
UrEvent is a full stack web service that aims to use personalization to improve event search and recommendation.

**Front end**: Interactive web page for users to search events and purchase tickets utilizing AJAX technology

**Back end**: Java servlets with RESTful APIs based on Apache Tomcat Server plus MySQL RDMS and live on Amazon EC2

**The Big Picture**:  
![framework](https://github.com/ZjWeb200/UrEvent/blob/master/big_picture.png)

**The Inner Logic**:
![architecture](https://user-images.githubusercontent.com/31113955/40943643-c04315c0-6806-11e8-80bc-ba6bc700f0cc.png)

## How does it Work
At the very beginning, a user needs to sign up an account. Once signed up, the user may login and start exploring.
The web service searches **nearby events** based on user's id and geo location. If a user has marked an event as **My favorite**, the system would also **recommend events** (similar events to the user's favorite) to the user. This content-based recommendation makes the website personalized to each user. If the user is no longer interested in a favorited event, he or she can simply mark it off, and the recommendations from the backend would change accordingly.
Should a user finds an interesting event nearby, he or she may directly click the URL link on the frontend web-page to **purchase tickets and pick seats**.

**The Frontend Web-page**:
![frontend](https://github.com/ZjWeb200/UrEvent/blob/master/frontend.JPG)

At the backend, since it deploys RESTful APIs, rpc (remote procedure call) operations are directly based on HTTP methods. The Tomcat server uses HTTP URL to indicate which service (corresponding Java servlet) and data (corresponding table/content from MySQL RDMS) a client requests/posts. TicketMaster API connecting to the web server is used as a "pool" for event searching and tickets purchasing.

**Website link**:
http://54.219.160.64/UrEvent/
