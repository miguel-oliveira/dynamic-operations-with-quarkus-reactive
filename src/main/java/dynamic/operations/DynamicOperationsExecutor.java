package dynamic.operations;

import io.smallrye.mutiny.Multi;
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
	final List<Multi<String>> operationEventStream = generateEventStreamOf(operations);
	return Multi.createBy().merging().streams(operationEventStream).collect().asList();
  }

  private List<Multi<String>> generateEventStreamOf(final List<String> operations) {
	return operations
		.stream()
		.map(operationsService::get)
		.map(this::createMultiFrom)
		.toList();
  }

  private Multi<String> createMultiFrom(final Function<String, String> operation) {
	return Multi.createFrom()
		.item(START_CONCURRENT)
		.onItem()
		.transform(operation)
		.runSubscriptionOn(executor);
  }
}