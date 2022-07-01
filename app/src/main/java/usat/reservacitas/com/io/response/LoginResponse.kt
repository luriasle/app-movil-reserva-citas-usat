package usat.reservacitas.com.io.response

import usat.reservacitas.com.model.Usuario

data class LoginResponse(
    val success: Boolean,
    val user: Usuario,
    val jwt: String
)
