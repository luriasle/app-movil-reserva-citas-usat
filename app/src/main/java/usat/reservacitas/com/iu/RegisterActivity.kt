package usat.reservacitas.com.iu

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import usat.reservacitas.com.R
import usat.reservacitas.com.databinding.ActivityRegisterBinding
import usat.reservacitas.com.io.ApiService
import usat.reservacitas.com.io.response.LoginResponse
import usat.reservacitas.com.util.PreferenceHelper
import usat.reservacitas.com.util.PreferenceHelper.set
import usat.reservacitas.com.util.toast

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    private val apiService by lazy {
        ApiService.create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.lblIrAlogin.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.btnConfirmarRegistro.setOnClickListener {
            performRegister()
        }

    }

    private fun performRegister() {
        val nombre = binding.txtRegistrarNombre.text.toString().trim()
        val correo = binding.txtRegistrarCorreo.text.toString().trim()
        val clave = binding.txtRegistrarClave.text.toString().trim()
        val claveConfirmar = binding.txtRegistrarClaveConfirmar.text.toString().trim()

        if (nombre.isEmpty() || correo.isEmpty() || clave.isEmpty() || claveConfirmar.isEmpty()) {
            toast(getString(R.string.Error_Registrar_Nuevo_Paciente))
            return
        }

        if (clave != claveConfirmar) {
            toast(getString(R.string.Error_Claves_No_Coinciden))
            return
        }

        var call = apiService.postRegistrarPaciente(nombre, correo, clave, claveConfirmar)
        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()


                    if (loginResponse == null) {
                        toast(getString(R.string.Error_Login_Response))
                        return
                    }
                    if (loginResponse.success) {
                        createSessionPreference(loginResponse.jwt)
                        toast(getString(R.string.bienvenido_nombre, loginResponse.user.name))
                        irAMenuActivity()
                    } else {
                        toast(getString(R.string.Error_Credenciales_Invalidas))
                    }


                } else {
                    toast(getString(R.string.Error_Register_Validation))
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                toast(t.localizedMessage)
            }

        })
    }

    private fun createSessionPreference(jwt: String) {
        val preferences = PreferenceHelper.defaultPrefs(this)
        preferences["jwt"] = jwt
    }

    private fun irAMenuActivity() {
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
        finish()
    }
}