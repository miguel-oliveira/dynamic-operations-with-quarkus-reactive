package dynamic.operations;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;

@ApplicationScoped
@AllArgsConstructor
public class DynamicOperationsExecutor {

  private static final String START_SEQUENTIAL = "sequential";
  private static final String START_CONCURRENT = "concurrent";

  private final OperationsService operationsService;

  Uni<String> executeSequential(final List<String> operations) {
    final Function<String, Uni<String>> start = s -> Uni.createFrom().item(s);
    final Stream<Function<String, Uni<String>>> chain = streamOf(operations);
    return chain.reduce(start, chain()).apply(START_SEQUENTIAL);
  }

  private Stream<Function<String, Uni<String>>> streamOf(final List<String> operations) {
    return operations.stream().map(operationsService::get);
  }

  private BinaryOperator<Function<String, Uni<String>>> chain() {
    return (current, next) -> value -> current.apply(value).chain(next::apply);
  }

  Uni<List<String>> executeConcurrent(final List<String> operations) {
    final Stream<Multi<String>> operationEventStream = generateEventStreamOf(operations);
    return Multi.createBy().merging().streams(operationEventStream.toList()).collect().asList();
  }

  private Stream<Multi<String>> generateEventStreamOf(final List<String> operations) {
    return streamOf(operations).map(this::createMultiFrom);
  }

  private Multi<String> createMultiFrom(final Function<String, Uni<String>> operation) {
    return Multi.createFrom()
        .item(START_CONCURRENT)
        .onItem()
        .transformToUniAndConcatenate(operation::apply);
  }
}
