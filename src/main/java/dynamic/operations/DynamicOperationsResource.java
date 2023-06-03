package dynamic.operations;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/dynamic-operations")
public class DynamicOperationsResource {

  private final DynamicOperationsExecutor executor;

  public DynamicOperationsResource(final DynamicOperationsExecutor executor) {
    this.executor = executor;
  }

  @GET
  @Path("/sequential")
  @Produces(MediaType.TEXT_PLAIN)
  public Uni<String> executeSequential(@QueryParam("operations") final List<String> operations) {
    return executor.executeSequential(operations);
  }

  @GET
  @Path("/concurrent")
  @Produces(MediaType.TEXT_PLAIN)
  public Uni<List<String>> executeConcurrent(
      @QueryParam("operations") final List<String> operations
  ) {
    return executor.executeConcurrent(operations);
  }

}
