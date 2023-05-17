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
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/dynamic-chain-of-command")
public class DynamicChainOfCommand {

  private final Logger LOGGER = Logger.getLogger(DynamicChainOfCommand.class.getName());

  private final Map<String, Function<String, Uni<? extends String>>> operations;

  public DynamicChainOfCommand() {
	this.operations = new HashMap<>();
	operations.put("a", this::operationA);
	operations.put("b", this::operationB);
	operations.put("c", this::operationC);
  }

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public Uni<String> execute(@QueryParam("operations") final List<String> operations) {
	final Uni<String> chainOfCommand = Uni.createFrom().item("start");
	return operations.stream()
		.map(this::getOperation)
		.reduce(chainOfCommand, getAccumulator(), getCombiner());
  }

  private BiFunction<Uni<String>, Function<String, Uni<? extends String>>, Uni<String>> getAccumulator() {
	return Uni::flatMap;
  }

  private BinaryOperator<Uni<String>> getCombiner() {
	return (firstUni, secondUni) -> firstUni.flatMap(v -> secondUni);
  }

  private Function<String, Uni<? extends String>> getOperation(final String operation) {
	validate(operation);
	return operations.get(operation);
  }

  private void validate(final String operation) {
	if (!operations.containsKey(operation)) {
	  throw new NotFoundException(String.format("Operation %s not found!", operation));
	}
  }

  private Uni<String> operationA(final String previous) {
	final String current = "operationA";
	log(previous, current);
	return Uni.createFrom().item(previous + " -> " + current);
  }

  private void log(final String previous, final String current) {
	LOGGER.log(Level.INFO, "{0} -> {1}", new Object[]{previous, current});
  }

  private Uni<String> operationB(final String previous) {
	final String current = "operationB";
	log(previous, current);
	return Uni.createFrom().item(previous + " -> " + current);
  }

  private Uni<String> operationC(final String previous) {
	final String current = "operationC";
	log(previous, current);
	return Uni.createFrom().item(previous + " -> " + current);
  }

}
