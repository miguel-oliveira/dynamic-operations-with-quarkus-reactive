package dynamic.operations;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class DynamicOperationsService {

  private final Logger LOGGER = Logger.getLogger(DynamicOperationsService.class.getName());
  private static final String START_SEQUENTIAL = "sequential";
  private static final String START_CONCURRENT = "concurrent";

  private final Executor executor;
  private final Map<String, Function<String, String>> operations;

  public DynamicOperationsService(final Executor executor) {
	this.executor = executor;
	this.operations = new HashMap<>();
	operations.put("a", this::operationA);
	operations.put("b", this::operationB);
	operations.put("c", this::operationC);
  }

  public Uni<String> executeSequential(final List<String> operations) {
	final Function<String, String> chainOfOperations = buildChainOfOperations(operations);
	return executeSequential(chainOfOperations);
  }

  private Function<String, String> buildChainOfOperations(final List<String> operations) {
	return operations
		.stream()
		.map(this::getOperation)
		.reduce(Function.identity(), Function::andThen);
  }

  private Function<String, String> getOperation(final String operation) {
	validate(operation);
	return operations.get(operation);
  }

  private void validate(final String operation) {
	if (!operations.containsKey(operation)) {
	  throw new NotFoundException(String.format("Operation %s not found!", operation));
	}
  }

  private Uni<String> executeSequential(final Function<String, String> chainOfOperations) {
	return Uni
		.createFrom()
		.item(chainOfOperations)
		.onItem()
		.transform(start -> start.apply(START_SEQUENTIAL))
		.runSubscriptionOn(executor);
  }

  private String operationA(final String previous) {
	final String current = "operationA";
	return operateOn(previous, current);
  }

  private String operateOn(final String previous, final String current) {
	log(previous, current);
	return previous + " -> " + current;
  }

  private void log(final String previous, final String current) {
	LOGGER.log(Level.INFO, "{0} -> {1}", new Object[]{previous, current});
  }

  private String operationB(final String previous) {
	final String current = "operationB";
	return operateOn(previous, current);
  }

  private String operationC(final String previous) {
	final String current = "operationC";
	return operateOn(previous, current);
  }

  public Uni<List<String>> executeConcurrent(final List<String> operations) {
	final List<Multi<String>> operationEventStream = generateOperationEventStream(operations);
	return Multi.createBy().merging().streams(operationEventStream).collect().asList();
  }

  private List<Multi<String>> generateOperationEventStream(final List<String> operations) {
	return operations
		.stream()
		.map(this::getOperation)
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