package argus.reader;

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
public class MarkupReader implements argus.reader.Reader, NodeVisitor {

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
