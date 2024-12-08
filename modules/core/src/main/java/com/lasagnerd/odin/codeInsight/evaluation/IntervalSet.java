package com.lasagnerd.odin.codeInsight.evaluation;

import lombok.Getter;

import java.util.*;

public class IntervalSet<T extends Number & Comparable<T>> implements Set<IntervalSet.Interval<T>> {

    // Define a generic Interval class
    @Getter
    public static class Interval<T extends Number & Comparable<T>> implements Comparable<Interval<T>> {
        private final T start;
        private final T end;

        public Interval(T start, T end) {
            if (start.compareTo(end) > 0) {
                throw new IllegalArgumentException("Start must be less than or equal to end.");
            }
            this.start = start;
            this.end = end;
        }

        // Calculate intersection with another interval
        public Interval<T> intersect(Interval<T> other) {
            T newStart = (this.start.compareTo(other.start) > 0) ? this.start : other.start;
            T newEnd = (this.end.compareTo(other.end) < 0) ? this.end : other.end;

            if (newStart.compareTo(newEnd) > 0) {
                return null; // No intersection
            }
            return new Interval<>(newStart, newEnd);
        }

        public boolean isSingleton() {
            return start.compareTo(end) == 0;
        }

        public T getSingletonValue() {
            if (!isSingleton()) {
                throw new IllegalStateException("Interval is not a singleton.");
            }
            return start;
        }

        @Override
        public String toString() {
            return "[" + start + ", " + end + "]";
        }

        @Override
        public int compareTo(Interval<T> other) {
            int startComparison = this.start.compareTo(other.start);
            if (startComparison != 0) {
                return startComparison;
            }
            return this.end.compareTo(other.end);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Interval)) return false;
            Interval<?> other = (Interval<?>) obj;
            return start.equals(other.start) && end.equals(other.end);
        }

        @Override
        public int hashCode() {
            return Objects.hash(start, end);
        }
    }

    private final Set<Interval<T>> intervals;

    public IntervalSet() {
        this.intervals = new HashSet<>();
    }

    @Override
    public boolean add(Interval<T> interval) {
        return intervals.add(interval);
    }

    @Override
    public boolean remove(Object o) {
        return intervals.remove(o);
    }

    @Override
    public boolean contains(Object o) {
        return intervals.contains(o);
    }

    @Override
    public int size() {
        return intervals.size();
    }

    @Override
    public boolean isEmpty() {
        return intervals.isEmpty();
    }

    @Override
    public void clear() {
        intervals.clear();
    }

    @Override
    public Iterator<Interval<T>> iterator() {
        return intervals.iterator();
    }

    @Override
    public Object[] toArray() {
        return intervals.toArray();
    }

    @Override
    public <E> E[] toArray(E[] a) {
        return intervals.toArray(a);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return intervals.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Interval<T>> c) {
        return intervals.addAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return intervals.retainAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return intervals.removeAll(c);
    }

    public Interval<T> calculateConjunction() {
        if (intervals.isEmpty()) {
            return null;
        }

        Iterator<Interval<T>> iterator = intervals.iterator();
        Interval<T> conjunction = iterator.next();

        while (iterator.hasNext()) {
            Interval<T> interval = iterator.next();
            conjunction = conjunction.intersect(interval);
            if (conjunction == null) {
                return null; // No overlap
            }
        }
        return conjunction;
    }

    public T getSingletonFromConjunction() {
        Interval<T> conjunction = calculateConjunction();
        if (conjunction != null && conjunction.isSingleton()) {
            return conjunction.getSingletonValue();
        }
        return null;
    }

    public static void main(String[] args) {
        IntervalSet<Integer> intervalSet = new IntervalSet<>();

        // Add intervals
        intervalSet.add(new Interval<>(1, 5));
        intervalSet.add(new Interval<>(3, 3));
        intervalSet.add(new Interval<>(2, 4));

        // Calculate conjunction and check for singleton
        Integer singleton = intervalSet.getSingletonFromConjunction();
        if (singleton != null) {
            System.out.println("The conjunction reduces to a singleton: " + singleton);
        } else {
            System.out.println("The conjunction does not reduce to a singleton.");
        }
    }
}
