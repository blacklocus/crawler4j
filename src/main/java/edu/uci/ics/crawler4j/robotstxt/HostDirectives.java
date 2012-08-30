/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uci.ics.crawler4j.robotstxt;

/**
 * @author Yasser Ganjisaffar <lastname at gmail dot com>
 */
public class HostDirectives {

	// If we fetched the directives for this host more than
	// 24 hours, we have to re-fetch it.
	private static final long EXPIRATION_DELAY = 24 * 60 * 1000L;

    private static final Long DEFAULT_CRAWL_DELAY = 0L; // No directive means no limit as far as robots parsing is concerned.

	private RuleSet disallows = new RuleSet();
	private RuleSet allows = new RuleSet();

    private Long crawlDelay = null;
    private volatile Long nextAllowedCrawl = System.currentTimeMillis();

	private long timeFetched;
	private long timeLastAccessed;

	public HostDirectives() {
		timeFetched = System.currentTimeMillis();
	}

	public boolean needsRefetch() {
		return (System.currentTimeMillis() - timeFetched > EXPIRATION_DELAY);
	}

    /**
     * For 'Crawl-delay' directives, calling this method will increment the time delay counter, regardless of whether
     * or not a subsequent fetch of the given url actually happens. i.e. only call this method once per url being
     * checked for fetch-ability.
     *
     * @param path
     * @return <code>null</code> if the path is never allowed. Otherwise a suggested delay in ms. In the case there
     *         was no 'Crawl-delay' directive parsed from the robots.txt, allowed paths will always return a suggested delay
     *         of 0ms.
     */
    public Long allowedIn(String path) {
        timeLastAccessed = System.currentTimeMillis();
        if (!disallows.containsPrefixOf(path) || allows.containsPrefixOf(path)) {
            return getDelayUntilNextAllowedCrawl(); // allowed after perhaps a non-0 suggested delay
        } else {
            return null; // never allowed
        }
    }

    public void addDisallow(String path) {
		disallows.add(path);
	}

	public void addAllow(String path) {
		allows.add(path);
	}
	
	public long getLastAccessTime() {
		return timeLastAccessed;
	}

    public void setCrawlDelay(long crawlDelayMs) {
        this.crawlDelay = crawlDelayMs;
    }

    /**
     * Gets suggested wait time before next crawl. Invoking this method assumes that a crawl will be scheduled at the
     * suggested delay and makes an internal note when, if asked again, the next delay should be, which is based on
     * the value set by {@link #setCrawlDelay(long)}.
     * <p/>
     * Example: This is a fresh instance with crawl delay of 5 seconds. 4 requests to this method are made
     * simultaneously. The returned values should be 0ms, 5000ms, 10000ms, and 15000ms .
     *
     * @return gets the suggested delay before the next crawl to this domain, 0 if no delay is necessary
     */
    public long getDelayUntilNextAllowedCrawl() {
        long delay = DEFAULT_CRAWL_DELAY;
        // Bypass crawl delay aggregation if no such directive was parsed.
        if (crawlDelay != null) {
            synchronized (nextAllowedCrawl) {
                long currentTime = System.currentTimeMillis();
                if (currentTime < nextAllowedCrawl) {
                    delay = nextAllowedCrawl - currentTime;
                    nextAllowedCrawl += crawlDelay;
                } else {
                    delay = 0L;
                    nextAllowedCrawl = currentTime + crawlDelay;
                }
            }
        }
        return delay;
    }
}