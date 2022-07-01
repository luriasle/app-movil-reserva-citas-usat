package usat.reservacitas.com.model

data class Especialidad(
    val id: Int,
    val name: String
) {
    override fun toString(): String {
        return name
    }
}