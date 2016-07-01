package com.edduarte.vokter.persistence.ram;

import com.edduarte.vokter.diff.DiffDetector;
import com.edduarte.vokter.persistence.Diff;
import com.edduarte.vokter.persistence.DiffCollection;
import com.edduarte.vokter.util.Params;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.0.0
 * @since 1.0.0
 */
public class RAMDiffCollection implements DiffCollection {

    private final Map<Params, List<Diff>> diffsMap;


    public RAMDiffCollection() {
        this.diffsMap = new HashMap<>();
    }


    @Override
    public void addDifferences(String documentUrl, String documentContentType,
                               List<DiffDetector.Result> diffs) {
        List<Diff> list = diffs.parallelStream()
                .map(RAMDiff::new)
                .collect(Collectors.toList());
        diffsMap.put(Params.of(documentUrl, documentContentType), list);
    }


    @Override
    public void removeDifferences(String documentUrl, String documentContentType) {
        diffsMap.remove(Params.of(documentUrl, documentContentType));
    }


    @Override
    public List<Diff> getDifferences(String documentUrl, String documentContentType) {
        return diffsMap.get(Params.of(documentUrl, documentContentType));
    }
}
