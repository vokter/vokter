# Vokter

[![Build Status](https://travis-ci.org/vokter/vokter.svg?branch=master)](https://travis-ci.org/vokter/vokter)
[![Coverage Status](https://coveralls.io/repos/github/vokter/vokter-core/badge.svg?branch=master)](https://coveralls.io/github/vokter/vokter-core?branch=master)

Vokter is a high-performance, scalable document store that combines [Locality-Sensitive Hashing for K-Shingles](https://github.com/edduarte/near-neighbor-search), [a fork of DiffMatchPatch](https://github.com/edduarte/indexed-diff-match-patch), [Bloom filters](https://github.com/google/guava/wiki/HashingExplained#bloomfilter) and [Quartz jobs](http://www.quartz-scheduler.org) to detect differences in web documents, triggering notifications when specified keywords were either added or removed.

At a basic level, Vokter manages a high number of concurrent scheduler jobs that fetch web documents on a periodic basis and perform difference detection, comparing occurrences between two snapshots of the same document, and difference matching, triggering a listener when a detected difference matches a registered keyword. It optionally supports multi-language stopword filtering, to ignore changes in common words with no important significance, and stemming to detect changes in lexically derived words. Appropriate stopword filtering and stemming algorithms are picked based on the inferred language of the document, using a [N-grams Naïve Bayesian classifier](https://github.com/optimaize/language-detector).

[You can find more info about the architecture and design of Vokter on my blog](https://www.edduarte.com/vokter-a-document-store-that-periodically-checks-for-changes-in-web-documents/).


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

