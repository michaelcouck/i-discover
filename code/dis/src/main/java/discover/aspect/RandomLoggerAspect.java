package discover.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Value;

import java.util.Random;

/**
 * @author Michael Couck
 * @since 16-10-2016
 */
public class RandomLoggerAspect {

    private Random random;
    @Value("${probability:10}")
    private int probability = 10;

    public RandomLoggerAspect() {
        random = new Random();
    }

    public Object log(final ProceedingJoinPoint proceedingJoinPoint) {
        if (random.nextInt(10000) % probability == 0) {
            try {
                return proceedingJoinPoint.proceed();
            } catch (final Throwable throwable) {
                throwable.printStackTrace();
            }
        }
        return Boolean.FALSE;
    }

    public void setProbability(int probability) {
        this.probability = probability;
    }

}
