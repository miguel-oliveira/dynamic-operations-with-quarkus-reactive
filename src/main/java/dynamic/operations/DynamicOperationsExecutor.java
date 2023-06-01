package dynamic.operations;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Function;

@ApplicationScoped
public class DynamicOperationsExecutor {

  private static final String START_SEQUENTIAL = "sequential";
  private static final String START_CONCURRENT = "concurrent";

  private final OperationsService operationsService;
  private final Executor executor;

  public DynamicOperationsExecutor(
	  final OperationsService operationsService,
	  final Executor executor
  ) {
	this.operationsService = operationsService;
	this.executor = executor;
  }

  public Uni<String> executeSequential(final List<String> operations) {
	final Function<String, String> chainOfOperations = operationsService.buildChainOf(operations);
	return executeSequential(chainOfOperations);
  }

  private Uni<String> executeSequential(final Function<String, String> chainOfOperations) {
	return Uni
		.createFrom()
		.item(chainOfOperations)
		.onItem()
		.transform(start -> start.apply(START_SEQUENTIAL))
		.runSubscriptionOn(executor);
  }

  public Uni<List<String>> executeConcurrent(final List<String> operations) {
	final List<Uni<String>> operationEventStream = generateEventStreamOf(operations);
	return Uni.join().all(operationEventStream).andCollectFailures();
  }

  private List<Uni<String>> generateEventStreamOf(final List<String> operations) {
	return operations
		.stream()
		.map(operationsService::get)
		.map(this::createUniFrom)
		.toList();
  }

  private Uni<String> createUniFrom(final Function<String, String> operation) {
	return Uni.createFrom()
		.item(START_CONCURRENT)
		.onItem()
		.transform(operation)
		.runSubscriptionOn(executor);
  }
}
