package com.edduarte.vokter.persistence.ram;

import com.edduarte.vokter.diff.DiffDetector;
import com.edduarte.vokter.diff.DiffEvent;
import com.edduarte.vokter.persistence.Diff;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class RAMDiff implements Diff {

    private final DiffEvent event;

    private final String text;

    private final int startIndex;


    public RAMDiff(DiffEvent event, String text, int startIndex) {
        this.event = event;
        this.text = text;
        this.startIndex = startIndex;
    }


    public RAMDiff(DiffDetector.Result r) {
        this(r.getEvent(), r.getText(), r.getStartIndex());
    }


    @Override
    public DiffEvent getEvent() {
        return event;
    }


    @Override
    public String getText() {
        return text;
    }


    @Override
    public int getStartIndex() {
        return startIndex;
    }


    @Override
    public int getEndIndex() {
        return startIndex + text.length();
    }
}
