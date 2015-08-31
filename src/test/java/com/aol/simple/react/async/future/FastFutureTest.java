package com.aol.simple.react.async.future;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import io.netty.util.internal.chmv8.ForkJoinPool;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Test;

public class FastFutureTest {
	PipelineBuilder future;

	@Before
	public void setup() {
		failed = null;
		future = new PipelineBuilder();
		sizes = new ArrayList<>();
		sizes2 = new ArrayList<>();
		sizes3 = new ArrayList<>();
	}

	Throwable failed;

	@Test
	public void onFail() {
		FastFuture f = future.onFail(t -> failed = t).thenApply(v -> {
			throw new RuntimeException();
		}).build();

		f.set("boo!");
		assertNotNull(failed);
	}

	@Test
	public void onFailRecovered() {
		FastFuture f = future.onFail(t -> failed = t)
				.exceptionally(e -> "hello world")
				.thenApply(v -> {
					throw new RuntimeException();
				}).build();

		f.set("boo!");
		assertThat(f.join(), equalTo("hello world"));
	}

	@Test
	public void onFailRecovered2() {
		FastFuture f = future.onFail(t -> failed = t)
				.exceptionally(e -> {
					if(e instanceof IOException) 
						return "hello world";
					throw (RuntimeException)e;
				})
				.exceptionally(e -> {
					if(e instanceof RuntimeException) 
						return "hello world2";
					throw (RuntimeException)e;
				})
				.thenApply(v -> {
					throw new RuntimeException();
				}).build();

		f.set("boo!");
		assertThat(f.join(), equalTo("hello world2"));
	}
	@Test
	public void onFailRecovered3() {
		FastFuture f = future.onFail(t -> failed = t)
				.exceptionally(e -> {
					if(e instanceof IOException) 
						return "hello world";
					throw (RuntimeException)e;
				})
				.exceptionally(e -> {
					if(e instanceof FileNotFoundException) 
						return "hello world2";
					throw (RuntimeException)e;
				})
				.exceptionally(e -> {
					if(e instanceof RuntimeException) 
						return "hello world3";
					throw (RuntimeException)e;
				})
				.thenApply(v -> {
					throw new RuntimeException();
				}).build();

		f.set("boo!");
		assertThat(f.join(), equalTo("hello world3"));
	}
	@Test
	public void onFailNull() {
		FastFuture f = future.thenApply(v -> {
			throw new RuntimeException();
		}).build();

		f.set("boo!");
		assertNull(failed);
	}

	@Test
	public void firstRecover() {
		FastFuture f = future.exceptionally(e -> "hello world")
				.thenApply(v -> {
					throw new RuntimeException();
				}).build();

		f.set("boo!");
		assertThat(f.join(), equalTo("hello world"));

	}

	@Test
	public void firstRecoverOrder() {
		FastFuture f = future.thenApply(v -> {
			throw new RuntimeException();
		}).exceptionally(e -> "hello world").build();

		f.set("boo!");
		assertThat(f.join(), equalTo("hello world"));

	}

	List<Integer> sizes;
	List<Integer> sizes2;
	List<Integer> sizes3;

