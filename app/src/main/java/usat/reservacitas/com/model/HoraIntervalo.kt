package usat.reservacitas.com.model

data class HoraIntervalo(
    val start: String,
    val end: String
) {
    override fun toString(): String {
        return "$start - $end"
    }
}
