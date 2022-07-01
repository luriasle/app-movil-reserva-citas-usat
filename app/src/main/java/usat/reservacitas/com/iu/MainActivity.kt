package usat.reservacitas.com.iu

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import usat.reservacitas.com.R
import usat.reservacitas.com.databinding.ActivityMainBinding
import usat.reservacitas.com.io.ApiService
import usat.reservacitas.com.io.response.LoginResponse
import usat.reservacitas.com.util.PreferenceHelper
import usat.reservacitas.com.util.PreferenceHelper.get
import usat.reservacitas.com.util.PreferenceHelper.set
import usat.reservacitas.com.util.toast

class MainActivity : AppCompatActivity() {

    private val apiService: ApiService by lazy {
        ApiService.create()
    }

    private lateinit var binding: ActivityMainBinding

    private val snackBar by lazy {
        Snackbar.make(binding.mainLayout, R.string.press_back_again, Snackbar.LENGTH_SHORT)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val preferences = PreferenceHelper.defaultPrefs(this)

        if (preferences["jwt", ""].contains(".")) {
            irAMenuActivity()
        }

        binding.btnLogin.setOnClickListener {
            performLogin()
        }

        binding.tvGoToRegister.setOnClickListener {
            Toast.makeText(this, "Por favor completa tus datos", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
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

    override fun onBackPressed() {
        if (snackBar.isShown)
            super.onBackPressed()
        else
            snackBar.show()
    }

    private fun performLogin() {
        val email = binding.txtEmail.text.toString()
        val password = binding.txtPassword.text.toString()

        if (email.trim().isEmpty() || password.trim().isEmpty()) {
            toast(getString(R.string.Ingresar_Correo_Clave))
            return
        }
        val call = apiService.postLogin(email.trim(), password.trim())
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
                    toast(getString(R.string.Error_Login_Response))
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                toast(t.localizedMessage)
            }

        })
    }
}