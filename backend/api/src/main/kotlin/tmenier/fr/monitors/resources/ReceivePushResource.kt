package tmenier.fr.monitors.resources

import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import tmenier.fr.common.enums.probes.PushSignal
import tmenier.fr.monitors.actions.ReceivePushAction

@Path("/api/push/{token}")
@Produces(MediaType.APPLICATION_JSON)
class ReceivePushResource(
    private val receivePushAction: ReceivePushAction,
) {
    @GET
    fun receiveGet(
        @PathParam("token") token: String,
        @QueryParam("status") status: String?,
        @QueryParam("msg") message: String?,
    ): Response = receive(token, status, message)

    @POST
    fun receivePost(
        @PathParam("token") token: String,
        @QueryParam("status") status: String?,
        @QueryParam("msg") message: String?,
    ): Response = receive(token, status, message)

    private fun receive(
        token: String,
        status: String?,
        message: String?,
    ): Response {
        val signal =
            PushSignal.parse(status)
                ?: return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(mapOf("ok" to false, "error" to "status must be up or down"))
                    .build()

        return when (receivePushAction.execute(token, signal, message)) {
            ReceivePushAction.Outcome.ACCEPTED ->
                Response.ok(mapOf("ok" to true)).build()

            ReceivePushAction.Outcome.PAUSED ->
                Response.ok(mapOf("ok" to true, "ignored" to "monitor paused")).build()

            ReceivePushAction.Outcome.NOT_FOUND ->
                Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(mapOf("ok" to false, "error" to "unknown push token"))
                    .build()
        }
    }
}
