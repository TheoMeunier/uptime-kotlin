package tmenier.fr

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Test

@QuarkusTest
class ExampleResourceTest {
    @Test
    fun testHelloEndpoint() {
        println("Hello word")
    }
}
