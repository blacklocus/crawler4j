package edu.uci.ics.crawler4j.robotstxt;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author jason
 */
public class RobotstxtParserTest {

    @Test
    public void testCrawlDelayParse() {
        HostDirectives hostDirectives = RobotstxtParser.parse("User-agent: *\nCrawl-delay: 5", null);
        Assert.assertEquals(0L, hostDirectives.getDelayUntilNextAllowedCrawl());
        Assert.assertTrue(hostDirectives.getDelayUntilNextAllowedCrawl() > 4500L); // should be very close to 5000 ms
    }
}
