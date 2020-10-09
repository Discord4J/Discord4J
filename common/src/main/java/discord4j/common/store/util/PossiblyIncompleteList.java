package discord4j.common.store.util;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Represents a list of elements with an additional hint to indicate whether the list is complete or not. Store
 * implementations are expected to provide the best effort to compute this hint.
 * @param <E>
 */
public class PossiblyIncompleteList<E> implements Iterable<E> {

    private final List<E> list;
    private final boolean isComplete;

    public PossiblyIncompleteList(List<E> list, boolean isComplete) {
        this.list = list;
        this.isComplete = isComplete;
    }

    public List<E> getList() {
        return list;
    }

    public boolean isComplete() {
        return isComplete;
    }

    @Override
    public Iterator<E> iterator() {
        return list.iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PossiblyIncompleteList<?> that = (PossiblyIncompleteList<?>) o;
        return isComplete == that.isComplete && list.equals(that.list);
    }

    @Override
    public int hashCode() {
        return Objects.hash(list, isComplete);
    }

    @Override
    public String toString() {
        return "PossiblyIncompleteList{" +
            "list=" + list +
            ", isComplete=" + isComplete +
            '}';
    }
}
