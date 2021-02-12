package io.github.crucible.patches;

import java.util.Iterator;
import java.util.List;

public class Hook {
    public static String join(List<String> pieces, String separator) {
        StringBuilder builder = new StringBuilder();
        Iterator<String> iterator = pieces.iterator();
        while (iterator.hasNext()) {
            builder.append(iterator.next());
            if (iterator.hasNext())
                builder.append(separator);
        }
        return builder.toString();
    }
}
