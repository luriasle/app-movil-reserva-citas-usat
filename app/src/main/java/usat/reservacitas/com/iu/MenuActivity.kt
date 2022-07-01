package usat.reservacitas.com.iu

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import usat.reservacitas.com.databinding.ActivityMenuBinding
import usat.reservacitas.com.io.ApiService
import usat.reservacitas.com.util.PreferenceHelper
import usat.reservacitas.com.util.PreferenceHelper.set
import usat.reservacitas.com.util.PreferenceHelper.get
import usat.reservacitas.com.util.toast

class MenuActivity : AppCompatActivity() {

    private val apiService: ApiService by lazy {
        ApiService.create()
    }

    private val preferences by lazy {
        PreferenceHelper.defaultPrefs(this)
    }
    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnCrearCita.setOnClickListener {
            val intent = Intent(this, CrearCitaActivity::class.java)
            startActivity(intent)
        }

        binding.btnMisCitas.setOnClickListener {
            val intent = Intent(this, MisCitasActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogout.setOnClickListener {
            performLogout()
        }
    }

    private fun clearSessionPreference() {
        preferences["jwt"] = ""
    }

    private fun performLogout() {
        val jwt = preferences["jwt", ""]
        val call = apiService.postLogout("Bearer $jwt")
        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                clearSessionPreference()
                val intent = Intent(this@MenuActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                toast(t.localizedMessage)
            }

        })
    }
}