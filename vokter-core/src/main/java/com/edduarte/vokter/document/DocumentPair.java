package com.edduarte.vokter.document;

import com.edduarte.vokter.model.mongodb.Document;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class DocumentPair {

    private final Document mA;

    private final Document mB;


    private DocumentPair(Document a, Document b) {
        this.mA = a;
        this.mB = b;
    }


    public static DocumentPair of(Document a, Document b) {
        return new DocumentPair(a, b);
    }


    public Document oldest() {
        return this.mA;
    }


    public Document latest() {
        return this.mB;
    }


    public String toString() {
        return "(" + this.oldest() + "," + this.latest() + ")";
    }


    public boolean equals(Object that) {
        if (!(that instanceof DocumentPair)) {
            return false;
        } else {
            DocumentPair thatPair = (DocumentPair) that;
            return this.mA.equals(thatPair.mA) && this.mB.equals(thatPair.mB);
        }
    }


    public int hashCode() {
        return 31 * this.mA.hashCode() + this.mB.hashCode();
    }
}
