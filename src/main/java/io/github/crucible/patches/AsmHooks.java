package io.github.crucible.patches;

import java.util.Iterator;
import java.util.List;

public class AsmHooks {
    public static String join(List<String> pieces, String separator) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> iterator = pieces.iterator();
        while (iterator.hasNext()) {
            sb.append(iterator.next());
            if (iterator.hasNext())
                sb.append(separator);
        }
        return sb.toString();
    }
}
