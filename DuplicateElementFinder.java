import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public class DuplicateElementFinder {
    public static <T> ArrayList<T> findDuplicates(ArrayList<T> values) {
        ArrayList<T> duplicates = new ArrayList<>();
        if (values == null || values.isEmpty()) {
            return duplicates;
        }

        Set<T> seen = new LinkedHashSet<>();
        Set<T> duplicateSet = new LinkedHashSet<>();

        for (T value : values) {
            if (!seen.add(value)) {
                duplicateSet.add(value);
            }
        }

        duplicates.addAll(duplicateSet);
        return duplicates;
    }
}
