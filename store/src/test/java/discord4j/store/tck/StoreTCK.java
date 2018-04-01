/*
 *     This file is part of Discord4J.
 *
 *     Discord4J is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Discord4J is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */

package discord4j.store.tck;

import discord4j.store.Store;
import discord4j.store.primitive.ForwardingStore;
import discord4j.store.primitive.LongObjStore;
import discord4j.store.service.StoreService;
import org.junit.Test;
import reactor.util.Logger;
import reactor.util.Loggers;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.*;

/**
 * Extend this class and provide your {@link discord4j.store.service.StoreService} implementation.
 *
 * Running all the tests will ensure that your store implements all the operations as expected in.
 *
 * You should probably implement your own tests on top of this to ensure your store's unique features work correctly.
 */
public abstract class StoreTCK {

    final static Class<? extends Comparable> GENERIC_KEY = String.class, PRIMITIVE_KEY = Long.class;

    final Logger logger = Loggers.getLogger(this.getClass());
    final Random rng = new Random();

    volatile int ranTestTotal;
    volatile int passedTestTotal;

    /**
     * Gets the StoreService instance to test compatibility for.
     *
     * @return The tested store service.
     */
    public abstract StoreService getStoreService();

    /**
     * Gets a generic store.
     */
    public <K extends Comparable<K>, V extends Serializable> Store<K, V> getObjObjStore(Class<K> keyClass, Class<V> valueClass) {
        return getStoreService().provideGenericStore(keyClass, valueClass);
    }

    /**
     * Gets a long store.
     */
    public <V extends Serializable> LongObjStore<V> getLongObjStore(Class<V> valueClass) {
        return getStoreService().provideLongObjStore(valueClass);
    }

    public final <K extends Comparable<K>, V extends Serializable> Map<K, V> map() {
        return new ConcurrentHashMap<>();
    }

    final long randLong() {
        return rng.nextLong();
    }

    final String randString() {
        int strLen = rng.nextInt(Integer.MAX_VALUE - 5);
        byte[] bytes = new byte[strLen];
        rng.nextBytes(bytes);
        return new String(bytes);
    }

    final <T extends Serializable> T randomizeObject(T obj) {
        try {
            for (Field field : obj.getClass().getDeclaredFields()) {
                if (field.isSynthetic()
                        || Modifier.isFinal(field.getModifiers())
                        || Modifier.isTransient(field.getModifiers())
                        || Modifier.isStatic(field.getModifiers()))
                    continue;

                field.setAccessible(true);

                if (field.getType().equals(long.class)) {
                    field.setLong(obj, randLong());
                } else if (field.getType().equals(long[].class)) {
                    int arrLen = rng.nextInt(Integer.MAX_VALUE - 5);
                    long[] arr = new long[arrLen];
                    for (int i = 0; i < arrLen; i++) {
                        arr[i] = randLong();
                    }
                    field.set(obj, arr);
                } else if (field.getType().equals(Long.class)) {
                    boolean isNull = Math.abs(rng.nextGaussian()) > 0.55;
                    if (isNull)
                        field.set(obj, null);
                    else
                        field.set(obj, (Long) randLong());
                } else if (field.getType().equals(int.class)) {
                    field.setInt(obj, rng.nextInt());
                } else if (field.getType().equals(boolean.class)) {
                    field.setBoolean(obj, rng.nextBoolean());
                } else if (field.getType().equals(String.class)) {
                    field.set(obj, randString());
                } else if (Serializable.class.isAssignableFrom(field.getType())) {
                    field.set(obj, randomizeObject((Serializable) field.getType().newInstance()));
                } else {
                    throw new RuntimeException("Unsupported field type " + field.getType());
                }
            }
        } catch (Exception toWrap) {
            if (toWrap instanceof RuntimeException)
                throw (RuntimeException) toWrap;
            throw new RuntimeException(toWrap);
        }
        return obj;
    }

    public final TestBean randomBean() {
        return randomizeObject(TestBean.getBean());
    }

