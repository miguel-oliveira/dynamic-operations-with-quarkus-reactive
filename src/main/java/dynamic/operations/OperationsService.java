package dynamic.operations;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class OperationsService {

  private final Logger LOGGER = Logger.getLogger(OperationsService.class.getName());

  private final Map<String, Function<String, Uni<String>>> operations;

  public OperationsService() {
	this.operations = Map.of(
		"a", this::operationA,
		"b", this::operationB,
		"c", this::operationC
	);
  }

  Function<String, Uni<String>> buildChainOf(final List<String> operations) {
	final Function<String, Uni<String>> start = s -> Uni.createFrom().item(s);
	return operations
		.stream()
		.map(this::get)
		.reduce(start, chain());
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

  private static WebApplicationException notFoundException(final String operation) {
	final String errorMessage = MessageFormat.format("Operation \"{0}\" not found!", operation);
	return new NotFoundException(
		errorMessage,
		Response.status(Status.NOT_FOUND).entity(errorMessage).build()
	);
  }

  private static BinaryOperator<Function<String, Uni<String>>> chain() {
	return (current, next) -> value -> current.apply(value).chain(next::apply);
  }

  private Uni<String> operationA(final String previous) {
	final String current = "operationA";
	return operateOn(previous, current);
  }

  private Uni<String> operateOn(final String previous, final String current) {
	return operationMessage(previous, current)
		.onItem()
		.transformToUni(this::logAndReturn);
  }

  private Uni<String> operationMessage(final String previous, final String current) {
	return Uni.createFrom().item(MessageFormat.format("{0} -> {1}", previous, current));
  }

  private Uni<String> logAndReturn(final String message) {
	LOGGER.log(Level.INFO, message);
	return Uni.createFrom().item(message);
  }

  private Uni<String> operationB(final String previous) {
	final String current = "operationB";
	return operateOn(previous, current);
  }

  private Uni<String> operationC(final String previous) {
	final String current = "operationC";
	return operateOn(previous, current);
  }

}
