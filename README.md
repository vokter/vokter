# Vokter

[![Build Status](https://travis-ci.org/vokter/vokter.svg?branch=master)](https://travis-ci.org/vokter/vokter)
[![Coverage Status](https://coveralls.io/repos/github/vokter/vokter-core/badge.svg?branch=master)](https://coveralls.io/github/vokter/vokter-core?branch=master)

Vokter is a high-performance, scalable document store that combines [Locality-Sensitive Hashing for K-Shingles](https://github.com/edduarte/near-neighbor-search), [a fork of DiffMatchPatch](https://github.com/edduarte/indexed-diff-match-patch), [Bloom filters](https://github.com/google/guava/wiki/HashingExplained#bloomfilter) and [Quartz jobs](http://www.quartz-scheduler.org) to detect differences in web documents, triggering notifications when specified keywords were either added or removed.

At a basic level, Vokter manages a high number of concurrent scheduler jobs that fetch web documents on a periodic basis and perform difference detection, comparing occurrences between two snapshots of the same document, and difference matching, triggering a listener when a detected difference matches a registered keyword. It optionally supports multi-language stopword filtering, to ignore changes in common words with no important significance, and stemming to detect changes in lexically derived words. Appropriate stopword filtering and stemming algorithms are picked based on the inferred language of the document, using a [N-grams Naïve Bayesian classifier](https://github.com/optimaize/language-detector).

[You can find more info about the architecture and design of Vokter on my blog](https://www.edduarte.com/vokter-a-java-library-that-detects-and-notifies-changes-in-web-documents/).


## Installation

Vokter uses the Reactive (Publish/Subscribe) model, where an additional Client web service with a REST API must be implemented to consume Vokter web service.  
<b>An example RESTful web app that interoperates with Vokter is [available here](https://github.com/vokter/vokter-client-java). Feel free to reuse this code in your own client app.</b>

1. Download the [latest release](https://github.com/vokter/vokter/releases/latest) of the core server

2. Run Vokter with ``` java -jar vokter-core.jar```
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
This will launch a embedded Jetty server with Jersey RESTful framework on 'localhost:9000' (by default). If Vokter was successfully deployed, opening this URL on a browser should display a landing page with usage instructions.

**Create a new watch job and attach a client REST endpoint**

POST http://localhost:9000/vokter/v1/subscribe
Payload:
```javascript
{
    "documentUrl": "http://www.example.com", // the page to be watched (mandatory field)
    "clientUrl": "http://your.site/client-rest-api", // the client web service that will receive detected differences (mandatory field)
    "keywords": // the keywords to watch for (mandatory field)
    [
        "vokter", // looks for changes with this word (and lexical variants if stemming is enabled)
        "vokter panoptes" // looks for changes with this exact phrase (and lexical variants if stemming is enabled)
    ],
    "interval": 600, // the elapsed duration (in seconds) between page checks (optional field, defaults to 600)
    "ignoreAdded": false, // if 'true', ignore events where the keyword was added to the page (optional field, defaults to 'false')
    "ignoreRemoved": false // if 'true', ignore events where the keyword was removed from the page (optional field, defaults to 'false')
}
```

Note that a subscribe request is uniquely identified by both its document URL and its client URL. This means that the same client can subscribe and receive notifications of multiple documents simultaneously, and the same document can be watched by multiple clients.

---

**Manually cancel a watch job**

POST http://localhost:9000/vokter/v1/cancel
Payload:
```javascript
{
    "documentUrl": "http://www.example.com", // the page that was being watched (mandatory field)
    "clientUrl": "http://your.site/client-rest-api" // the client web service (mandatory field)
}
```

---

**Both of the calls above return the following JSON body**
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
```json
{
    "status": "timeout",
    "url": "http://www.example.com",
    "diffs": []
}
```

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

