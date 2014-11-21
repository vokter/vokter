package argus.index;

import com.google.common.cache.CacheLoader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.*;
import java.util.Set;

/**
 * A cache loader that reads local index files separated by first-character of
 * tokens. This cache loader is used by a LoadingCache, instantiated at the
 * CollectionBuilder, to manage temporary in-memory tokens.
 *
 * @author Eduardo Duarte (<a href="mailto:eduardo.miguel.duarte@gmail.com">eduardo.miguel.duarte@gmail.com</a>)
 * @version 1.0
 */
public final class TermLoader extends CacheLoader<String, Term> {

    private File parentIndexFolder;

    TermLoader(File parentIndexFolder) {
        this.parentIndexFolder = parentIndexFolder;
    }

    @Override
    @ParametersAreNonnullByDefault
    public Term load(String tokenText) throws Exception {
        if (tokenText.isEmpty()) {
            return null;
        }

        Character c = tokenText.charAt(0);
        File indexFile = new File(parentIndexFolder, c.toString());

        if (indexFile.exists() && !indexFile.isDirectory()) {
            LineIterator lines = FileUtils.lineIterator(indexFile);

            while (lines.hasNext()) {
                String line = lines.next();
                int lineTextEndIndex = line.indexOf(":");
                if (lineTextEndIndex > 0) {
                    String lineText = line.substring(0, lineTextEndIndex);
                    if (tokenText.equals(lineText)) {
                        lines.close();
                        return Term.parseLine(line);
                    }
                }
            }

            lines.close();
        }

        // If this point is reached, then there are no stored
        // tokens with the specified text in the index
        return null;
    }

    @ParametersAreNonnullByDefault
    public void write(Character c, Set<Term> termsToWrite) {
        if (termsToWrite.isEmpty()) {
            return;
        }
        File indexFile = new File(parentIndexFolder, c.toString());

        try {
            Writer writer = new BufferedWriter(new FileWriter(indexFile));
            for (Term term : termsToWrite) {
                term.writeLine(writer);
            }

        } catch (IOException ex) {
            System.err.println("Error while writing index for tokens starting " +
                    "with the character '" + c + "'.");
            ex.printStackTrace();
        }
    }
}
