/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.parmenion.libraries.transakt;

import java.util.NoSuchElementException;
import java.util.Objects;

public final class AuthOptional<T> {
    /**
     * Common instance for {@code empty()}.
     */
    private static final AuthOptional<?> EMPTY = new AuthOptional<>();

    /**
     * If non-null, the value; if null, indicates no value is present
     */
    private final T value;

    /**
     * Constructs an empty instance.
     *
     * @implNote Generally only one empty instance, {@link AuthOptional#EMPTY},
     * should exist per VM.
     */
    private AuthOptional() {
        this.value = null;
    }

    /**
     * Returns an empty {@code AuthOptional} instance.  No value is present for this
     * AuthOptional.
     *
     * @apiNote Though it may be tempting to do so, avoid testing if an object
     * is empty by comparing with {@code ==} against instances returned by
     * {@code Option.empty()}. There is no guarantee that it is a singleton.
     * Instead, use {@link #isPresent()}.
     *
     * @param <T> Type of the non-existent value
     * @return an empty {@code AuthOptional}
     */
    public static<T> AuthOptional<T> empty() {
        @SuppressWarnings("unchecked")
        AuthOptional<T> t = (AuthOptional<T>) EMPTY;
        return t;
    }

    /**
     * Constructs an instance with the value present.
     *
     * @param value the non-null value to be present
     * @throws NullPointerException if value is null
     */
    private AuthOptional(T value) {
        this.value = Objects.requireNonNull(value);
    }

    /**
     * Returns an {@code AuthOptional} with the specified present non-null value.
     *
     * @param <T> the class of the value
     * @param value the value to be present, which must be non-null
     * @return an {@code AuthOptional} with the value present
     * @throws NullPointerException if value is null
     */
    public static <T> AuthOptional<T> of(T value) {
        return new AuthOptional<>(value);
    }

    /**
     * Returns an {@code AuthOptional} describing the specified value, if non-null,
     * otherwise returns an empty {@code AuthOptional}.
     *
     * @param <T> the class of the value
     * @param value the possibly-null value to describe
     * @return an {@code AuthOptional} with a present value if the specified value
     * is non-null, otherwise an empty {@code AuthOptional}
     */
    public static <T> AuthOptional<T> ofNullable(T value) {
        if (value == null){
            return empty();
        } else {
           return of(value);
        }
    }

    /**
     * If a value is present in this {@code AuthOptional}, returns the value,
     * otherwise throws {@code NoSuchElementException}.
     *
     * @return the non-null value held by this {@code AuthOptional}
     * @throws NoSuchElementException if there is no value present
     *
     * @see AuthOptional#isPresent()
     */
    public T get() {
        if (value == null) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    /**
     * Return {@code true} if there is a value present, otherwise {@code false}.
     *
     * @return {@code true} if there is a value present, otherwise {@code false}
     */
    public boolean isPresent() {
        return value != null;
    }



    /**
     * Return the value if present, otherwise return {@code other}.
     *
     * @param other the value to be returned if there is no value present, may
     * be null
     * @return the value, if present, otherwise {@code other}
     */
    public T orElse(T other) {
        return value != null ? value : other;
    }



    /**
     * Indicates whether some other object is "equal to" this AuthOptional. The
     * other object is considered equal if:
     * <ul>
     * <li>it is also an {@code AuthOptional} and;
     * <li>both instances have no value present or;
     * <li>the present values are "equal to" each other via {@code equals()}.
     * </ul>
     *
     * @param obj an object to be tested for equality
     * @return {code true} if the other object is "equal to" this object
     * otherwise {@code false}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AuthOptional)) {
            return false;
        }

        AuthOptional<?> other = (AuthOptional<?>) obj;
        return Objects.equals(value, other.value);
    }

    /**
     * Returns the hash code value of the present value, if any, or 0 (zero) if
     * no value is present.
     *
     * @return hash code value of the present value or 0 if no value is present
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    /**
     * Returns a non-empty string representation of this AuthOptional suitable for
     * debugging. The exact presentation format is unspecified and may vary
     * between implementations and versions.
     *
     * @implSpec If a value is present the result must include its string
     * representation in the result. Empty and present Optionals must be
     * unambiguously differentiable.
     *
     * @return the string representation of this instance
     */
    @Override
    public String toString() {
        return value != null
            ? String.format("AuthOptional[%s]", value)
            : "AuthOptional.empty";
    }
}
