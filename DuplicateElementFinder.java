import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility to identify duplicate elements from a list while preserving duplicate encounter order.
 */
public class DuplicateElementFinder {
    /**
     * Returns unique duplicate elements from the provided list.
     *
     * @param values source list to inspect; null or empty list returns an empty result
     * @return duplicates in the order their second occurrence is first detected
     */
    public static <T> ArrayList<T> findDuplicates(List<T> values) {
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
