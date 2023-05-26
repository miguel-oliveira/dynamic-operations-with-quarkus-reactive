package chain.of.command;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class DynamicChainOfCommandService {

  private final Logger LOGGER = Logger.getLogger(DynamicChainOfCommandService.class.getName());
  private static final String START_SEQUENTIAL = "startSequential";
  private static final String START_CONCURRENT = "startConcurrent";

  private final Executor executor;
  private final Map<String, Function<String, String>> operations;

  public DynamicChainOfCommandService(final Executor executor) {
	this.executor = executor;
	this.operations = new HashMap<>();
	operations.put("a", this::operationA);
	operations.put("b", this::operationB);
	operations.put("c", this::operationC);
	operations.put("d", this::operationD);
  }

  public Uni<String> executeSequential(final List<String> operations) {
	final Uni<String> chainOfCommand = Uni.createFrom().item(START_SEQUENTIAL);
	return operations
		.stream()
		.map(this::getOperation)
		.reduce(chainOfCommand, accumulate(), combine())
		.runSubscriptionOn(executor);
  }

  private BiFunction<Uni<String>, Function<String, String>, Uni<String>> accumulate() {
	return (uni, operation) -> uni.onItem().transform(operation);
  }

  private BinaryOperator<Uni<String>> combine() {
	return (u1, u2) -> {
	  throw new IllegalArgumentException("Parallel operation execution not available!");
	};
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

  private String operationD(final String previous) {
	final String current = "operationD";
	try {
	  Thread.sleep(1000);
	} catch (InterruptedException e) {
	  throw new RuntimeException(e);
	}
	return operateOn(previous, current);
  }

  public Uni<List<String>> executeConcurrent(final List<String> operations) {
	final List<Multi<String>> eventStream = generateEventStream(operations);
	return Multi.createBy().merging().streams(eventStream).collect().asList();
  }

  private List<Multi<String>> generateEventStream(final List<String> operations) {
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
