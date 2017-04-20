package models.results;

import java.util.*;

public class Preorder<T> {

    public static <T> Preorder<T> construct(Collection<T> elements, Comparator<T> comparator) {
        return new Preorder<>(elements, comparator);
    }

    public static <T extends Comparable<T>> Preorder<T> construct(Collection<T> elements) {
        return construct(elements, Comparator.naturalOrder());
    }

    private final Map<T, Integer> element2level;
    private final List<List<T>> levels;
    private final int size;
    private final int[] accumulatedSizes; //0, l(0), l(0) + l(1), ..., l(0) + ... + l(n - 1)

    private Preorder(Collection<T> elements, Comparator<T> comparator) {
        ArrayList<T> sorted = new ArrayList<>(elements);
        sorted.sort(comparator);

        levels = new ArrayList<>();
        element2level = new HashMap<>();
        T lastAdded = null;
        List<T> lastLevel = null;
        int levelInd = -1;
        for (T e : sorted) {
            if (lastAdded == null || comparator.compare(e, lastAdded) != 0) {
                lastLevel = new ArrayList<>();
                levels.add(lastLevel);
                levelInd++;
            }
            lastLevel.add(e);
            lastAdded = e;
            element2level.put(e, levelInd);
        }

        size = elements.size();
        //compute accumulated sizes
        accumulatedSizes = new int[levels.size()];
        for (int i = 0; i < levels.size(); i++)
            if (i == 0)
                accumulatedSizes[i] = 0;
            else
                accumulatedSizes[i] = accumulatedSizes[i - 1] + levels.get(i).size();
    }

    public int getLevel(T t) {
        return element2level.get(t);
    }

    public int getLevelsCount() {
        return levels.size();
    }

    public int getLevelSize(int level) {
        return levels.get(level).size();
    }

    public int getAccumulatedLevelSize(int level) {
        return accumulatedSizes[level];
    }

    public int size() {
        return size;
    }
}
