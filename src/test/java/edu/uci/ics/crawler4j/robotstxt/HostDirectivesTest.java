package edu.uci.ics.crawler4j.robotstxt;

import junit.framework.Assert;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author jason
 */
public class HostDirectivesTest {

    @Test
    public void testDelay() throws InterruptedException {
        final HostDirectives hostDirectives = new HostDirectives();
        hostDirectives.setCrawlDelay(5000L);

        final Set<Long> delays = Collections.synchronizedSet(new TreeSet<Long>());

        final Random random = new Random(System.currentTimeMillis());

        ExecutorService executorService = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 4; i++) {
            executorService.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    Thread.sleep(random.nextInt(100));
                    delays.add(hostDirectives.getDelayUntilNextAllowedCrawl());
                    return null;
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(1000L, TimeUnit.SECONDS);

        Iterator<Long> it = delays.iterator();
        Long lastDelay = it.next();
        Assert.assertEquals(Long.valueOf(0L), lastDelay);
        while (it.hasNext()) {
            Long delay = it.next();
            // There's some curbing going on that makes the delays slightly less than 5 seconds, so be fuzzy on the window
            Assert.assertTrue(lastDelay + 4000L < delay);
            lastDelay = delay;
        }

    }
}
