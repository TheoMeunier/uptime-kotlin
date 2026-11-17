package tmenier.fr.auth

import jakarta.annotation.security.DenyAll
import jakarta.annotation.security.PermitAll
import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.HEAD
import jakarta.ws.rs.OPTIONS
import jakarta.ws.rs.PATCH
import jakarta.ws.rs.POST
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method

class EndpointSecurityAnnotationsTest {
    @Test
    fun `every endpoint declares who may call it`() {
        val endpoints = resourceClasses().flatMap { type -> type.declaredMethods.filter(::isEndpoint).map { type to it } }

        assertTrue(endpoints.isNotEmpty(), "No endpoint found")

        val unsecured =
            endpoints
                .filterNot { (type, method) -> isSecured(method) || isSecured(type) }
                .map { (type, method) -> "${type.simpleName}.${method.name}" }

        assertEquals(emptyList<String>(), unsecured)
    }

    private fun isEndpoint(method: Method): Boolean = HTTP_METHODS.any { method.isAnnotationPresent(it) }

    private fun isSecured(element: AnnotatedElement): Boolean = SECURITY_ANNOTATIONS.any { element.isAnnotationPresent(it) }

    private fun resourceClasses(): List<Class<*>> {
        val loader = javaClass.classLoader

        return loader
            .getResources(ROOT_PACKAGE.replace('.', '/'))
            .toList()
            .filter { it.protocol == "file" }
            .flatMap { url ->
                val root = File(url.toURI())
                root
                    .walk()
                    .filter { it.isFile && it.name.endsWith(".class") && !it.name.contains('$') }
                    .map { ROOT_PACKAGE + "." + it.relativeTo(root).path.removeSuffix(".class").replace(File.separatorChar, '.') }
                    .toList()
            }.distinct()
            .map { Class.forName(it, false, loader) }
            .filter { it.isAnnotationPresent(Path::class.java) }
    }

    companion object {
        private const val ROOT_PACKAGE = "tmenier.fr"

        private val HTTP_METHODS =
            listOf(GET::class.java, POST::class.java, PUT::class.java, DELETE::class.java, PATCH::class.java, HEAD::class.java, OPTIONS::class.java)

        private val SECURITY_ANNOTATIONS = listOf(RolesAllowed::class.java, PermitAll::class.java, DenyAll::class.java)
    }
}
