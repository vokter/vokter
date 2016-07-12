# Vokter

[![Build Status](https://travis-ci.org/vokter/vokter-core.svg?branch=master)](https://travis-ci.org/vokter/vokter-core)
[![Coverage Status](https://coveralls.io/repos/github/vokter/vokter-core/badge.svg?branch=master)](https://coveralls.io/github/vokter/vokter-core?branch=master)

Vokter is a high-performance, scalable framework that combines [Locality-Sensitive Hashing for K-Shingles](https://github.com/edduarte/near-neighbor-search), [a fork of DiffMatchPatch](https://github.com/edduarte/indexed-diff-match-patch), [Bloom filters](https://github.com/google/guava/wiki/HashingExplained#bloomfilter) and [Quartz jobs](http://www.quartz-scheduler.org) to detect differences in web documents, triggering notifications when specified keywords were either added or removed.

At a basic level, Vokter manages a high number of concurrent jobs that fetch web documents on a periodic basis and perform difference detection, comparing occurrences between two snapshots of the same document, and difference matching, triggering a listener when a detected difference matches a registered keyword. It optionally supports multi-language stopword filtering, to ignore changes in common words with no important significance, and stemming to detect changes in lexically derived words. Appropriate stopword filtering and stemming algorithms are picked based on the inferred language of the document, using a [N-grams Naïve Bayesian classifier](https://github.com/optimaize/language-detector).

- [Getting Started](#getting-started)
    + [Installation](#installation)
    + [Usage](#usage)
    + [Notifications](#notifications)
        * [OK](#ok)
        * [Timeout](#timeout)
- [Architecture](#architecture)
    + [Job Management](#job-management)
    + [Scaling](#scaling)
    + [Persistence](#persistence)
    + [Reading](#reading)
    + [Indexing](#indexing)
- [Caveats / Future Work](#caveats-future-work)
- [License](#license)

## Usage

### Maven
```
<dependency>
    <groupId>com.edduarte</groupId>
    <artifactId>vokter-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle
```
dependencies {
    compile 'com.edduarte:vokter-core:1.0.0'
}
```

Create a job manager with a unique name (yes, you can have multiple job managers using the same persistence collections):
```java
import com.edduarte.vokter.job.JobManager;

JobManager manager = JobManager.create("vokter_manager_1", new JobManagerHandler() {

    @Override
    public boolean onNotification(String documentUrl, String documentContentType, Session session, Set<Match> diffs) {
        // when differences are found for the specified Session
    }

    @Override
    public boolean onTimeout(String documentUrl, String documentContentType, Session session) {
        // when the job timed-out due to failing to load the document after 10 attempts
    }
});
```

And add a job, detecting differences every 10 minutes:
```java
String documentUrl = "http://www.example.com"; // the page to be watched (mandatory field)
Session clientSession = new Session("client_id"); // the unique identification of the client in this job manager
List<String> keywords = Arrays.asList(
    "argus", // looks for changes with this word (and lexical variants if stemming is enabled)
    "zeus larissaios" // looks for changes with this exact phrase (both words must be in the diff)
);
manager.add(RequestBuilder.add(documentUrl, session, keywords));
```

You can optionally enable stemming, filter stopwords and ignore casing when attempting to match differences with keywords, so for example, the keyword "House of the Gods" would match a difference with the text "god house":

```java
manager.add(RequestBuilder.add(documentUrl, session, keywords)
                .enableStemming()
                .filterStopwords()
                .ignoreCase());
```

You can also set the interval:

```java
manager.add(RequestBuilder.add(documentUrl, session, keywords)
                .withInterval(15, TimeUnit.MINUTE));
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

The detection job is responsible for fetching a new document and comparing it with the previous document, detecting textual differences between the two. To do that, the robust DiffMatchPatch algorithm is used.

The matching job is responsible for querying the list of detected differences with specific requested keywords.

Harmonization of keywords-to-differences is performed passing the differences through a Bloom filter, to remove differences that do not have the specified keywords, and a character-by-character comparator on the remaining differences, to ensure that the difference contains any of the keywords.

Since the logic of difference retrieval is spread between two jobs, one that is agnostic of requests and one that is specific to the request and its keywords, Vokter reduces workload by scheduling only one difference detection job per watched web-page. For this, jobs are grouped into clusters, where its unique identifier is the document URL. In other words each cluster imperatively contains a single scheduled detection job and one or more matching jobs.

## Scaling

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

To ensure a concurrent architecture, where multiple parsing calls should be performed in parallel, Vokter will instance multiple parsers when deployed and store them in a blocking queue. The number of parsers corresponds to the number of cores available in the machine where Vokter was deployed to.

# Caveats / Future Work

Despite every part of its architecture having been optimized to accommodate to a massive amount of parallel tasks, Vokter has only been used in a production environment for academic projects and has yet to be battle-tested in high-usage consumer software. If you're using Vokter in your projects, let me know :)

Additionally, there is some room for improvement:

## Web crawling

One way to improve user experience is by integrating web crawling in Reader modules, allowing users to set their visit policy (e.g. number of nested documents accessed). Within the current architecture where there is a unique detection job per document, detection jobs must be organized by link hierarchy order: if job 1 watches A and job 2 watches B, and if A has a link to B, then job 2 can be canceled and job 1 should trigger matchers of 1 and 2, where matchers of 2 only match differences found in document B. This implies a extremely optimized algorithm that has the potential of reducing the total number of simultaneous jobs significantly!

## Fault-tolerance and timeout in matching jobs

Currently only detection jobs can be timed-out after failing to load a new snapshot of the document after a specified number of attempts. However, sending a response to the client can fail too, and currently there is no fault-tolerance implemented for this. If a client fails to receive the data, maybe because the client itself has been shutdown without canceling its jobs from Vokter, then a potential high number of active detection and matching jobs are unnecessary.


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

