package argus;

import argus.reader.Reader;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.lang.MutableString;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import java.io.IOException;
import java.io.InputStream;

/**
 * A reader class that supports reading documents in the XML format.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 2.0
 */
public class HtmlXmlReader implements Reader {

    @Override
    public MutableString readDocumentContents(InputStream documentStream) throws IOException {

        Document doc = Jsoup.parse(documentStream, null, "");

        FormattingVisitor formatter = new FormattingVisitor();
        NodeTraversor traversal = new NodeTraversor(formatter);
        traversal.traverse(doc);

        String plainText = formatter.toString();
        plainText = plainText.replaceAll("<.*?>", "");

        return new MutableString(plainText);

//        LineIterator it = IOUtils.lineIterator(new InputStreamReader(documentStream));
//        MutableString sb = new MutableString();
//
//        while (it.hasNext()) {
//            String processedLine = it.next();
//
//            processedLine = processedLine.replaceAll("</.*?>", " ");
//            processedLine = processedLine.replaceAll("<.*?>", " ");
//            processedLine = processedLine.replaceAll("<.*?/>", " ");
//
//            processedLine = processedLine.trim();
//
//            sb.append(processedLine);
//
//            if (it.hasNext()) {
//                sb.append(" ");
//            }
//        }
//
//        it.close();
//        return sb.compact();
    }

    @Override
    public ImmutableSet<String> getSupportedContentTypes() {
        return ImmutableSet.of(
                "text/html",
                "text/xml",
                "application/atom+xml",
                "application/rdf+xml",
                "application/rss+xml",
                "application/soap+xml",
                "application/rdf+xml",
                "application/xhtml+xml",
                "application/xml",
                "application/xml-dtd");
    }

    private class FormattingVisitor implements NodeVisitor {
        private static final int maxWidth = 80;
        private int width;
        private StringBuilder accum;

        private FormattingVisitor() {
            this.width = 0;
            this.accum = new StringBuilder();
        }

        public void head(Node node, int depth) {
            String name = node.nodeName();
            if (node instanceof TextNode) {
                this.append(((TextNode) node).text());
            } else if (name.equals("li")) {
                this.append("\n * ");
            }

        }

        public void tail(Node node, int depth) {
            String name = node.nodeName();
            if (name.equals("br")) {
                this.append("\n");
            } else if (StringUtil.in(name, new String[]{"p", "h1", "h2", "h3", "h4", "h5"})) {
                this.append("\n\n");
            } else if (name.equals("a")) {
                this.append(String.format(" <%s>", new Object[]{node.absUrl("href")}));
            }

        }

        private void append(String text) {
            if (text.startsWith("\n")) {
                this.width = 0;
            }

            if (!text.equals(" ") || this.accum.length() != 0 && !StringUtil.in(this.accum.substring(this.accum.length() - 1), new String[]{" ", "\n"})) {
                if (text.length() + this.width > 80) {
                    String[] words = text.split("\\s+");

                    for (int i = 0; i < words.length; ++i) {
                        String word = words[i];
                        boolean last = i == words.length - 1;
                        if (!last) {
                            word = word + " ";
                        }

                        if (word.length() + this.width > 80) {
                            this.accum.append("\n").append(word);
                            this.width = word.length();
                        } else {
                            this.accum.append(word);
                            this.width += word.length();
                        }
                    }
                } else {
                    this.accum.append(text);
                    this.width += text.length();
                }

            }
        }

        public String toString() {
            return this.accum.toString();
        }
    }
}
