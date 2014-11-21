package argus.util;

import argus.evaluation.EvaluationParam;
import com.google.common.collect.ImmutableList;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.split;

/**
 * A loader class that parses a evaluation file, containing queries and
 * expected results.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 */
public class EvaluationFileLoader {

    private static final Logger logger = LoggerFactory.getLogger(StopwordFileLoader.class);

    public List<EvaluationParam> load(InputStream evaluationFileStream, boolean ignoreSlops) {
        List<EvaluationParam> evQueries = new ArrayList<>();
        try {
            List<String> lines = IOUtils.readLines(evaluationFileStream);

            boolean isParsing = false;
            String id = null;
            String text = null;
            int slop = 0;
            List<String> sortedDocuments = new ArrayList<>();

            for (String line : lines) {
                line = line.trim();
                if (line.equals("\\\\")) {
                    if (id != null && text != null) {
                        evQueries.add(new EvaluationParam(
                                id,
                                text,
                                slop,
                                sortedDocuments
                        ));
                    }
                    id = null;
                    text = null;
                    slop = 0;
                    sortedDocuments = new ArrayList<>();
                    isParsing = false;

                } else if (isParsing) {
                    sortedDocuments.add(line);

                } else if (!line.isEmpty()) {
                    String[] s = split(line, ':');
                    if (Objects.equals(s[0], "Q")) {
                        isParsing = true;

                        id = s[1];

                        String remainingString = s[2];
                        Pattern p = Pattern.compile("\"([^\"]*)\"");
                        Matcher m = p.matcher(remainingString);
                        while (m.find()) {
                            text = m.group(1);
                        }
                        if (!ignoreSlops) {
                            int indexOfTilde = remainingString.indexOf('~');
                            if (indexOfTilde >= 0) {
                                String rangeString = remainingString.substring(indexOfTilde + 1);
                                slop = Integer.parseInt(rangeString);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("There was a problem detecting the currently implemented Reader classes.", e);
        }
        return ImmutableList.copyOf(evQueries);
    }
}
