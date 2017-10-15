/**
 * 
 */
package com.zhazhapan.qiniu;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author pantao
 *
 */
public class ThreadPool {

	public static ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 10, 5, TimeUnit.SECONDS,
			new LinkedBlockingDeque<Runnable>(1), new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					return new Thread(r);
				}
			});

	public static void shutdown() {
		executor.shutdown();
	}

	public static void shutdownNow() {
		executor.shutdownNow();
	}
}
