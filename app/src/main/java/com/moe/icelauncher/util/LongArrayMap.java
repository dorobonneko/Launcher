package com.moe.icelauncher.util;
import java.util.Iterator;
import android.util.LongSparseArray;

public class LongArrayMap<E> extends LongSparseArray<E> implements Iterable<E> {

    public boolean containsKey(long key) {
        return indexOfKey(key) >= 0;
    }

    public boolean isEmpty() {
        return size() <= 0;
    }

    @Override
    public LongArrayMap<E> clone() {
        return (LongArrayMap<E>) super.clone();
    }

    @Override
    public Iterator<E> iterator() {
        return new ValueIterator();
    }

    @Thunk class ValueIterator implements Iterator<E> {

        private int mNextIndex = 0;

        @Override
        public boolean hasNext() {
            return mNextIndex < size();
        }

        @Override
        public E next() {
            return valueAt(mNextIndex ++);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
	}
