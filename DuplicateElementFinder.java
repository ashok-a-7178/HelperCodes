import java.util.ArrayList;
import java.util.HashSet;
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
    public static <T> List<T> findDuplicates(List<T> values) {
        if (values == null || values.isEmpty()) {
            return new ArrayList<>();
        }

        Set<T> seen = new HashSet<>();
        Set<T> duplicates = new LinkedHashSet<>();

        for (T value : values) {
            if (!seen.add(value)) {
                duplicates.add(value);
            }
        }

        return new ArrayList<>(duplicates);
    }
}
