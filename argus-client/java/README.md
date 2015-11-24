# Argus Client in Java

This is an example client, in Java, that interoperates with Argus using REST calls. It implements three simple REST methods, a mandatory POST ("/rest/notification") that receives Argus detected differences for watched pages, and 2 optional GETs that allow the user to interact with Argus using the browser's address bar. Notifications are simply printed on the console when received.

- [Getting Started](#getting-started)
    + [Installation](#installation)
        * [Running with Apache Tomcat](#running-with-apache-tomcat)
        * [Running with Jetty](#running-with-jetty)
    + [Usage](#usage)
        * [Subscribe](#subscribe)
        * [Cancel](#cancel)
- [License](#license)

# Getting Started

## Installation

<b>If you already have a running Argus server, skip to step 5.</b>

1. Download and install [MongoDB](https://www.mongodb.org/downloads)

2. Run MongoDB with ``` mongod ```

3. Download the [latest release of Argus core server](https://github.com/edduarte/argus/releases/download/1.4.1/argus-core.zip)

4. Run Argus server with ``` java -jar argus-core.jar ``` ([more information on running and using the core service](https://github.com/edduarte/argus#installation))

5. Download the [latest war of this Java client](https://github.com/edduarte/argus/releases/download/1.4.1/argus-example-app-java.zip)

    ### Running with Apache Tomcat

    1. Download and install [Apache Tomcat](https://tomcat.apache.org)
    2. Place the downloaded war file in ``` tomcat-folder/webapps ```
    3. Run Tomcat with ``` tomcat-folder/bin/startup.sh ```

    ### Running with Jetty

    1. Download and install [Jetty](http://www.eclipse.org/jetty/)
    2. Place the downloaded war file in ``` jetty-folder/webapps ```
    3. Run Jetty with ``` java -jar jetty-folder/start.jar ```

Note that if you change the default host of the deployed web app container or of the deployed Argus server, you will need to change the first few Strings in the ClientResource.java file and pack a new war.

## Usage

### Subscribe

Load the URL below on your browser or curl, replacing ENCODED_URL with an encoded URL and KEYWORDS with an encoded string of multiple keywords, separated by commas:
```
http://localhost:8080/rest/watch?url=ENCODED_URL&keywords=KEYWORDS
```

For example, the following URL will subscribe the client to receive notifications when changes to the words 'a' and 'the' are found in the URL bbc.com:
```
http://localhost:8080/rest/watch?url=http%3A%2F%2Fbbc.com&keywords=a,the
```

### Cancel

Load the URL below on your browser or curl, replacing ENCODED_URL with an encoded URL:
```
http://localhost:8080/rest/cancel?url=ENCODED_URL
```

For example, the following URL will cancel the watch job launched by this client for the URL bbc.com:
```
http://localhost:8080/rest/cancel?url=http%3A%2F%2Fbbc.com
```


# License

    Copyright 2015 Ed Duarte

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