    @Test
    public final void tckEntryPoint() {
        StoreService service = getStoreService();
        assertNotNull("Store service is null!", service);
        logger.info("Running the Store TCK on {}...", service.getClass());

        boolean hasGenericStores;

        if (service.hasGenericStores()) {
            logger.info("Generic stores enabled.");
            hasGenericStores = true;
            Store<String, TestBean> testStore = getObjObjStore(String.class, TestBean.class);

            safeTest(() -> testSerialization(testStore, String.class));
            //TODO
        } else {
            logger.warn("Generic stores are not enabled on this service!");
            logger.warn("This is usually not intended, but if it is please ignore this warning.");
            hasGenericStores = false;
        }

        boolean hasLongObjStores;
        if (service.hasLongObjStores()) {
            logger.info("Long-Object stores enabled.");
            hasLongObjStores = true;
            LongObjStore<TestBean> testStore = getLongObjStore(TestBean.class);

            safeTest(() -> testSerialization(testStore, Long.class));
            //TODO
        } else {
            hasLongObjStores = false;
            LongObjStore<?> testStore = null;
            try {
                testStore = service.provideLongObjStore(TestBean.class);
            } catch (Throwable ignored) {}
            if (testStore == null || testStore instanceof ForwardingStore) {
                logger.info("Long-Object stores disabled.");
            } else {
                logger.warn("Long-Object stores are not enabled on this service!");
                logger.warn("It is possible that this is not intended based on heuristics.");
                logger.warn("If this is intended, please ignore this warning.");
            }
        }

        if (!hasGenericStores && !hasLongObjStores) {
            logger.error("No stores are available!");
            logger.error("Please ensure that the StoreService#hasGenericStores and StoreService#hasLongObjStores flags are correct.");
            throw new AssertionError("Tests failed.");
        }

        if (passedTestTotal == ranTestTotal) {
            logger.info("{}/{} tests passed. This service is likely a compatible store implementation.", passedTestTotal, ranTestTotal);
        } else {
            logger.error("{}/{} tests passed.", passedTestTotal, ranTestTotal);
            logger.error("It is possible the store can still work with Discord4J, but support is not guaranteed!");
            throw new AssertionError("Tests failed.");
        }
    }

    final boolean safeTest(Runnable r) {
        try {
            r.run();
            return true;
        } catch (Throwable t) {
            logger.error("Error caught!", t);
            return false;
        }
    }

    final <K extends Comparable<K>> K randKey(Class<K> keyClass) {
        if (keyClass.equals(GENERIC_KEY))
            return (K) randString();
        else if (keyClass.equals(PRIMITIVE_KEY))
            return (K) (Long) randLong();
        else
            throw new RuntimeException("Invalid key type " + keyClass);
    }

    //Generic store tests
    final <K extends Comparable<K>> void testSerialization(Store<K, TestBean> store, Class<K> keyClass) {
        ranTestTotal++;
        TestBean bean = randomBean();
        K key = randKey(keyClass);
        assertTrue(bean.isOriginal());

        store.save(key, bean).block();

        TestBean beanCopy = store.find(key).block();
        assertNotNull("Bean not correctly saved!", beanCopy);

        if (bean == beanCopy) { //Same ref? Likely in memory store
            passedTestTotal++;
            return;
        }

        assertFalse("Transient field serialized!", beanCopy.isOriginal());
        assertEquals("Same beans are not equivalent!", bean, beanCopy);
        passedTestTotal++;
    }

    //Primitive store tests

    public static class TestBean implements Serializable {

        private static final long serialVersionUID = 629911716178802723L;

        transient boolean isOriginal = false;

        long someLong;
        long[] someLongArray;
        @Nullable Long someLongObject;
        int someInt;
        boolean someBoolean;
        @Nullable String someString;
        @Nullable AnotherBean someObject;

        public static TestBean getBean() {
            TestBean bean = new TestBean();
            bean.isOriginal = true;
            return bean;
        }

        public TestBean() {}

        public boolean isOriginal() {
            return isOriginal;
        }

        public long getSomeLong() {
            return someLong;
        }

        public void setSomeLong(long someLong) {
            this.someLong = someLong;
        }

        public long[] getSomeLongArray() {
            return someLongArray;
        }

        public void setSomeLongArray(long[] someLongArray) {
            this.someLongArray = someLongArray;
        }

        @Nullable
        public Long getSomeLongObject() {
            return someLongObject;
        }

        public void setSomeLongObject(@Nullable Long someLongObject) {
            this.someLongObject = someLongObject;
        }

        public int getSomeInt() {
            return someInt;
        }