	@Test
	public void testThenApplyAsync() {
		try {
			future = future
					.<String, String> thenApplyAsync(String::toUpperCase,
							ForkJoinPool.commonPool())
					.peek(System.out::println)
					.<String, Integer> thenApply(s -> s.length())
					.thenApply(l -> {
						sizes.add((Integer) l);
						return l;
					})
					.peek(System.out::println)
					.<Integer, Integer> thenApplyAsync(l -> l + 2,
							ForkJoinPool.commonPool())
					.peek(System.out::println)
					.<Integer, Integer> thenApply(l -> {
						sizes2.add(l);
						return l;
					})
					.<Integer, Integer> thenApplyAsync(l -> l + 2,
							ForkJoinPool.commonPool())
					.peek(System.out::println)

					.<Integer, Integer> thenApply(l -> {
						sizes3.add(l);
						return l;
					});
			StringBuilder suffix = new StringBuilder();
			for (int i = 0; i < 100; i++) {

				FastFuture f2 = future.build();

				f2.set("hello world" + suffix.toString());
				f2.join();
				FastFuture f3 = future.build();
				f3.set("hello world2" + suffix.toString());
				f3.join();
				suffix.append("" + i);
			}
			for (int i = 0; i < 11; i++) {
				assertFalse(sizes.contains(i));
			}
			for (int i = 11; i < 201; i++) {
				assertTrue(sizes.contains(i));
			}
			for (int i = 201; i < 211; i++) {
				assertFalse(sizes.contains(i));
			}
			System.out.println(sizes);
			System.out.println(sizes2);
			System.out.println(sizes3);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	boolean called = false;

	@Test
	public void onComplete_alreadyCompleted() {
		called = false;
		FastFuture f = future.<Integer, Integer> thenApply(i -> i + 2).build();
		f.set(10);
		assertTrue(f.isDone());
		f.onComplete(event -> called = true);
		assertTrue(called);
	}

	@Test
	public void onComplete_notCompleted() {
		called = false;
		FastFuture f = future.<Integer, Integer> thenApply(i -> i + 2).build();
		f.onComplete(event -> called = true);
		f.set(10);
		assertTrue(f.isDone());

		assertTrue(called);
	}

	@Test
	public void essential_alreadyCompleted() {
		called = false;
		FastFuture f = future.<Integer, Integer> thenApply(i -> i + 2).build();
		f.set(10);
		assertTrue(f.isDone());
		f.essential(event -> called = true);
		assertTrue(called);
	}

	@Test
	public void essential_notCompleted() {
		called = false;
		FastFuture f = future.<Integer, Integer> thenApply(i -> i + 2).build();
		f.essential(event -> called = true);
		f.set(10);
		assertTrue(f.isDone());

		assertTrue(called);
	}

	@Test
	public void essential_notCompleted_race() throws InterruptedException {
		for (int i = 0; i < 100; i++) {
			CountDownLatch race = new CountDownLatch(1);
			CountDownLatch init = new CountDownLatch(1);
			called = false;
			FastFuture f = future.<Integer, Integer> thenApply(x -> x + 2)
					.build();
			Thread t1 = new Thread(() -> {
				init.countDown();
				try {
					race.await();
				} catch (Exception e) {

					e.printStackTrace();
				}

				f.essential(event -> called = true);
			});
			t1.start();
			race.countDown();
			f.set(10);

			t1.join();
			assertTrue(f.isDone());

			assertTrue(called);
		}

	}

	@Test
	public void essentialReversed_notCompleted_race()
			throws InterruptedException {
		for (int i = 0; i < 100; i++) {
			System.out.println(i);
			CountDownLatch race = new CountDownLatch(1);
			CountDownLatch init = new CountDownLatch(1);
			called = false;
			FastFuture f = future.<Integer, Integer> thenApply(x -> x + 2)
					.build();
			Thread t1 = new Thread(() -> {
				init.countDown();
				try {
					race.await();
				} catch (Exception e) {

					e.printStackTrace();
				}

				f.set(10);

			});
			t1.start();
			race.countDown();
			f.essential(event -> called = true);

			t1.join();
			assertTrue(f.isDone());

			assertTrue(called);
		}

	}

	@Test
	public void essential_withNoPipeline_notCompleted_race()
			throws InterruptedException {
		for (int i = 0; i < 100; i++) {
			CountDownLatch race = new CountDownLatch(1);
			CountDownLatch init = new CountDownLatch(1);
			called = false;
			FastFuture f = future.build();
			Thread t1 = new Thread(() -> {
				init.countDown();
				try {
					race.await();
				} catch (Exception e) {

					e.printStackTrace();
				}

				f.essential(event -> called = true);
			});
			t1.start();
			race.countDown();
			f.set(10);

			t1.join();
			assertTrue(f.isDone());

			assertTrue(called);
		}

	}

	@Test
	public void onComplete_notCompleted_race() throws InterruptedException {
		for (int i = 0; i < 100; i++) {
			CountDownLatch race = new CountDownLatch(1);
			CountDownLatch init = new CountDownLatch(1);
			called = false;
			FastFuture f = future.<Integer, Integer> thenApply(x -> x + 2)
					.build();
			Thread t1 = new Thread(() -> {
				init.countDown();
				try {
					race.await();
				} catch (Exception e) {

					e.printStackTrace();
				}

				f.onComplete(event -> called = true);
			});
			t1.start();
			race.countDown();
			f.set(10);

			t1.join();
			assertTrue(f.isDone());

			assertTrue(called);
		}

	}

	@Test
	public void onCompleteReversed_notCompleted_race()
			throws InterruptedException {
		for (int i = 0; i < 100; i++) {
			CountDownLatch race = new CountDownLatch(1);
			CountDownLatch init = new CountDownLatch(1);
			called = false;
			FastFuture f = future.<Integer, Integer> thenApply(x -> x + 2)
					.build();
			Thread t1 = new Thread(() -> {
				init.countDown();
				try {
					race.await();
				} catch (Exception e) {

					e.printStackTrace();
				}

				f.set(10);
			});
			t1.start();
			race.countDown();
			f.onComplete(event -> called = true);

			t1.join();
			assertTrue(f.isDone());

			assertTrue(called);
		}

	}

	@Test
	public void onCompleteWithNoPipeline_notCompleted_race()
			throws InterruptedException {
		for (int i = 0; i < 100; i++) {
			CountDownLatch race = new CountDownLatch(1);
			CountDownLatch init = new CountDownLatch(1);
			called = false;
			FastFuture f = future.build();
			Thread t1 = new Thread(() -> {
				init.countDown();
				try {
					race.await();
				} catch (Exception e) {

					e.printStackTrace();
				}

				f.onComplete(event -> called = true);
			});
			t1.start();
			race.countDown();
			f.set(10);

			t1.join();
			assertTrue(f.isDone());

			assertTrue(called);
		}

	}

}