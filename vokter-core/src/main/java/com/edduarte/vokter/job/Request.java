package com.edduarte.vokter.job;

import com.edduarte.vokter.diff.DiffEvent;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class Request {

    public static Request.Add add(String documentUrl,
                                  String clientUrl,
                                  List<String> keywords) {
        return new Add(documentUrl, clientUrl, keywords);
    }


    public static class Add {

        final String documentUrl;

        final String clientId;

        final List<String> keywords;

        String documentContentType = MediaType.TEXT_HTML;

        List<DiffEvent> events = Arrays.asList(DiffEvent.inserted, DiffEvent.deleted);

        boolean filterStopwords = false;

        boolean enableStemming = false;

        boolean ignoreCase = false;

        int snippetOffset = 50;

        int interval = 600;


        public Add(String documentUrl, String clientId, List<String> keywords) {
            this.documentUrl = documentUrl;
            this.clientId = clientId;
            this.keywords = keywords;
        }


        public Add withDocumentContentType(String documentContentType) {
            this.documentContentType = documentContentType;
            return this;
        }


        public Add withEvents(List<DiffEvent> events) {
            this.events = events;
            return this;
        }


        public Add withEvents(DiffEvent... events) {
            this.events = Arrays.asList(events);
            return this;
        }


        public Add filterStopwords() {
            this.filterStopwords = true;
            return this;
        }


        public Add enableStemming() {
            this.enableStemming = true;
            return this;
        }


        public Add ignoreCase() {
            this.ignoreCase = true;
            return this;
        }


        public Add withSnippetOffset(int snippetOffset) {
            this.snippetOffset = snippetOffset;
            return this;
        }


        public Add withInterval(int intervalInSeconds) {
            this.interval = intervalInSeconds;
            return this;
        }


        public Add withInterval(long delay, TimeUnit unit) {
            this.interval = (int) unit.toSeconds(delay);
            return this;
        }
    }
}
