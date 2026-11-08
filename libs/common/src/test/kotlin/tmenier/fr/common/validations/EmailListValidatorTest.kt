package tmenier.fr.common.validations

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullAndEmptySource
import org.junit.jupiter.params.provider.ValueSource

class EmailListValidatorTest {
    @ParameterizedTest
    @ValueSource(
        strings = [
            "uptime-kotlin@exemple.com",
            "uptime-kotlin@exemple.com, uptime-kotlin2@exemple.com",
            "a@exemple.com;b@exemple.fr",
            " a@exemple.com , b@exemple.com, ",
        ],
    )
    fun `accepte une ou plusieurs adresses valides`(value: String) {
        assertTrue(EmailListValidator.isValidEmailList(value))
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(
        strings = [
            " , ",
            "pas-un-email",
            "a@exemple.com, pas-un-email",
            "a@exemple.com b@exemple.com",
            "a@exemple",
        ],
    )
    fun `refuse les listes invalides`(value: String?) {
        assertFalse(EmailListValidator.isValidEmailList(value))
    }

    @Test
    fun `parse decoupe et nettoie la liste`() {
        assertEquals(
            listOf("a@exemple.com", "b@exemple.com"),
            EmailListValidator.parse(" a@exemple.com ,; b@exemple.com "),
        )
    }
}
