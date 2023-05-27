package dynamic.operations;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class DynamicOperationsExecutor {

  private static final String START_SEQUENTIAL = "sequential";
  private static final String START_CONCURRENT = "concurrent";

  private final OperationsService operationsService;

  public DynamicOperationsExecutor(final OperationsService operationsService) {
	this.operationsService = operationsService;
  }

  public String executeSequential(final List<String> operations) {
	final Function<String, String> chainOfOperations = operationsService.buildChainOf(operations);
	return chainOfOperations.apply(START_SEQUENTIAL);
  }

  public List<String> executeConcurrent(final List<String> operations) {
	final List<Function<String, String>> operationList = get(operations);

	final List<CompletableFuture<String>> futures =
		operationList.stream()
			.map(f -> CompletableFuture.supplyAsync(() -> f.apply(START_CONCURRENT))).toList();

	return futures.stream().map(CompletableFuture::join).collect(Collectors.toList());
  }

  private List<Function<String, String>> get(final List<String> operations) {
	return operations
		.stream()
		.map(operationsService::get)
		.toList();
  }

}
