package taskscheduler;

import java.math.BigInteger;

// this class represents an amount of time in milliseconds
public final class Duration{
    // the duration (in milliseconds)
    private final BigInteger millis;

    // private constructor
    private Duration(BigInteger milliseconds){
        millis = milliseconds;
    }

    // public factory constructor
    public static Duration ofMillis(long milliseconds){
        return new Duration(BigInteger.valueOf(milliseconds));
    }

    // helper method for addition
    public Duration add(Duration other){
        //check for null case (do for other methods as well)

        // calculate the added time
        BigInteger sum = this.millis.add(other.millis);

        // create and return new duration
        return new Duration(sum);
    }

    // helper method for subtraction (performs this - other)
    public Duration subtract(Duration other){
        // calculate difference
        BigInteger diff = this.millis.subtract(other.millis);

        // create and return new duration
        return new Duration(diff);
    }

    // helper method for comparing two Durations (0 if they are the same, 1 if this > other, -1 if this < other)
    public int compareTo(Duration other){
        // use BigInteger compareTo to do the comparison
        return this.millis.compareTo(other.millis);
    }
}