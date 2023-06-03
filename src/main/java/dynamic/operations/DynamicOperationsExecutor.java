package dynamic.operations;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.function.Function;
import lombok.AllArgsConstructor;

@ApplicationScoped
@AllArgsConstructor
public class DynamicOperationsExecutor {

  private static final String START_SEQUENTIAL = "sequential";
  private static final String START_CONCURRENT = "concurrent";

  private final OperationsService operationsService;

  Uni<String> executeSequential(final List<String> operations) {
    final Function<String, Uni<String>> chain = operationsService.buildChainOf(operations);
    return chain.apply(START_SEQUENTIAL);
  }

  Uni<List<String>> executeConcurrent(final List<String> operations) {
    final List<Multi<String>> operationEventStream = generateEventStreamOf(operations);
    return Multi.createBy().merging().streams(operationEventStream).collect().asList();
  }

  private List<Multi<String>> generateEventStreamOf(final List<String> operations) {
    return operations.stream().map(operationsService::get).map(this::createMultiFrom).toList();
  }

  private Multi<String> createMultiFrom(final Function<String, Uni<String>> operation) {
    return Multi.createFrom()
        .item(START_CONCURRENT)
        .onItem()
        .transformToUniAndConcatenate(operation::apply)
        .runSubscriptionOn(Infrastructure.getDefaultExecutor());
  }
}