        public void setSomeInt(int someInt) {
            this.someInt = someInt;
        }

        public boolean isSomeBoolean() {
            return someBoolean;
        }

        public void setSomeBoolean(boolean someBoolean) {
            this.someBoolean = someBoolean;
        }

        @Nullable
        public String getSomeString() {
            return someString;
        }

        public void setSomeString(@Nullable String someString) {
            this.someString = someString;
        }

        @Nullable
        public AnotherBean getSomeObject() {
            return someObject;
        }

        public void setSomeObject(@Nullable AnotherBean someObject) {
            this.someObject = someObject;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof TestBean)) {
                return false;
            }
            TestBean bean = (TestBean) o;
            return isOriginal() == bean.isOriginal() &&
                    getSomeLong() == bean.getSomeLong() &&
                    getSomeInt() == bean.getSomeInt() &&
                    isSomeBoolean() == bean.isSomeBoolean() &&
                    Arrays.equals(getSomeLongArray(), bean.getSomeLongArray()) &&
                    Objects.equals(getSomeLongObject(), bean.getSomeLongObject()) &&
                    Objects.equals(getSomeString(), bean.getSomeString()) &&
                    Objects.equals(getSomeObject(), bean.getSomeObject());
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(isOriginal(), getSomeLong(), getSomeLongObject(), getSomeInt(), isSomeBoolean(), getSomeString(), getSomeObject());
            result = 31 * result + Arrays.hashCode(getSomeLongArray());
            return result;
        }

        @Override
        public String toString() {
            return "TestBean{" +
                    "isOriginal=" + isOriginal +
                    ", someLong=" + someLong +
                    ", someLongArray=" + Arrays.toString(someLongArray) +
                    ", someLongObject=" + someLongObject +
                    ", someInt=" + someInt +
                    ", someBoolean=" + someBoolean +
                    ", someString='" + someString + '\'' +
                    ", someObject=" + someObject +
                    '}';
        }
    }

    public static class AnotherBean implements Serializable {

        private static final long serialVersionUID = 2422061064093555299L;

        long someLong;
        long[] someLongArray;
        @Nullable Long someLongObject;
        int someInt;
        boolean someBoolean;
        @Nullable String someString;

        public AnotherBean() {}

        public long getSomeLong() {
            return someLong;
        }

        public void setSomeLong(long someLong) {
            this.someLong = someLong;
        }

        public long[] getSomeLongArray() {
            return someLongArray;
        }

        public void setSomeLongArray(long[] someLongArray) {
            this.someLongArray = someLongArray;
        }

        @Nullable
        public Long getSomeLongObject() {
            return someLongObject;
        }

        public void setSomeLongObject(@Nullable Long someLongObject) {
            this.someLongObject = someLongObject;
        }

        public int getSomeInt() {
            return someInt;
        }

        public void setSomeInt(int someInt) {
            this.someInt = someInt;
        }

        public boolean isSomeBoolean() {
            return someBoolean;
        }

        public void setSomeBoolean(boolean someBoolean) {
            this.someBoolean = someBoolean;
        }

        @Nullable
        public String getSomeString() {
            return someString;
        }

        public void setSomeString(@Nullable String someString) {
            this.someString = someString;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof AnotherBean)) {
                return false;
            }
            AnotherBean that = (AnotherBean) o;
            return getSomeLong() == that.getSomeLong() &&
                    getSomeInt() == that.getSomeInt() &&
                    isSomeBoolean() == that.isSomeBoolean() &&
                    Arrays.equals(getSomeLongArray(), that.getSomeLongArray()) &&
                    Objects.equals(getSomeLongObject(), that.getSomeLongObject()) &&
                    Objects.equals(getSomeString(), that.getSomeString());
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(getSomeLong(), getSomeLongObject(), getSomeInt(), isSomeBoolean(), getSomeString());
            result = 31 * result + Arrays.hashCode(getSomeLongArray());
            return result;
        }

        @Override
        public String toString() {
            return "AnotherBean{" +
                    "someLong=" + someLong +
                    ", someLongArray=" + Arrays.toString(someLongArray) +
                    ", someLongObject=" + someLongObject +
                    ", someInt=" + someInt +
                    ", someBoolean=" + someBoolean +
                    ", someString='" + someString + '\'' +
                    '}';
        }
    }
}
