/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.solder.exception.control;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;

import org.jboss.solder.reflection.HierarchyDiscovery;
import org.jboss.solder.exception.control.HandlerMethod;
import org.jboss.solder.exception.control.TraversalMode;

/**
 * Comparator to sort exception handlers according qualifier ({@link TraversalMode#DEPTH_FIRST} first), precedence
 * (highest to lowest) and finally hierarchy (least to most specific).
 */
@SuppressWarnings({"MethodWithMoreThanThreeNegations"})
public final class ExceptionHandlerComparator implements Comparator<HandlerMethod<?>> {

    /**
     * {@inheritDoc}
     */
    public int compare(HandlerMethod<?> lhs, HandlerMethod<?> rhs) {
        if (lhs.equals(rhs)) {
            return 0;
        }

        // Really this is so all handlers are returned in the TreeSet (even if they're of the same type, but one is
        // inbound, the other is outbound
        if (lhs.getExceptionType().equals(rhs.getExceptionType())) {
            final int returnValue = this.comparePrecedence(lhs.getPrecedence(), rhs.getPrecedence(),
                    lhs.getTraversalMode() == TraversalMode.DEPTH_FIRST);
            // Compare number of qualifiers if they exist so handlers that handle the same type
            // are both are returned and not thrown out (order doesn't really matter)
            if (returnValue == 0 && !lhs.getQualifiers().isEmpty()) {
                return -1;
            }

            // Either precedence is non-zero or lhs doesn't have qualifiers so return the precedence compare
            // If it's 0 this is essentially the same handler for our purposes
            return returnValue;
        } else {
            return compareHierarchies(lhs.getExceptionType(), rhs.getExceptionType());
        }

        // Currently we're only looking at one type of traversal mode, if this changes, we'll need
        // to re-add lines to check for this.
    }

    private int compareHierarchies(Type lhsExceptionType, Type rhsExceptionType) {
        HierarchyDiscovery lhsHierarchy = new HierarchyDiscovery(lhsExceptionType);
        Set<Type> lhsTypeclosure = lhsHierarchy.getTypeClosure();

        if (lhsTypeclosure.contains(rhsExceptionType)) {
            final int indexOfLhsType = new ArrayList<Type>(lhsTypeclosure).indexOf(lhsExceptionType);
            final int indexOfRhsType = new ArrayList<Type>(lhsTypeclosure).indexOf(rhsExceptionType);

            if (indexOfLhsType > indexOfRhsType) {
                return 1;
            }
        }
        return -1;
    }

    private int comparePrecedence(final int lhs, final int rhs, final boolean isAsc) {
        if (isAsc) {
            return (lhs - rhs);
        } else {
            return (lhs - rhs) * -1;
        }
    }
}
