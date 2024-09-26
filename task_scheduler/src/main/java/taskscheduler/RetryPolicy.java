package taskscheduler;

public class RetryPolicy {

    // integer noting how many times a task can be retried before giving up
    private final int maxAttempts;

    // timeFunction to determine how much time to wait between attempts
    private final TimeFunction timeFunction;

    public RetryPolicy(int maxAttemptsValue, TimeFunction timeFunctionValue){
        // initialize value
        maxAttempts = maxAttemptsValue;

        // check for null
        if(timeFunctionValue == null)
            throw new NullPointerException("time function cannot be null");
        
        // initialize value
        timeFunction = timeFunctionValue;
    }

    // getter method for maxAttempts
    public int getMaxAttempts(){
        return maxAttempts;
    }

    // getter method for the delay after x attempts (minimum value is 1 retry, since delay should not be polled for a task that has not been attempted yet)
    public Duration getTimeoutForAttempt(int attemptNum){
        // bounds check
        if(attemptNum < 1)
            throw new IllegalArgumentException("the attempt number must be at least 1");
        
        // poll the delay function
        return timeFunction.getTimeoutFor(attemptNum);
    }

    // a public interface which allows the user to control the delay between retries
    public interface TimeFunction {
        public Duration getTimeoutFor(int attemptNum);
    }

    // a basic, unchanging timeFunction which spits out the same timeout regardless of attempt number
    public static class UniformTimeout implements TimeFunction {

        // tasks will have to wait this amount of time between retries regardless of the number of retries
        private final Duration time;

        // constructor where you provide the delay value
        public UniformTimeout(Duration timeValue){
            // check for null
            if(timeValue == null)
                throw new NullPointerException("timeValue cannot be null");

            time = timeValue;
        }

        // returns the same time every call
        @Override
        public Duration getTimeoutFor(int attemptNum){
            return time;
        }
    }

    // given a starting value, the exponential timeout function doubles the delay with each incremental attempt
    public static class ExponentialTimeout implements TimeFunction {

        // starting delay time
        Duration initialDelay;

        // constructor
        public ExponentialTimeout(Duration initialDelayValue){
            // check for null
            if(initialDelayValue == null)
                throw new NullPointerException("initial delay cannot be null");
            
            // initialize value
            initialDelay = initialDelayValue;
        }
        
        // timeout function doubles the delay value with each new attempt
        @Override
        public Duration getTimeoutFor(int attemptNum){
            // convert time to long so we can do math
            long duration = initialDelay.toMillis();

            // calculates the delay using power function
            duration = duration * (long)Math.pow(2, attemptNum-1);
            
            // convert back to a Duration and return value
            return Duration.ofMillis(duration);
        }
    }
}