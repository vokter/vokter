# Vokter REST Server

[![Build Status](https://travis-ci.org/vokter/vokter-server.svg?branch=master)](https://travis-ci.org/vokter/vokter-server)
[![Coverage Status](https://coveralls.io/repos/github/vokter/vokter-server/badge.svg?branch=master)](https://coveralls.io/github/vokter/vokter-server?branch=master)

Vokter is a platform that provides web-page monitoring, triggering notifications when specified keywords were either added or removed from a web document.

Vokter Server is a high-performant REST service that encapsulates [Vokter Core](https://github.com/vokter/vokter-core) in a Dropwizard app while using multi-node cluster persistence of data and jobs in Hazelcast (for quick cached retrieval) and Cassandra.

You can test it out at http://vokter.herokuapp.com

Check [Vokter Core](https://github.com/vokter/vokter-core) for a standalone library version of this, which you can use in your own Java projects.

- [Getting Started](#getting-started)
    + [Installation](#installation)
    + [Usage](#usage)
    + [Notifications](#notifications)
        * [OK](#ok)
        * [Timeout](#timeout)
- [Architecture](#architecture)
    + [Job Management](#job-management)
        * [Difference Detection](#difference-detection)
        * [Difference Matching](#difference-matching)
        * [Clustering](#clustering)
        * [Scaling](#scaling)
    + [Persistence](#persistence)
    + [Reading](#reading)
    + [Indexing](#indexing)
- [License](#license)

# Getting Started

## Installation

Vokter Server uses the Reactive (Publish/Subscribe) model, where an additional Client web service with a REST API must be implemented to consume Vokter web service.  
<b>An example RESTful web app that interoperates with Vokter is [available here](https://github.com/vokter/vokter-client-jersey2). Feel free to reuse this code in your own client app.</b>

Once you have a client web service running, follow the steps below:

1. Download and install [MongoDB](https://www.mongodb.org/downloads)

2. Run MongoDB with ``` mongod ```

3. Download the [latest release of Vokter server](https://github.com/vokter/vokter-server/releases/)

4. Run Vokter with ``` java -jar vokter-server.jar server```
```
Optional arguments:
 -h,--help               Shows this help prompt.
 -p,--port <arg>         Core server port. Defaults to 9000.
 -dbh,--db-host <arg>    Database host. Defaults to localhost.
 -dbp,--db-port <arg>    Database port. Defaults to 27017.
 -ic,--ignore-case       Flag that forces the document to be in lower-case,
                         so that during difference matching every match
                         will be case insensitive (regardless of the user 
                         setting "ignoreCase" as false in his request)
 -stop,--stopwords       Flag that enables filtering of stopwords during
                         k-shingling of documents on difference detection 
                         jobs. This composes a trade-off between stopping all 
                         matching of common words that the user might have 
                         specified as his desired keywords on purpose, and 
                         reducing thetotal number of jobs triggered. This 
                         option is only used on shingling / LSH, and has no 
                         effect on the user's setting \"filterStopwords\", 
                         since that one concerns keyword matching and not 
                         detection.
```
This will launch a embedded Jetty server with Jersey RESTful framework on 'localhost:8080' (by default). If Vokter was successfully deployed, opening the deployed url on a browser should display a landing page with usage instructions.


## Usage

All usage documentation is available at 'localhost:8080' when the server is deployed (using Swagger and Swagger-UI). You can also check it on the demo app: http://vokter.herokuapp.com

<b>Watch for content changes in a document</b>

POST http://localhost:8080/vokter/v2
Payload:  
```javascript
{
    "documentUrl": "http://www.example.com", // the page to be watched (mandatory field)
    "clientUrl": "http://your.site/client-rest-api", // the client web service that will receive detected differences (mandatory field)
    "keywords": // the keywords to watch for (mandatory field)
    [
        "argus", // looks for changes with this word (and lexical variants if stemming is enabled)
        "zeus larissaios" // looks for changes with this exact phrase (both words must be in the diff)
    ],
    "interval": 600, // the elapsed duration in seconds between page checks (optional field, defaults to 600)
    "events": ["added", "removed"] // (optional field, included both events by default)
}
```

Note that a subscribe request is uniquely identified by both its document URL and its client URL. This means that the same client can subscribe and receive notifications of multiple documents simultaneously, and the same document can be watched by multiple clients.

---

<b>Manually cancel a watch job</b>

POST http://localhost:9000/vokter/v1/cancel  
Payload:  
```javascript
{
    "documentUrl": "http://www.example.com", // the page that was being watched (mandatory field)
    "clientUrl": "http://your.site/client-rest-api" // the client web service (mandatory field)
}
```

---

Both of the calls above return the following JSON body:
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
| 400 | 4 | At least one difference event ('added' or 'removed') must not be ignored. |
| 409 | 5 | The request conflicts with an existing active job, since the provided document URL is already being watched and notified to the provided client URL. |
| 415 | 6 | The request body has an invalid format. |
| 404 | 7 | The specified job to cancel does not exist. |

## Notifications

Notifications are REST requests, sent as POSTs, to the provided client URL at any time. The client URL should be implemented to accept the two requests below.

### Differences found

When detected differences are matched with keywords, Vokter sends notifications to the provided client URL with the following JSON body:
```javascript
{
    "status": "ok",
    "url": "http://www.example.com",
    "diffs": [
        {
            "event": "added",
            "keyword": "argus",
            "text": "Argus' wakeful", // the exact text that was added which matched one of the user's keywords
            "snippet": "In the 5th century and later, Argus' wakeful alertness ..." // a bigger text snippet of the difference in context
        },
        {
            "event": "removed",
            "keyword": "argus",
            "text": "the sacrifice of Argus ", // the exact text that was removed which matched one of the user's keywords
            "snippet": "... sacrifice of Argus liberated Io and allowed ..." // a bigger text snippet of the difference in context
        }
    ]
}
```

### Timeout

Vokter is capable of managing a high number of concurrent watch jobs, and is implemented to save resources and free up database and memory space whenever possible. To this effect, Vokter automatically expires jobs when it fails to fetch a web document after 10 consecutive tries. When that happens, the following JSON body is sent:
```javascript
{
    "status": "timeout",
    "url": "http://www.example.com",
    "diffs": []
}
```

# Architecture

## Job Management

There are two types of jobs, concurrently executed and scheduled periodically (using Quartz Scheduler): difference detection jobs and difference matching jobs.

### Difference Detection

The detection job is responsible for fetching a new document and comparing it with the previous document, detecting textual differences between the two. To do that, the robust DiffMatchPatch algorithm is used.

### Difference Matching

The matching job is responsible for querying the list of detected differences with specific requested keywords.

Harmonization of keywords-to-differences is performed passing the differences through a Bloom filter, to remove differences that do not have the specified keywords, and a character-by-character comparator on the remaining differences, to ensure that the difference contains any of the keywords.

### Clustering

Since the logic of difference retrieval is spread between two jobs, one that is agnostic of requests and one that is specific to the request and its keywords, Vokter reduces workload by scheduling only one difference detection job per watched web-page. For this, jobs are grouped into clusters, where its unique identifier is the document URL. In other words each cluster imperatively contains a single scheduled detection job and one or more matching jobs.

### Scaling

Vokter was conceived to be able scale and to be future-proof, and to this effect it was implemented to deal with a high number of jobs in terms of batching and persistence.

The clustering design mentioned above implies that, as the number of clients grows linearly, the detection logic remains independent of the clients and is only executed at a given set of triggers.

In terms of orchestration, there are two mechanisms created to reduce redundant resource-consumption, both in memory as well as in the database:

1. if the difference detection job fails to fetch content from a specific URL after 10 consecutive attempts, the entire cluster for that URL is expired. When expiring a cluster, all of the associated client REST APIs receive a time-out call.
2. every time a matching job is cancelled by its client, Vokter checks if there are still matching-jobs in its cluster, and if not, the cluster is cleared from the workspace.

## Persistence

Documents, indexing results, found differences are all stored in MongoDB. To avoid multiple bulk operations on the database, every query (document, tokens, occurrences and differences) is covered by memory cache with an expiry duration between 20 seconds and 1 minute.

Persistence of difference-detection jobs and difference-matching jobs is also covered, using a custom MongoDB Job Store by Michael Klishin and Alex Petrov.

## OSGi-based architecture

Vokter support for reading of a given MediaType is provided by Reader modules, where raw content is converted into a clean string filtered of non-informative data (e.g. XML tags). These modules are loaded in a OSGi-based architecture, meaning that compiled Reader classes can be loaded or unloaded without requiring a reboot. When needed, usually when reading a new document or snapshot, Vokter will query for available Readers by Content-Type supported.

This same plugin-like architecture is implemented for Stemmer modules. Using a language detection prediction model, Vokter determines the most probable language of the document and queries on-demand for available Stemmers by language supported.

## Indexing

The string of text that represents the document snapshot that was captured during the Reading phase is passed through a parser that tokenizes, filters stop-words and stems text. For every token found, its occurrences (positional index, starting character index and ending character index) in the document are stored. When a detected difference affected a token, the character indexes of its occurrences can be used to retrieve snippets of text. With this, Vokter can instantly show to user, along with the notifications of differences detected, the added text in the new snapshot or the removed text in the previous snapshot.

Because different documents can have different languages, which require specialized stemmers and stop-word filters to be used, the language must be obtained. Unlike the Content-Type, which is often provided as a HTTP header when fetching the document, the Accept-Language is not for the most part. Instead, Vokter infers the language from the document content using a language detector algorithm based on Bayesian probabilistic models and N-Grams, developed by Nakatani Shuyo, Fabian Kessler, Francois Roland and Robert Theis.

Stemmer classes and stop-word files, both from the Snowball project, follow the plugin paradigm, similarly to the Reader classes. This means that both can be changed during runtime and Vokter will be updated without requiring a restart. Moreover, like the Reader classes, Stemmer classes are cached for 5 seconds before being invalidated to avoid repeated instancing for consecutive stems of documents with the same language (for example, English).

To ensure a concurrent architecture, where multiple parsing calls should be performed in parallel, Vokter will instance multiple parsers when deployed and store them in a blocking queue. The number of parsers corresponds to the number of cores available in the machine where Vokter was deployed to.

# License

    Copyright 2015 Eduardo Duarte

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

