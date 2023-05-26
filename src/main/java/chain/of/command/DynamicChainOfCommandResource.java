package chain.of.command;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/dynamic-chain-of-command")
public class DynamicChainOfCommandResource {

  private final DynamicChainOfCommandService service;

  public DynamicChainOfCommandResource(final DynamicChainOfCommandService service) {
	this.service = service;
  }

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public Uni<String> execute(@QueryParam("operations") final List<String> operations) {
	return service.executeSequential(operations);
  }


}
