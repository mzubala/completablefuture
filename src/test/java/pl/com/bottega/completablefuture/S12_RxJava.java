package pl.com.bottega.completablefuture;

import io.reactivex.Observable;
import pl.com.bottega.completablefuture.util.AbstractFuturesTest;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

public class S12_RxJava extends AbstractFuturesTest {

	public static final String MSG = "Don't panic";

	@Test
	public void shouldConvertCompletedFutureToCompletedObservable() throws Exception {
		//given
		CompletableFuture<String> future = CompletableFuture.completedFuture("Abc");

		//when
		Observable<String> observable = Observable.fromFuture(future);

		//then
		assertThat(observable.toList().blockingGet()).containsExactly("Abc");
	}

	@Test
	public void shouldConvertFailedFutureIntoObservableWithFailure() throws Exception {
		//given
		CompletableFuture<String> future = failedFuture(new IllegalStateException(MSG));

		//when
		Observable<String> observable = Observable.fromFuture(future);

		//then
		final List<String> result = observable
				.onErrorReturn(Throwable::getMessage)
				.toList()
				.blockingGet();
		assertThat(result.get(0)).contains(MSG);
	}

	@Test
	public void shouldConvertObservableWithManyItemsToFutureOfList() throws Exception {
		//given
		Observable<Integer> observable = Observable.just(1, 2, 3);

		//when
		Future<List<Integer>> future = observable.toList().toFuture();

		//then
		assertThat(future.get(1, SECONDS)).containsExactly(1, 2, 3);
	}

	@Test
	public void shouldConvertObservableWithSingleItemToFuture() throws Exception {
		//given
		Observable<Integer> observable = Observable.just(1);

		//when
		Future<Integer> future = observable.toFuture();

		//then
		assertThat(future.get(1, SECONDS)).isEqualTo(1);
	}

	<T> CompletableFuture<T> failedFuture(Exception error) {
		CompletableFuture<T> future = new CompletableFuture<>();
		future.completeExceptionally(error);
		return future;
	}
}

