# Argus

[![Build Status](https://travis-ci.org/edduarte/argus.svg?branch=master)](https://travis-ci.org/edduarte/argus)
[![Coverage Status](https://img.shields.io/coveralls/edduarte/argus.svg)](https://coveralls.io/r/edduarte/argus)
[![GitHub release](https://img.shields.io/github/release/edduarte/argus.svg)](https://github.com/edduarte/argus/releases)

Argus is a high-performance, scalable web service that provides web-page monitoring, triggering notifications when specified keywords were either added or removed from a web document. It supports multi-language parsing and supports reading the most popular web document formats like HTML, JSON, XML, Plain-text and other variations of these.

This service implements a information retrieval system that fetches, indexes and performs queries over web documents on a periodic basis. Difference detection is implemented by comparing occurrences between two snapshots of the same document.

## Installation

Argus uses the Publish/Subscribe model, where <u>**an additional Client web service with a REST API must be implemented to request and consume Argus web service**</u>. This allows for an asynchronous operation, where the client does not have to lock its threads while waiting for page changes notifications nor implement a busy-waiting condition checking of Argus status.

Once you have a client web service running, follow the steps below:

1. Download [MongoDB](https://www.mongodb.org/downloads)
2. Run MongoDB with
```
mongod
```
3. Download the [latest release](https://github.com/edduarte/argus/releases) of Argus
4. Run Argus with
```
java -jar argus-core-1.3.0.jar

Optional arguments:
 -h,--help               Shows this help prompt.
 -p,--port <arg>         Core server port. Defaults to 9000.
 -dbh,--db-host <arg>    Database host. Defaults to localhost.
 -dbp,--db-port <arg>    Database port. Defaults to 27017.
 -case,--preserve-case   Keyword matching with case sensitivity.
 -stop,--stopwords       Keyword matching with stopword filtering.
 -stem,--stemming        Keyword matching with stemming (lexical
                         variants).
 -t,--threads <arg>      Number of threads to be used for computation and
                         indexing processes. Defaults to the number of
                         available cores.
```
5. If Argus was successfully deployed, opening 'localhost:9000' will launch a landing page with usage instructions.



## Usage

To watch for content, a POST call must be sent to ** http://localhost:9000/argus/v1/watch ** with the following JSON body:
```javascript
{
    "documentUrl": "http://www.example.com", // the page to be watched (mandatory field)
    "receiverUrl": "http://your.site/client-rest-api", // the client web service that will receive detected differences (mandatory field)
    "keywords": // the keywords to watch for (mandatory field)
    [
        "argus", // looks for changes with this word (if stemming is enabled, looks for changes in lexical variants)
        "panoptes", // looks for changes with this word (if stemming is enabled, looks for changes in lexical variants)
        "argus panoptes" // looks for an exact match of this phrase (if stemming is enabled, looks for changes in lexical variants)
    ],
    "interval": 600, // the elapsed duration (in seconds) between page checks (optional field, defaults to 600)
    "ignoreAdded": false, // if 'true', ignore events where the keyword was added to the page (optional field, defaults to 'false')
    "ignoreRemoved": false // if 'true', ignore events where the keyword was removed from the page (optional field, defaults to 'false')
}
```

When detected differences are matched with keywords, notifications are asynchronously sent to the provided response URL in POST with the following JSON body:
```javascript
{
    "status": "ok",
    "url": "http://www.example.com",
    "diffs": [
        {
            "action": "added",
            "keyword": "argus",
            "snippet": "In the 5th century and later, Argus' wakeful alertness ..."
        },
        {
            "action": "removed",
            "keyword": "argus",
            "snippet": "... sacrifice of Argus liberated Io and allowed ..."
        }
    ]
}
```

Argus is capable of managing a high number of concurrent watch jobs, as it is implemented to save as much resources as possible and free up database and memory space whenever possible. One method of resource freeing is to automatically timeout watch jobs when it fails to fetch a web document after 10 consecutive tries. When that happens, the following JSON body is sent to the response URL:
```javascript
{
    "status": "timeout",
    "url": "http://www.example.com",
    "diffs": []
}
```

Finally, to manually cancel a watch job, a POST call must be sent to ** http://localhost:9000/argus/v1/cancel ** with the following JSON body:
```javascript
{
    "documentUrl": "http://www.example.com", // the page that was being watched (mandatory field)
    "receiverUrl": "http://your.site/client-rest-api" // the client web service (mandatory field)
}
```

Immediate responses are returned for every watch or cancel request, showing if the request was successful or not with the following JSON body:
```javascript
{
    "code": "ok"/"error",
    "message": "..."
}
```


## Libraries used

Jersey RESTful framework: https://jersey.java.net  
Jetty Embedded server: http://eclipse.org/jetty/  
Genia Parser: http://people.ict.usc.edu/~sagae/parser/gdep/  
LingPipe: http://alias-i.com/lingpipe/  
Snowball stopwords and stemmers: http://snowball.tartarus.org  
Language Detector: https://github.com/optimaize/language-detector  
Cache2K: http://cache2k.org  
Quartz Scheduler: http://quartz-scheduler.org  
MongoDB Java driver: http://docs.mongodb.org/ecosystem/drivers/java/  
DiffMatchPatch: https://code.google.com/p/google-diff-match-patch/  
Gson: https://code.google.com/p/google-gson/  
Jsonic: http://jsonic.sourceforge.jp  
jsoup: http://jsoup.org  
Jackson: http://jackson.codehaus.org  
DSI Utilities: http://dsiutils.di.unimi.it  
Commons-IO: http://jackson.codehaus.org  
Commons-Codec: http://commons.apache.org/proper/commons-codec/  
Commons-CLI: http://commons.apache.org/proper/commons-cli/  
Commons-Lang: http://commons.apache.org/proper/commons-lang/


## License

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

