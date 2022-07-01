package usat.reservacitas.com.model

import com.google.gson.annotations.SerializedName

data class MisCitas(
    val id: Int,
    val description: String,
    val type: String,
    val status: String,
    @SerializedName("scheduled_date") val fechaCita: String,
    @SerializedName("scheduled_time_12") val horaCita: String,
    @SerializedName("created_at") val fechaCreacionCita: String,
    val specialty: Especialidad,
    val doctor: Doctor
)
