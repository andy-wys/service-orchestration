package org.andy.so.core.util;

/**
 * 拷贝自 org.apache.commons:commons-lang3:3.12.0 包，以减少包依赖
 *
 * @author: andy
 */
@SuppressWarnings("unused")
public final class SoObjectUtil {
    /**
     * Checks if all values in the array are not {@code nulls}.
     *
     * @param values the values to test, may be {@code null} or empty
     * @return {@code false} if there is at least one {@code null} value in the array or the array is {@code null},
     * {@code true} if all values in the array are not {@code null}s or array contains no elements.
     */
    public static boolean allNotNull(final Object... values) {
        if (values == null) {
            return false;
        }

        for (final Object val : values) {
            if (val == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if all values in the given array are {@code null}.
     *
     * @param values the values to test, may be {@code null} or empty
     * @return {@code true} if all values in the array are {@code null}s,
     * {@code false} if there is at least one non-null value in the array.
     */
    public static boolean allNull(final Object... values) {
        return !anyNotNull(values);
    }

    /**
     * Checks if any value in the given array is not {@code null}.
     *
     * @param values the values to test, may be {@code null} or empty
     * @return {@code true} if there is at least one non-null value in the array,
     * {@code false} if all values in the array are {@code null}s.
     * If the array is {@code null} or empty {@code false} is also returned.
     */
    public static boolean anyNotNull(final Object... values) {
        return firstNonNull(values) != null;
    }

    /**
     * Checks if any value in the given array is {@code null}.
     *
     * @param values the values to test, may be {@code null} or empty
     * @return {@code true} if there is at least one {@code null} value in the array,
     * {@code false} if all the values are non-null.
     * If the array is {@code null} or empty, {@code true} is also returned.
     */
    public static boolean anyNull(final Object... values) {
        return !allNotNull(values);
    }

    /**
     * <p>Returns the first value in the array which is not {@code null}.
     * If all the values are {@code null} or the array is {@code null}
     * or empty then {@code null} is returned.</p>
     *
     * <pre>
     * ObjectUtils.firstNonNull(null, null)      = null
     * ObjectUtils.firstNonNull(null, "")        = ""
     * ObjectUtils.firstNonNull(null, null, "")  = ""
     * ObjectUtils.firstNonNull(null, "zz")      = "zz"
     * ObjectUtils.firstNonNull("abc", *)        = "abc"
     * ObjectUtils.firstNonNull(null, "xyz", *)  = "xyz"
     * ObjectUtils.firstNonNull(Boolean.TRUE, *) = Boolean.TRUE
     * ObjectUtils.firstNonNull()                = null
     * </pre>
     *
     * @param <T>    the component type of the array
     * @param values the values to test, may be {@code null} or empty
     * @return the first value from {@code values} which is not {@code null},
     * or {@code null} if there are no non-null values
     * @since 3.0
     */
    @SafeVarargs
    public static <T> T firstNonNull(final T... values) {
        if (values != null) {
            for (final T val : values) {
                if (val != null) {
                    return val;
                }
            }
        }
        return null;
    }
}
