package com.turkcell.soccer.aspect;

import com.turkcell.soccer.annotation.RateLimit;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.turkcell.soccer.exception.RateLimitExceededException;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Aspect
@Component
public class RateLimitAspect {

    private final Map<String, Bucket> buckets =  new ConcurrentHashMap<>();

    @Before("@annotation(rateLimit)")
    public void checkRateLimit(RateLimit rateLimit) {

        String ip = getClientIp();
        String key = ip + "_" + rateLimit.capacity() + "_" + rateLimit.timeInSeconds();

        // Creates a bucket if that IP address has none
        Bucket bucket = buckets.computeIfAbsent(key, k -> createBucket(rateLimit));

        if (!bucket.tryConsume(1)) {
            log.warn("Rate limit exceeded for {}, try again", key);
            throw new RateLimitExceededException("Çok hızlı gidiyorsun! Lütfen biraz bekle.");
        }

    }


    private Bucket createBucket(RateLimit rateLimit) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(rateLimit.capacity()) // capacity of the bucket
                .refillGreedy(rateLimit.capacity(), Duration.ofSeconds(rateLimit.timeInSeconds())) // Refill speed
                .build();

        return Bucket.builder().addLimit(limit).build();
    }




    private String getClientIp() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }


}
