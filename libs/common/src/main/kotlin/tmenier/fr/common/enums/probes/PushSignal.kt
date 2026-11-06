package tmenier.fr.common.enums.probes

enum class PushSignal {
    UP,
    DOWN,
    ;

    companion object {
        fun parse(value: String?): PushSignal? =
            when (value?.trim()?.lowercase()) {
                null, "", "up", "ok", "success" -> UP
                "down", "fail", "failure", "error" -> DOWN
                else -> null
            }
    }
}
