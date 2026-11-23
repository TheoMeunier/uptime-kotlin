package tmenier.fr.auth.extensions

import org.eclipse.microprofile.jwt.JsonWebToken
import java.util.UUID

fun JsonWebToken.userId(): UUID = UUID.fromString(name)
