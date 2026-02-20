public class BinarySearch {
    public static int search(int[] values, int target) {
        int left = 0;
        int right = values.length - 1;

        while (left <= right) {
            int middle = left + (right - left) / 2;
            int current = values[middle];

            if (current == target) {
                return middle;
            }

            if (current < target) {
                left = middle + 1;
            } else {
                right = middle - 1;
            }
        }

        return -1;
    }
}
