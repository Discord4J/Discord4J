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

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * This represents a magic exception for signalling that a store contains no value for a query and that the store
 * can be trusted (stores which are unreliable/missing values should just return empty streams instead of throwing
 * this).
 *
 * @see discord4j.store.Store
 * @see discord4j.store.primitive.LongObjStore
 */
public final class AbsentValue extends RuntimeException {

    public static final AbsentValue INSTANCE = new AbsentValue();

    private AbsentValue() {}

    private AbsentValue(String message) {
        super(message);
    }

    private AbsentValue(String message, Throwable cause) {
        super(message, cause);
    }

    private AbsentValue(Throwable cause) {
        super(cause);
    }

    private AbsentValue(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public String getMessage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getLocalizedMessage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Throwable getCause() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Throwable initCause(Throwable cause) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void printStackTrace() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void printStackTrace(PrintStream s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void printStackTrace(PrintWriter s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Throwable fillInStackTrace() {
        throw new UnsupportedOperationException();
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStackTrace(StackTraceElement[] stackTrace) {
        throw new UnsupportedOperationException();
    }
}
