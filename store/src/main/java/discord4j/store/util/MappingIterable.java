/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.store.util;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.*;

public class MappingIterable<I, O> implements Iterable<O> {

    private final Function<I, O> mapper;
    private final Iterable<I> original;

    public MappingIterable(Function<I, O> mapper, Iterable<I> original) {
        this.mapper = mapper;
        this.original = original;
    }

    @Override
    public void forEach(Consumer<? super O> action) {
        original.forEach(it -> {
            O mapped = mapper.apply(it);
            action.accept(mapped);
        });
    }

    @Override
    public Spliterator<O> spliterator() {
        return new MappingSpliterator(original.spliterator());
    }

    @Override
    public Iterator<O> iterator() {
        return new MappingIterator(original.iterator());
    }

    private class MappingIterator implements Iterator<O> {

        private final Iterator<I> original;

        private MappingIterator(Iterator<I> original) {
            this.original = original;
        }

        @Override
        public boolean hasNext() {
            return original.hasNext();
        }

        @Override
        public O next() {
            return mapper.apply(original.next());
        }

        @Override
        public void remove() {
            original.remove();
        }

        @Override
        public void forEachRemaining(Consumer<? super O> action) {
            original.forEachRemaining(it -> {
                O mapped = mapper.apply(it);
                action.accept(mapped);
            });
        }
    }

    private class MappingSpliterator implements Spliterator<O> {

        private final Spliterator<I> original;

        private MappingSpliterator(Spliterator<I> original) {
            this.original = original;
        }

        @Override
        public boolean tryAdvance(Consumer<? super O> action) {
            return original.tryAdvance(it -> {
                O mapped = mapper.apply(it);
                action.accept(mapped);
            });
        }

        @Override
        public void forEachRemaining(Consumer<? super O> action) {
            original.forEachRemaining(it -> {
                O mapped = mapper.apply(it);
                action.accept(mapped);
            });
        }

        @Override
        public Spliterator<O> trySplit() {
            return new MappingSpliterator(original.trySplit());
        }

        @Override
        public long estimateSize() {
            return original.estimateSize();
        }

        @Override
        public long getExactSizeIfKnown() {
            return original.getExactSizeIfKnown();
        }

        @Override
        public int characteristics() {
            return original.characteristics();
        }

        @Override
        public boolean hasCharacteristics(int characteristics) {
            return original.hasCharacteristics(characteristics);
        }

//        @Override FIXME: No way to reverse mapper function, removing the ability for this delegation
//        public Comparator<? super O> getComparator() {
//            return new MappingComparator(original.getComparator());
//        }
    }
}
