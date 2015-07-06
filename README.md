# Argus

[![Build Status](https://travis-ci.org/edduarte/argus.svg?branch=master)](https://travis-ci.org/edduarte/argus)
[![Coverage Status](https://img.shields.io/coveralls/edduarte/argus.svg)](https://coveralls.io/r/edduarte/argus)
[![GitHub version](https://badge.fury.io/gh/edduarte%2Fargus.svg)](http://badge.fury.io/gh/edduarte%2Fargus)

Argus is high-performance, scalable web service that provides web-page monitoring, triggering notifications when specified keywords were either added or removed from a web document. It supports multi-language parsing and supports reading the most popular web document formats like HTML, JSON, XML, Plain-text and other variations of these.

This service implements a information retrieval system that fetches, indexes and performs queries over web documents on a periodic basis. Difference detection is implemented by comparing occurrences between two snapshots of the same document.


## Installation

TODO


## Usage

To set Argus to watch for content, a POST call must be sent to **http://localhost:8080/rest/watch** with the following JSON message:
```json
{
    "documentUrl": "http://www.example.com/url/to/watch",
    "responseUrl": "http://your.site/async-response-receiver",
    "keywords": [
        "single-word-keyword",
        "keyword with multiple words"
    ],
    "interval": 600
}
```
The example above sets Argus to watch the website "example.com/url/to/watch" every 600 seconds and detect if any of the provided keywords was either added or deleted. When detected differences are matched with keywords, notifications are asynchronously sent to the provided response URL in POST with the following JSON message:
```json
{
    "status": "ok",
    "url": "http://www.example.com/url/to/watch",
    "diffs": [
        {
            "action": "inserted",
            "keyword": "keyword-that-matched-this-difference",
            "snippet": "snippet of what was added to the document"
        },
        {
            "action": "deleted",
            "keyword": "keyword-that-matched-this-difference",
            "snippet": "snippet of what was removed from the document"
        }
    ]
}
```
Argus is capable of managing a high number of concurrent watch jobs, as it is implemented to save as much resources as possible and free up database and memory space whenever possible. One method of resource freeing is to automatically timeout watch jobs when it fails to fetch a web document after 10 consecutive tries. When that happens, the following JSON message is sent to the response URL:
```json
{
    "status": "timeout",
    "url": "http://www.example.com/url/to/watch",
    "diffs": []
}
```
Finally, to manually cancel a watch job, a POST call must be sent to **http://localhost:8080/rest/cancel** with the following JSON message:
```json
{
    "documentUrl": "http://www.example.com/url/to/cancel",
    "responseUrl": "http://your.site/async-response-receiver"
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

