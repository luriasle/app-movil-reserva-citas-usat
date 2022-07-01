package usat.reservacitas.com.io

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import usat.reservacitas.com.io.response.LoginResponse
import usat.reservacitas.com.io.response.SimpleResponse
import usat.reservacitas.com.model.Doctor
import usat.reservacitas.com.model.Especialidad
import usat.reservacitas.com.model.Horas
import usat.reservacitas.com.model.MisCitas

interface ApiService {

    // Obtener lista de especialidades
    @GET("specialties")
    fun getEspecialidades(): Call<ArrayList<Especialidad>>

    // Obtener doctores por especialidad
    @GET("specialties/{specialty}/doctors")
    fun getDoctores(@Path("specialty") idEspecialidad: Int): Call<ArrayList<Doctor>>

    // Obtener horario del doctor seleccionado
    @GET("schedule/hours")
    fun getHoras(@Query("doctor_id") doctorId: Int, @Query("date") fecha: String): Call<Horas>

    // Iniciar sesión
    @POST("login")
    fun postLogin(
        @Query("email") email: String,
        @Query("password") clave: String
    ): Call<LoginResponse>

    // Cerrar sesión
    @POST("logout")
    fun postLogout(@Header("Authorization") authHeader: String): Call<Void>

    // Obtener citas del paciente
    @GET("appointments")
    fun getMisCitas(@Header("Authorization") authHeader: String): Call<ArrayList<MisCitas>>

    // Registrar cita
    @POST("appointments")
    @Headers("Accept: application/json")
    fun postRegistrarCita(
        @Header("Authorization") authHeader: String,
        @Query("description") description: String,
        @Query("specialty_id") specialtyId: Int,
        @Query("doctor_id") doctorId: Int,
        @Query("scheduled_date") sheduledDate: String,
        @Query("scheduled_time") sheduledTime: String,
        @Query("type") type: String
    ): Call<SimpleResponse>

    // Registrar paciente
    @POST("register")
    @Headers("Accept: application/json")
    fun postRegistrarPaciente(
        @Query("name") name: String,
        @Query("email") email: String,
        @Query("password") password: String,
        @Query("password_confirmation") password_confirmation: String
    ): Call<LoginResponse>

    companion object Factory {
        private const val BASE_URL = "http://web-api-reserva-citas.herokuapp.com/api/"

        fun create(): ApiService {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
            return retrofit.create(ApiService::class.java)
        }
    }
}