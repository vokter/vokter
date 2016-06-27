package com.edduarte.vokter.document;

import com.edduarte.vokter.model.mongodb.Document;
import com.edduarte.vokter.parser.ParserPool;
import com.edduarte.vokter.parser.SimpleParser;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;

import static org.junit.Assert.assertEquals;

/**
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public class DocumentBuilderTest {

    private static final Logger logger = LoggerFactory.getLogger(DocumentBuilderTest.class);

    // with correct content type specified
    // expected result: document content type equals the specified content type

    @Test
    public void htmlCorrectContentType() {
        Document document = DocumentBuilder
                .fromUrl("https://en.wikipedia.org/wiki/Argus_Panoptes", MediaType.TEXT_HTML)
                .build();
        assertEquals(MediaType.TEXT_HTML, document.getContentType());
    }

    @Test
    public void xmlCorrectContentType() {
        Document document = DocumentBuilder
                .fromUrl("https://en.wikipedia.org/wiki/Special:Export/Argus_Panoptes", MediaType.APPLICATION_XML)
                .build();
        assertEquals(MediaType.APPLICATION_XML, document.getContentType());
    }

    @Test
    public void jsonCorrectContentType() {
        Document document = DocumentBuilder
                .fromUrl("https://en.wikipedia.org/w/api.php?format=json&action=query&titles=Argus_Panoptes&prop=revisions&rvprop=content", MediaType.APPLICATION_JSON)
                .build();
        assertEquals(MediaType.APPLICATION_JSON, document.getContentType());
    }


    // with wrong content type specified
    // expected result: document builder used the response content type, so
    // the document content type equals the correct content type

    @Test
    public void htmlWrongContentType() {
        Document document = DocumentBuilder
                .fromUrl("https://en.wikipedia.org/wiki/Argus_Panoptes", MediaType.APPLICATION_JSON)
                .build();
        assertEquals(MediaType.TEXT_HTML, document.getContentType());
    }

    @Test
    public void xmlWrongContentType() {
        Document document = DocumentBuilder
                .fromUrl("https://en.wikipedia.org/wiki/Special:Export/Argus_Panoptes", MediaType.TEXT_HTML)
                .build();
        assertEquals(MediaType.APPLICATION_XML, document.getContentType());
    }

    @Test
    public void jsonWrongContentType() {
        Document document = DocumentBuilder
                .fromUrl("https://en.wikipedia.org/w/api.php?format=json&action=query&titles=Argus_Panoptes&prop=revisions&rvprop=content", MediaType.APPLICATION_XML)
                .build();
        assertEquals(MediaType.APPLICATION_JSON, document.getContentType());
    }


    // with null content type specified
    // expected result: document builder used the response content type, so
    // the document content type equals the correct content type

    @Test
    public void htmlNoContentType() {
        Document document = DocumentBuilder
                .fromUrl("https://en.wikipedia.org/wiki/Argus_Panoptes", null)
                .build();
        assertEquals(MediaType.TEXT_HTML, document.getContentType());
    }

    @Test
    public void xmlNoContentType() {
        Document document = DocumentBuilder
                .fromUrl("https://en.wikipedia.org/wiki/Special:Export/Argus_Panoptes", null)
                .build();
        assertEquals(MediaType.APPLICATION_XML, document.getContentType());
    }

    @Test
    public void jsonNoContentType() {
        Document document = DocumentBuilder
                .fromUrl("https://en.wikipedia.org/w/api.php?format=json&action=query&titles=Argus_Panoptes&prop=revisions&rvprop=content", null)
                .build();
        assertEquals(MediaType.APPLICATION_JSON, document.getContentType());
    }
}
