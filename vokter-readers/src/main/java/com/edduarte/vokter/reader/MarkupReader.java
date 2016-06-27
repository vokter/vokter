/*
 * Copyright 2014 Eduardo Duarte
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.edduarte.vokter.reader;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.lang.MutableString;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;

/**
 * A reader class that supports reading documents in the XML format.
 *
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
public class MarkupReader implements com.edduarte.vokter.reader.Reader, NodeVisitor {

    private final StringBuilder accumulator;

    private int width;


    public MarkupReader() {
        this.width = 0;
        this.accumulator = new StringBuilder();
    }


    @Override
    public MutableString readDocumentContents(InputStream documentStream) throws IOException {
        accumulator.delete(0, accumulator.length());

        Document doc = Jsoup.parse(documentStream, null, "");

        NodeTraversor traversal = new NodeTraversor(this);
        traversal.traverse(doc);

        String plainText = accumulator.toString();
        plainText = plainText.replaceAll("<.*?>", "");

        return new MutableString(plainText);
    }


    @Override
    public ImmutableSet<String> getSupportedContentTypes() {
        return ImmutableSet.of(
                MediaType.TEXT_HTML,
                MediaType.TEXT_XML,
                MediaType.APPLICATION_XML,
                MediaType.APPLICATION_ATOM_XML,
                MediaType.APPLICATION_SVG_XML,
                MediaType.APPLICATION_XHTML_XML,
                "application/rdf+xml",
                "application/rss+xml",
                "application/soap+xml",
                "application/rdf+xml",
                "application/xml-dtd");
    }


    @Override
    public void head(Node node, int depth) {
        String name = node.nodeName();
        if (node instanceof TextNode) {
            this.append(((TextNode) node).text());
        } else if (name.equals("li")) {
            this.append("\n * ");
        }
    }


    @Override
    public void tail(Node node, int depth) {
        String name = node.nodeName();
        if (name.equals("br")) {
            this.append("\n");
        } else if (StringUtil.in(name, "p", "h1", "h2", "h3", "h4", "h5")) {
            this.append("\n\n");
        } else if (name.equals("a")) {
            this.append(String.format(" <%s>", node.absUrl("href")));
        }
    }


    private void append(String text) {
        if (text.startsWith("\n")) {
            this.width = 0;
        }

        if (!text.equals(" ") || this.accumulator.length() != 0 &&
                !StringUtil.in(this.accumulator.substring(this.accumulator.length() - 1), " ", "\n")) {
            if (text.length() + this.width > 80) {
                String[] words = text.split("\\s+");

                for (int i = 0; i < words.length; ++i) {
                    String word = words[i];
                    boolean last = i == words.length - 1;
                    if (!last) {
                        word = word + " ";
                    }

                    if (word.length() + this.width > 80) {
                        this.accumulator.append("\n").append(word);
                        this.width = word.length();
                    } else {
                        this.accumulator.append(word);
                        this.width += word.length();
                    }
                }
            } else {
                this.accumulator.append(text);
                this.width += text.length();
            }
        }
    }
}
