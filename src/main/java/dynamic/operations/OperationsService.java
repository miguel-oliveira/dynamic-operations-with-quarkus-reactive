package dynamic.operations;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.Map;
import java.util.function.Function;

@ApplicationScoped
public class OperationsService {

  private static final Duration DELAY_A = Duration.ofMillis(3000);
  private static final Duration DELAY_B = Duration.ofMillis(2000);
  private static final Duration DELAY_C = Duration.ofMillis(1000);
  private final Map<String, Function<String, Uni<String>>> operations;

  public OperationsService() {
    this.operations = Map.of(
        "a", this::operationA,
        "b", this::operationB,
        "c", this::operationC
    );
  }

  private Uni<String> operationA(final String previous) {
    final String current = "A";
    return operateOn(previous, current, DELAY_A);
  }

  private Uni<String> operateOn(final String previous, final String current, final Duration delay) {
    return Uni.createFrom()
        .item(() -> MessageFormat.format("{0} -> {1}", previous, current))
        .onItem()
        .delayIt().by(delay)
        .log();
  }

  private Uni<String> operationB(final String previous) {
    final String current = "B";
    return operateOn(previous, current, DELAY_B);
  }

  private Uni<String> operationC(final String previous) {
    final String current = "C";
    return operateOn(previous, current, DELAY_C);
  }

  Function<String, Uni<String>> get(final String operation) {
    validate(operation);
    return operations.get(operation);
  }

  private void validate(final String operation) {
    if (!operations.containsKey(operation)) {
      throw notFoundException(operation);
    }
  }

  private WebApplicationException notFoundException(final String operation) {
    final String errorMessage = MessageFormat.format("Operation \"{0}\" not found!", operation);
    return new NotFoundException(
        errorMessage,
        Response.status(Status.NOT_FOUND).entity(errorMessage).build()
    );
  }

}
