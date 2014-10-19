package org.projectbuendia.utils;

/**
 * Thrown when external input has an invalid type, value, or format.
 *
 * Use this exception instead of IllegalArgumentException to indicate that
 * there is an error in external input.  This exception is checked, meaning
 * that the program is expected to recover from the error.
 * IllegalArgumentException, on the other hand, should be used to indicate
 * that a fault has occurred in the program.  An IllegalArgumentException is
 * unchecked, meaning that the problem is unrecoverable.
 */
public class InvalidInputException extends Exception {
}
