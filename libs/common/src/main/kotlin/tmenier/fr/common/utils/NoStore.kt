package tmenier.fr.common.utils

import jakarta.ws.rs.core.Response

fun Response.ResponseBuilder.noStore(): Response.ResponseBuilder = header("Cache-Control", "no-store")
