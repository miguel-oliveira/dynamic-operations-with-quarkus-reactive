package dynamic.operations;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class OperationsService {

  private final Logger LOGGER = Logger.getLogger(OperationsService.class.getName());

  private final Map<String, Function<String, String>> operations;

  public OperationsService() {
	this.operations = new HashMap<>();
	operations.put("a", this::operationA);
	operations.put("b", this::operationB);
	operations.put("c", this::operationC);
  }

  Function<String, String> buildChainOf(final List<String> operations) {
	return operations
		.stream()
		.map(this::get)
		.reduce(Function.identity(), Function::andThen);
  }

  Function<String, String> get(final String operation) {
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

  private String operationA(final String previous) {
	final String current = "operationA";
	return operateOn(previous, current);
  }

  private String operateOn(final String previous, final String current) {
	final String message = operationMessage(previous, current);
	LOGGER.log(Level.INFO, message);
	return message;
  }

  private String operationMessage(final String previous, final String current) {
	return MessageFormat.format("{0} -> {1}", previous, current);
  }

  private String operationB(final String previous) {
	final String current = "operationB";
	return operateOn(previous, current);
  }

  private String operationC(final String previous) {
	final String current = "operationC";
	return operateOn(previous, current);
  }
}
