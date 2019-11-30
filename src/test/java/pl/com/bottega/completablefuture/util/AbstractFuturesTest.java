package pl.com.bottega.completablefuture.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.com.bottega.completablefuture.stackoverflow.ArtificialSleepWrapper;
import pl.com.bottega.completablefuture.stackoverflow.FallbackStubClient;
import pl.com.bottega.completablefuture.stackoverflow.HttpStackOverflowClient;
import pl.com.bottega.completablefuture.stackoverflow.InjectErrorsWrapper;
import pl.com.bottega.completablefuture.stackoverflow.LoggingWrapper;
import pl.com.bottega.completablefuture.stackoverflow.StackOverflowClient;

import java.util.concurrent.*;

public class AbstractFuturesTest {

	private static final Logger log = LoggerFactory.getLogger(AbstractFuturesTest.class);

	protected final ExecutorService executorService = Executors.newFixedThreadPool(10, threadFactory("Custom"));

	@Rule
	public TestName testName = new TestName();

	protected ThreadFactory threadFactory(String nameFormat) {
		return new ThreadFactoryBuilder().setNameFormat(nameFormat + "-%d").build();
	}

	protected final StackOverflowClient client = new FallbackStubClient(
			new InjectErrorsWrapper(
					new LoggingWrapper(
							new ArtificialSleepWrapper(
									new HttpStackOverflowClient()
							)
					), "php"
			)
	);

	@Before
	public void logTestStart() {
		log.debug("Starting: {}", testName.getMethodName());
	}

	@After
	public void stopPool() throws InterruptedException {
		executorService.shutdown();
		executorService.awaitTermination(10, TimeUnit.SECONDS);
	}

	protected CompletableFuture<String> questions(String tag) {
		return CompletableFuture.supplyAsync(() ->
				client.mostRecentQuestionAbout(tag),
				executorService);
	}

}
