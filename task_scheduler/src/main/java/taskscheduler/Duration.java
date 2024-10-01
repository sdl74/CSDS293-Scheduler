package taskscheduler;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalTime;

// this class represents an amount of time in milliseconds
public final class Duration implements Serializable{
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
        // check for null
        if(other == null)
            throw new NullPointerException("Cannot copy null duration");

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

    // helper method to convert the Duration to a long representing the amount of milliseconds
    public long toMillis(){
        return millis.longValue();
    }

    // helper method to find the amount of time between two LocalTimes and return it as a Duration
    public static Duration timeBetween(LocalTime time1, LocalTime time2){
        // separate null checks
        // check for null values
        if(time1 == null || time2 == null)
            throw new NullPointerException("LocalTime provided cannot be null");

        // ensure that time2 > time1
        if(time1.compareTo(time2) > 0){
            LocalTime temp = time1;
            time1 = time2;
            time2 = temp;
        }

        // calculate the time between the localTimes
        return Duration.ofMillis(time1.until(time2, java.time.temporal.ChronoUnit.MILLIS));
    }
}