package chain.of.command;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/dynamic-chain-of-command")
public class DynamicChainOfCommand {

  private final Logger LOGGER = Logger.getLogger(DynamicChainOfCommand.class.getName());

  private final Executor executor;
  private final Map<String, Function<String, String>> operations;

  public DynamicChainOfCommand(final Executor executor) {
	this.executor = executor;
	this.operations = new HashMap<>();
	operations.put("a", this::operationA);
	operations.put("b", this::operationB);
	operations.put("c", this::operationC);
  }

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public Uni<String> execute(@QueryParam("operations") final List<String> operations) {
	final Uni<String> chainOfCommand = Uni.createFrom().item("start");
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

}
