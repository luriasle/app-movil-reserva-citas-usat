package usat.reservacitas.com.iu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import usat.reservacitas.com.databinding.ActivityMisCitasBinding
import usat.reservacitas.com.io.ApiService
import usat.reservacitas.com.model.MisCitas
import usat.reservacitas.com.util.PreferenceHelper
import usat.reservacitas.com.util.PreferenceHelper.get
import usat.reservacitas.com.util.toast

class MisCitasActivity : AppCompatActivity() {

    private val apiService: ApiService by lazy {
        ApiService.create()
    }

    private val preferences by lazy {
        PreferenceHelper.defaultPrefs(this)
    }

    private val misCitasAdapter = MisCitasAdapter()

    private lateinit var binding: ActivityMisCitasBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMisCitasBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        cargarMisCitas()

        binding.rvMisCitas.layoutManager = LinearLayoutManager(this)
        binding.rvMisCitas.adapter = misCitasAdapter
    }

    private fun cargarMisCitas() {
        val jwt = preferences["jwt", ""]
        val call = apiService.getMisCitas("Bearer $jwt")
        call.enqueue(object : Callback<ArrayList<MisCitas>> {
            override fun onResponse(
                call: Call<ArrayList<MisCitas>>,
                response: Response<ArrayList<MisCitas>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        misCitasAdapter.miscitas = it
                        misCitasAdapter.notifyDataSetChanged()
                    }
                }
            }

            override fun onFailure(call: Call<ArrayList<MisCitas>>, t: Throwable) {
                toast(t.localizedMessage)
            }

        })
    }
}