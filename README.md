# Argus

[![Build Status](https://travis-ci.org/edduarte/argus.svg?branch=master)](https://travis-ci.org/edduarte/argus)
[![Coverage Status](https://coveralls.io/repos/github/edduarte/argus/badge.svg?branch=master)](https://coveralls.io/github/edduarte/argus?branch=master)
[![https://gitter.im/edduarte/argus](https://badges.gitter.im/edduarte/argus.svg)](https://gitter.im/edduarte/argus?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Argus is a high-performance, scalable web service that uses Quartz Scheduler, DiffMatchPatch and MongoDB to provide web-page monitoring, triggering notifications when specified keywords were either added or removed from a web document.

This service implements a information retrieval system that fetches, indexes and performs queries over web documents on a periodic basis. Difference detection is implemented by comparing occurrences between two snapshots of the same document. Additionally, it supports multi-language stop-word filtering to ignore changes in common grammatical conjunctions or articles, and stemming to detect changes in lexically derived words.

- [Getting Started](#getting-started)
    + [Installation](#installation)
    + [Usage](#usage)
        * [Request: Subscribe](#request-subscribe)
        * [Request: Cancel](#request-cancel)
        * [Response](#response)
        * [Notification: OK](#notification-ok)
        * [Notification: Timeout](#notification-timeout)
- [Dependencies](#dependencies)
- [Architecture](#architecture)
    + [Job Management](#job-management)
        * [Difference Detection](#difference-detection)
        * [Difference Matching](#difference-matching)
        * [Clustering](#clustering)
        * [Scaling](#scaling)
    + [Persistence](#persistence)
    + [Reading](#reading)
    + [Indexing](#indexing)
- [Caveats / Future Work](#caveats--future-work)
- [License](#license)

# Getting Started

## Installation

Argus uses the Reactive (Publish/Subscribe) model, where an additional Client web service with a REST API must be implemented to consume Argus web service.  
<b>An example RESTful web app that interoperates with Argus is [available here](https://github.com/edduarte/argus/tree/master/argus-client/java). Feel free to reuse this code in your own client app.</b>

Once you have a client web service running, follow the steps below:

1. Download and install [MongoDB](https://www.mongodb.org/downloads)

2. Run MongoDB with ``` mongod ```

3. Download the [latest release of Argus core server](https://github.com/edduarte/argus/releases/download/1.4.1/argus-core.zip)

4. Run Argus with ``` java -jar argus-core.jar```
```
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
This will launch a embedded Jetty server with Jersey RESTful framework on 'localhost:9000' (by default). If Argus was successfully deployed, opening the deployed url on a browser should display a landing page with usage instructions.


## Usage

### Request: Subscribe

To watch for content changes in a document, a POST call must be sent to <b>http://localhost:9000/argus/v1/subscribe</b> with the following JSON body:
```javascript
{
    "documentUrl": "http://www.example.com", // the page to be watched (mandatory field)
    "clientUrl": "http://your.site/client-rest-api", // the client web service that will receive detected differences (mandatory field)
    "keywords": // the keywords to watch for (mandatory field)
    [
        "argus", // looks for changes with this word (and lexical variants if stemming is enabled)
        "argus panoptes" // looks for changes with this exact phrase (and lexical variants if stemming is enabled)
    ],
    "interval": 600, // the elapsed duration (in seconds) between page checks (optional field, defaults to 600)
    "ignoreAdded": false, // if 'true', ignore events where the keyword was added to the page (optional field, defaults to 'false')
    "ignoreRemoved": false // if 'true', ignore events where the keyword was removed from the page (optional field, defaults to 'false')
}
```

Note that a subscribe request is uniquely identified by both its document URL and its client URL. This means that a single client can subscribe and receive notifications of multiple documents simultaneously, and a single document can be watched by multiple clients.

### Request: Cancel

To manually cancel a watch job, a POST call must be sent to <b>http://localhost:9000/argus/v1/cancel</b> with the following JSON body:
```javascript
{
    "documentUrl": "http://www.example.com", // the page that was being watched (mandatory field)
    "clientUrl": "http://your.site/client-rest-api" // the client web service (mandatory field)
}
```

### Response

Immediate responses are returned for every subscribe or cancel request, showing if said request was successful or not with the following JSON body:
```javascript
{
    "code": "0" // a number that uniquely identifies this error type (0 when the request was successful)
    "message": "" // reason for the error (empty when the request was successful)
}
```

The error code is useful to convert received responses into custom error messages displayed to the user in the client app.

The following list shows all possible responses:

| Status Code | Body 'code' | Body 'message' |
|-----|------|---------|
| 200 | 0 |  |
| 400 | 1 | The provided document URL is invalid. |
| 400 | 2 | The provided client URL is invalid. |
| 400 | 3 | You need to provide at least one valid keyword. |
| 400 | 4 | At least one difference action ('added' or 'removed') must not be ignored. |
| 409 | 5 | The request conflicts with a currently active watch job, since the provided document URL is already being watched and notified to the provided client URL. |
| 415 | 6 | The request body has an invalid format. |
| 404 | 7 | The specified job to cancel does not exist. |

### Notification: OK

Notifications are REST requests, sent as POSTs, to the provided client URL at any time. The client URL should be implemented to accept the requests below.

When detected differences are matched with keywords, Argus sends notifications to the provided client URL with the following JSON body:
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

### Notification: Timeout

Argus is capable of managing a high number of concurrent watch jobs, and is implemented to save resources and free up database and memory space whenever possible. To this effect, Argus automatically expires jobs when it fails to fetch a web document after 10 consecutive tries. When that happens, the following JSON body is sent:
```javascript
{
    "status": "timeout",
    "url": "http://www.example.com",
    "diffs": []
}
```

# Dependencies

Jersey RESTful framework: https://jersey.java.net  
Jetty Embedded server: http://eclipse.org/jetty/  
Snowball stop-words and stemmers: http://snowball.tartarus.org  
Language Detector: https://github.com/optimaize/language-detector  
Cache2K: http://cache2k.org  
MongoDB Java driver: http://docs.mongodb.org/ecosystem/drivers/java/  
Quartz Scheduler: http://quartz-scheduler.org  
Quartz MongoDB-based store: https://github.com/michaelklishin/quartz-mongodb  
DiffMatchPatch: https://code.google.com/p/google-diff-match-patch/  
Gson: https://code.google.com/p/google-gson/  
Jsonic: http://jsonic.sourceforge.jp  
Jsoup: http://jsoup.org  
Jackson: http://jackson.codehaus.org  
DSI Utilities: http://dsiutils.di.unimi.it  
Commons-IO: http://jackson.codehaus.org  
Commons-Codec: http://commons.apache.org/proper/commons-codec/  
Commons-CLI: http://commons.apache.org/proper/commons-cli/  
Commons-Lang: http://commons.apache.org/proper/commons-lang/  
Commons-Validator: http://commons.apache.org/proper/commons-validator/  

# Architecture

## Job Management

There are 2 types of jobs, concurrently executed and scheduled periodically (using Quartz Scheduler) with an interval of 600 seconds by default: difference detection jobs and difference matching jobs.

### Difference Detection

The detection job is responsible for fetching a new document and comparing it with the previous document, detecting textual differences between the two. To do that, the robust DiffMatchPatch algorithm is used.

### Difference Matching

The matching job is responsible for querying the list of detected differences with specific requested keywords.

Harmonization of keywords-to-differences is performed passing the differences through a Bloom filter, to remove differences that do not have the specified keywords, and a character-by-character comparator on the remaining differences, to ensure that the difference contains any of the keywords.

### Clustering

Since the logic of difference retrieval is spread between two jobs, one that is agnostic of requests and one that is specific to the request and its keywords, Argus reduces workload by scheduling only one difference detection job per watched web-page. For this effect, jobs are grouped into clusters, where its unique identifier is the document URL. Each cluster contains, imperatively, a single scheduled detection job and one or more matching jobs.

### Scaling

Argus was conceived to be able scale and to be future-proof, and to this effect it was implemented to deal with a high number of jobs in terms of batching / persistence and of real-time / concurrency.

The clustering design mentioned above implies that, as the number of clients grows linearly, the number of jobs will grow semi-linearly because the first call for a URL will spawn two jobs and the remaining calls for the same URL will spawn only one.

In terms of orchestration, there are two mechanisms created to reduce redundant resource-consumption, both in memory as well as in the database:

1. if the difference detection job fails to fetch content from a specific URL after 10 consecutive attempts, the entire cluster for that URL is expired. When expiring a cluster, all of the associated client REST APIs receive a time-out call.
2. every time a matching job is cancelled by its client, Argus checks if there are still matching-jobs in its cluster, and if not, the cluster is cleared from the workspace.

## Persistence

Documents, indexing results, found differences are all stored in MongoDB. To avoid multiple bulk operations on the database, every query (document, tokens, occurrences and differences) is covered by memory cache with an expiry duration between 20 seconds and 1 minute.

Persistence of difference-detection jobs and difference-matching jobs is also covered, using a custom MongoDB Job Store by Michael Klishin and Alex Petrov.

## Reading

Argus supports reading of multiple web document formats, like HTML, XML, JSON and Plain-Text, where raw content is converted into a clean string, filtered of non-informative data (e.g. XML tags). Reading logic, which is different for all formats, is covered by Reader classes which follow the plugin paradigm. This means that compiled Reader classes can be added to or removed from the 'argus-readers' folder during runtime, and Argus will be able to dynamically load a suitable Reader class for each document Content-Type.

When Reader classes are instanced, they are stored in on-heap memory cache temporarily (5 seconds). This reduces the elapsed duration of discovering available Reader classes and instancing one for consecutive reads of documents with the same Content-Type.

## Indexing

The string of text that represents the document snapshot that was captured during the Reading phase is passed through a parser that tokenizes, filters stop-words and stems text. For every token found, its occurrences (positional index, starting character index and ending character index) in the document are stored. When a detected difference affected a token, the character indexes of its occurrences can be used to retrieve snippets of text. With this, Argus can instantly show to user, along with the notifications of differences detected, the added text in the new snapshot or the removed text in the previous snapshot.

Because different documents can have different languages, which require specialized stemmers and stop-word filters to be used, the language must be obtained. Unlike the Content-Type, which is often provided as a HTTP header when fetching the document, the Accept-Language is not for the most part. Instead, Argus infers the language from the document content using a language detector algorithm based on Bayesian probabilistic models and N-Grams, developed by Nakatani Shuyo, Fabian Kessler, Francois Roland and Robert Theis.

Stemmer classes and stop-word files, both from the Snowball project, follow the plugin paradigm, similarly to the Reader classes. This means that both can be changed during runtime and Argus will be updated without requiring a restart. Moreover, like the Reader classes, Stemmer classes are cached for 5 seconds before being invalidated to avoid repeated instancing for consecutive stems of documents with the same language (for example, English).

To ensure a concurrent architecture, where multiple parsing calls should be performed in parallel, Argus will instance multiple parsers when deployed and store them in a blocking queue. The number of parsers corresponds to the number of cores available in the machine where Argus was deployed to.

# Caveats / Future Work

- this project has only been used in a production environment for academic projects, and has not been battle-tested or integrated in consumer software;
- client APIs are publicly exposed, and anyone can simulate Argus notifications sent to that API and produce erroneous results on the client app. A secret token should be passed on successful subscribe requests and on further notifications to that client, so that the client can properly identify the received request as Argus';
- stopword filtering and stemming should be done on a request basis, not on a server basis;
- only MongoDB is currently supported, but adding support to MySQL and PostgreSQL should not be very hard to do;
- the architecture should be revised so that intervals cannot be configured and matching jobs are not scheduled but rather invoked once their respective detection job is complete.


<b>More information on the latter caveat:</b> the intervals for difference-matching jobs can be set on the watch request, but difference-detection occurs independently of difference-matching so it can accommodate to all matching jobs for the same document. This means that difference-detection job needs to use an internal interval (420 seconds), and that matching jobs that are configured to run more frequently than that interval will look for matches on the same detected differences two times or more. 

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

