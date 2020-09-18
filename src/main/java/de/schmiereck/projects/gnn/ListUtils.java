package de.schmiereck.projects.gnn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public abstract class ListUtils {

    public static <T> List<List<T>> permutations(List<T> es) {
        List<List<T>> permutations = new ArrayList<List<T>>();

        if (es.isEmpty()){
            return permutations;
        }

        // We add the first element
        permutations.add(new ArrayList<T>(Arrays.asList(es.get(0))));

        // Then, for all elements e in es (except from the first)
        for (int ePos = 1, len = es.size(); ePos < len; ePos++) {
            T e = es.get(ePos);

            // We take remove each list l from 'permutations'
            for (int pPos = permutations.size() - 1; pPos >= 0; pPos--) {
                List<T> l = permutations.remove(pPos);

                // And adds a copy of l, with e inserted at index k for each position k in l
                for (int lPos = l.size(); lPos >= 0; lPos--) {
                    ArrayList<T> ts2 = new ArrayList<>(l);
                    ts2.add(lPos, e);
                    permutations.add(ts2);
                }
            }
        }
        return permutations;
    }

    public static <T> void permute(final List<List<T>> lists,
                                   final Consumer<List<T>> consumer)
    {
        final int[] indexPosArr = new int[lists.size()];

        final int last_index = lists.size() - 1;
        final List<T> permuted = new ArrayList<T>(lists.size());

        for (int pos = 0; pos < lists.size(); ++pos) {
            permuted.add(null);
        }

        while (indexPosArr[last_index] < lists.get(last_index).size()) {
            for (int pos = 0; pos < lists.size(); ++pos) {
                permuted.set(pos, lists.get(pos).get(indexPosArr[pos]));
            }
            consumer.accept(permuted);

            for (int pos = 0; pos < lists.size(); ++pos) {
                ++indexPosArr[pos];
                if (indexPosArr[pos] < lists.get(pos).size()) {
                    /* stop at first element without overflow */
                    break;
                } else {
                    if (pos < last_index) {
                        indexPosArr[pos] = 0;
                    }
                }
            }
        }
    }

    /**
     * https://stackoverflow.com/questions/60251019/is-there-a-non-recursive-solution-to-get-a-list-of-all-possible-concatenations-o
     *
     * Return all possible combination of n Strings
     * @param set Array of Strings to combine
     * @param n Number of parts to combine in each row
     */
    static <T> List<List<T>> getAllCombination(T[] set, int n)
    {
        List<List<T>> combinations = new ArrayList<>();

        // First step (0)
        // Create initial combinations, filled with the first String.
        for (int number = 0; number < set.length; number++)
        {
            List<T> array = new ArrayList<>(n); // the final size of each array is already known
            array.add(0, set[number]); // fill the first Part
            for (int step = 1; step < n; step++) {
                array.add(step, null);
            }
            combinations.add(array);
        }

        // In each following step, we add one number to each combination
        for (int step = 1; step < n; step++)
        {
            // Backup the size because we do not want to process
            // the new items that are added by the following loop itself.
            int size = combinations.size();

            // Add one number to the existing combinations
            for (int combination = 0; combination < size; combination++)
            {
                // Add one part to the existing array
                List<T> array = combinations.get(combination);
                array.set(step, set[0]);

                // For all additional Strings, create a copy of the array
                for (int number = 1; number < set.length; number++)
                {
                    List<T> copy = new ArrayList(array);
                    copy.set(step, set[number]);
                    combinations.add(copy);
                }
            }
        }
        return combinations;
    }
}
