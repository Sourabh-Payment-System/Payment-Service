package payment.system.app.config;

import lombok.extern.slf4j.Slf4j;

import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WalletRetryListener implements RetryListener {
	@Override
	public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {

		log.info("Starting retryable wallet operation");

		return true;
	}

	@Override
	public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback,
			Throwable throwable) {

		log.warn("Retry attempt={} exceptionType={} message={}", context.getRetryCount(),
				throwable.getClass().getSimpleName(), throwable.getMessage());
	}

	@Override
	public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback,
			Throwable throwable) {

		if (throwable != null) {

			log.error("Retry operation finished with exception after {} retry attempts", context.getRetryCount(),
					throwable);
		}
	}
}