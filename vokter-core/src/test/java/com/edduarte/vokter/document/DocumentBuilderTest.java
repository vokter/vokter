package com.edduarte.vokter.document;

import com.edduarte.vokter.persistence.Document;
import com.edduarte.vokter.persistence.ram.RAMDocument;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 * @since 1.0
 */
public class DocumentBuilderTest {

    private static final Logger logger = LoggerFactory.getLogger(DocumentBuilderTest.class);

    private static LanguageDetector langDetector;


    @BeforeClass
    public static void setUp() throws IOException, InterruptedException {
        List<LanguageProfile> languageProfiles = new LanguageProfileReader().readAllBuiltIn();
        langDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                .withProfiles(languageProfiles)
                .build();
    }

    // with correct content type specified
    // expected result: document content type equals the specified content type


    @Test
    public void htmlCorrectContentType() {
        Document document = DocumentBuilder
                .fromUrl("https://en.wikipedia.org/wiki/Argus_Panoptes", MediaType.TEXT_HTML)
                .build(langDetector, RAMDocument.class);
        assertEquals(MediaType.TEXT_HTML, document.getContentType());
    }


    @Test
    public void xmlCorrectContentType() {
        Document document = DocumentBuilder
                .fromUrl("https://en.wikipedia.org/wiki/Special:Export/Argus_Panoptes", MediaType.APPLICATION_XML)
                .build(langDetector, RAMDocument.class);
        assertEquals(MediaType.APPLICATION_XML, document.getContentType());
    }


    @Test
    public void jsonCorrectContentType() {
        Document document = DocumentBuilder
                .fromUrl("https://en.wikipedia.org/w/api.php?format=json&action=query&titles=Argus_Panoptes&prop=revisions&rvprop=content", MediaType.APPLICATION_JSON)
                .build(langDetector, RAMDocument.class);
        assertEquals(MediaType.APPLICATION_JSON, document.getContentType());
    }


    // with wrong content type specified
    // expected result: document builder used the response content type, so
    // the document content type equals the correct content type


    @Test
    public void htmlWrongContentType() {
        Document document = DocumentBuilder
                .fromUrl("https://en.wikipedia.org/wiki/Argus_Panoptes", MediaType.APPLICATION_JSON)
                .build(langDetector, RAMDocument.class);
        assertEquals(MediaType.TEXT_HTML, document.getContentType());
    }


    @Test
    public void xmlWrongContentType() {
        Document document = DocumentBuilder
                .fromUrl("https://en.wikipedia.org/wiki/Special:Export/Argus_Panoptes", MediaType.TEXT_HTML)
                .build(langDetector, RAMDocument.class);
        assertEquals(MediaType.APPLICATION_XML, document.getContentType());
    }


    @Test
    public void jsonWrongContentType() {
        Document document = DocumentBuilder
                .fromUrl("https://en.wikipedia.org/w/api.php?format=json&action=query&titles=Argus_Panoptes&prop=revisions&rvprop=content", MediaType.APPLICATION_XML)
                .build(langDetector, RAMDocument.class);
        assertEquals(MediaType.APPLICATION_JSON, document.getContentType());
    }


    // with null content type specified
    // expected result: document builder used the response content type, so
    // the document content type equals the correct content type


    @Test
    public void htmlNoContentType() {
        Document document = DocumentBuilder
                .fromUrl("https://en.wikipedia.org/wiki/Argus_Panoptes", null)
                .build(langDetector, RAMDocument.class);
        assertEquals(MediaType.TEXT_HTML, document.getContentType());
    }


    @Test
    public void xmlNoContentType() {
        Document document = DocumentBuilder
                .fromUrl("https://en.wikipedia.org/wiki/Special:Export/Argus_Panoptes", null)
                .build(langDetector, RAMDocument.class);
        assertEquals(MediaType.APPLICATION_XML, document.getContentType());
    }


    @Test
    public void jsonNoContentType() {
        Document document = DocumentBuilder
                .fromUrl("https://en.wikipedia.org/w/api.php?format=json&action=query&titles=Argus_Panoptes&prop=revisions&rvprop=content", null)
                .build(langDetector, RAMDocument.class);
        assertEquals(MediaType.APPLICATION_JSON, document.getContentType());
    }
}
