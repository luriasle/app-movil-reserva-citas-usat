package usat.reservacitas.com.iu

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import usat.reservacitas.com.R
import usat.reservacitas.com.databinding.*
import usat.reservacitas.com.io.ApiService
import usat.reservacitas.com.io.response.SimpleResponse
import usat.reservacitas.com.model.Doctor
import usat.reservacitas.com.model.Especialidad
import usat.reservacitas.com.model.Horas
import usat.reservacitas.com.util.PreferenceHelper
import usat.reservacitas.com.util.PreferenceHelper.get
import usat.reservacitas.com.util.toast
import java.util.Calendar

class CrearCitaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCrearCitaBinding
    private lateinit var bindingPaso1: CardViewPaso1Binding
    private lateinit var bindingPaso2: CardViewPaso2Binding
    private lateinit var bindingPaso3: CardViewPaso3Binding

    private val apiService: ApiService by lazy {
        ApiService.create()
    }

    private val preferences by lazy {
        PreferenceHelper.defaultPrefs(this)
    }

    private val selectedCalendar = Calendar.getInstance()
    private var selectedRadioButtonHora: RadioButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCrearCitaBinding.inflate(layoutInflater)

        val view = binding.root
        setContentView(view)

        bindingPaso1 = binding.paso1
        bindingPaso2 = binding.paso2
        bindingPaso3 = binding.paso3

        bindingPaso1.btnSiguiente.setOnClickListener {
            if (bindingPaso1.txtDescripcion.text.toString().length < 3) {
                bindingPaso1.txtDescripcion.error =
                    getString(R.string.validar_descripcion_cita)
            } else {
                // continúa a paso2 (cardviewpaso2.xml)
                bindingPaso1.cvPaso1.visibility = View.GONE
                bindingPaso2.cvPaso2.visibility = View.VISIBLE
            }
        }

        bindingPaso2.btnSiguiente2.setOnClickListener {
            if (bindingPaso2.txtFechaCita.text.toString().isEmpty()) {
                bindingPaso2.txtFechaCita.error =
                    getString(R.string.validar_fecha_cita)
            } else if (selectedRadioButtonHora == null) {
                Snackbar.make(
                    binding.crearCitaLayout,
                    R.string.validar_hora_cita,
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {
                // continúa a paso3 (cardviewpaso3.xml)
                mostrarDataCitaAConfirmar()
                bindingPaso2.cvPaso2.visibility = View.GONE
                bindingPaso3.cvPaso3.visibility = View.VISIBLE
            }
        }

        bindingPaso3.btnConfirmar.setOnClickListener {
            performRegistrarCita()
        }

        cargarEspecialidades()
        escucharCambiosEspecialidad()
        escucharCambiosHora()
    }

    private fun performRegistrarCita() {
        bindingPaso3.btnConfirmar.isClickable = false
        val jwt = preferences["jwt", ""]
        val authHeader = "Bearer $jwt"
        val descripcion = bindingPaso3.txtConfirmarDescripcion.text.toString()
        val especialidad = bindingPaso1.cmbEspecialidad.selectedItem as Especialidad
        val doctor = bindingPaso2.cmbDoctor.selectedItem as Doctor
        val fechaCita = bindingPaso3.txtConfirmarFechaCita.text.toString()
        val horaCita = bindingPaso3.txtConfirmarHoraCita.text.toString()
        val tipoCita = bindingPaso3.txtConfirmarTipoCita.text.toString()
        val call = apiService.postRegistrarCita(
            authHeader,
            descripcion,
            especialidad.id,
            doctor.id,
            fechaCita,
            horaCita,
            tipoCita
        )

        call.enqueue(object : Callback<SimpleResponse> {
            override fun onResponse(
                call: Call<SimpleResponse>,
                response: Response<SimpleResponse>
            ) {
                if (response.isSuccessful) {
                    toast(getString(R.string.Cita_Creada_Correctamente))
                    finish()
                } else {
                    toast(getString(R.string.Error_Crear_Cita))
                    bindingPaso3.btnConfirmar.isClickable = true
                }
            }

            override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                toast(t.localizedMessage)
                bindingPaso3.btnConfirmar.isClickable = true
            }

        })
    }

    fun onClickProgramarCita(v: View?) {
        val year = selectedCalendar.get(Calendar.YEAR)
        val month = selectedCalendar.get(Calendar.MONTH)
        val dayOfMonth = selectedCalendar.get(Calendar.DAY_OF_MONTH)

        val listener = DatePickerDialog.OnDateSetListener { datePicker, y, m, d ->
            selectedCalendar.set(y, m, d)

            bindingPaso2.txtFechaCita.setText(
                resources.getString(
                    R.string.date_format,
                    y,
                    (m + 1).dosDigitos(),
                    d.dosDigitos()
                )
            )
            bindingPaso2.txtFechaCita.error = null
        }

        val datePickerDialog = DatePickerDialog(this, listener, year, month, dayOfMonth)

        val datePicker = datePickerDialog.datePicker
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        datePicker.minDate = calendar.timeInMillis // +1
        calendar.add(Calendar.DAY_OF_MONTH, 29)
        datePicker.maxDate = calendar.timeInMillis // +30
        datePickerDialog.show()
    }

    private fun mostrarRadiosHoras(horas: ArrayList<String>) {
        selectedRadioButtonHora = null
        bindingPaso2.rgHorarioIzq.removeAllViews()
        bindingPaso2.rgHorarioDer.removeAllViews()

        if (horas.isEmpty()) {
            bindingPaso2.txtNoHayHorasDisponibles.visibility = View.VISIBLE
            return
        }

        bindingPaso2.txtNoHayHorasDisponibles.visibility = View.GONE

        var irAIzquierda = true

        horas.forEach {
            val radioButton = RadioButton(this)
            radioButton.id = View.generateViewId()
            radioButton.text = it

            radioButton.setOnClickListener { view ->
                selectedRadioButtonHora?.isChecked = false

                selectedRadioButtonHora = view as RadioButton?
                selectedRadioButtonHora?.isChecked = true
            }

            if (irAIzquierda)
                bindingPaso2.rgHorarioIzq.addView(radioButton)
            else
                bindingPaso2.rgHorarioDer.addView(radioButton)
            irAIzquierda = !irAIzquierda
        }
    }

    private fun Int.dosDigitos() = if (this >= 10) this.toString() else "0$this"

    override fun onBackPressed() {
        if (bindingPaso3.cvPaso3.visibility == View.VISIBLE) {
            bindingPaso3.cvPaso3.visibility = View.GONE
            bindingPaso2.cvPaso2.visibility = View.VISIBLE
        } else if (bindingPaso2.cvPaso2.visibility == View.VISIBLE) {
            bindingPaso2.cvPaso2.visibility = View.GONE
            bindingPaso1.cvPaso1.visibility = View.VISIBLE
        } else if (bindingPaso1.cvPaso1.visibility == View.VISIBLE) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.dialog_exit_titulo))
            builder.setMessage(getString(R.string.dialog_exit_mensaje))
            builder.setPositiveButton(getString(R.string.positivo_btn)) { _, _ ->
                finish()
            }
            builder.setNegativeButton(getString(R.string.negativo_btn)) { dialog, _ ->
                dialog.dismiss()
            }

            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun mostrarDataCitaAConfirmar() {
        bindingPaso3.txtConfirmarDescripcion.text =
            bindingPaso1.txtDescripcion.text.toString()
        bindingPaso3.txtConfirmarEspecialidad.text =
            bindingPaso1.cmbEspecialidad.selectedItem.toString()

        val rbSeleccionadoBtnId = bindingPaso1.rgTipo.checkedRadioButtonId
        val rbTipoSeleccionado =
            bindingPaso1.rgTipo.findViewById<RadioButton>(rbSeleccionadoBtnId)
        bindingPaso3.txtConfirmarTipoCita.text = rbTipoSeleccionado.text.toString()

        bindingPaso3.txtConfirmarMedico.text =
            bindingPaso2.cmbDoctor.selectedItem.toString()
        bindingPaso3.txtConfirmarFechaCita.text =
            bindingPaso2.txtFechaCita.text.toString()
        bindingPaso3.txtConfirmarHoraCita.text =
            selectedRadioButtonHora?.text.toString()
    }

    private fun cargarEspecialidades() {
        val call = apiService.getEspecialidades()
        call.enqueue(object : Callback<ArrayList<Especialidad>> {
            override fun onResponse(
                call: Call<ArrayList<Especialidad>>,
                response: Response<ArrayList<Especialidad>>
            ) {
                if (response.isSuccessful) { // True [200...300]
                    val especialidades = response.body()
                    if (especialidades != null) {
                        bindingPaso1.cmbEspecialidad.adapter = ArrayAdapter(
                            this@CrearCitaActivity,
                            android.R.layout.simple_list_item_1,
                            especialidades
                        )
                    }
                }
            }

            override fun onFailure(call: Call<ArrayList<Especialidad>>, t: Throwable) {
                Toast.makeText(
                    this@CrearCitaActivity,
                    getString(R.string.Error_Cargar_Especialidades),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }

        })
    }

    private fun escucharCambiosEspecialidad() {
        bindingPaso1.cmbEspecialidad.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapter: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val especialidad = adapter?.getItemAtPosition(position) as Especialidad
                    cargarDoctores(especialidad.id)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

            }
    }

    private fun cargarDoctores(idEspecialidad: Int) {
        val call = apiService.getDoctores(idEspecialidad)
        call.enqueue(object : Callback<ArrayList<Doctor>> {
            override fun onResponse(
                call: Call<ArrayList<Doctor>>,
                response: Response<ArrayList<Doctor>>
            ) {
                if (response.isSuccessful) { // True [200...300]
                    val doctores = response.body()
                    if (doctores != null) {
                        bindingPaso2.cmbDoctor.adapter = ArrayAdapter(
                            this@CrearCitaActivity,
                            android.R.layout.simple_list_item_1,
                            doctores
                        )
                    }

                }
            }

            override fun onFailure(call: Call<ArrayList<Doctor>>, t: Throwable) {
                Toast.makeText(
                    this@CrearCitaActivity,
                    getString(R.string.Error_Cargar_Doctores),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }

        })
    }

    private fun escucharCambiosHora() {
        bindingPaso2.cmbDoctor.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapter: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val doctor = adapter?.getItemAtPosition(position) as Doctor
                    cargarHoras(doctor.id, bindingPaso2.txtFechaCita.text.toString())
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

            }

        bindingPaso2.txtFechaCita.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val doctor = bindingPaso2.cmbDoctor.selectedItem as Doctor
                cargarHoras(doctor.id, bindingPaso2.txtFechaCita.text.toString())
            }

        })
    }

    private fun cargarHoras(doctorId: Int, fecha: String) {
        if (fecha.isEmpty()) {
            return
        }
        val call = apiService.getHoras(doctorId, fecha)
        call.enqueue(object : Callback<Horas> {
            override fun onResponse(call: Call<Horas>, response: Response<Horas>) {
                if (response.isSuccessful) {
                    val horas = response.body()
                    /*Toast.makeText(
                        this@CrearCitaActivity,
                        "mañana: ${horas?.morning?.size}, tarde: ${horas?.afternoon?.size}",
                        Toast.LENGTH_SHORT
                    ).show()*/
                    horas?.let {
                        bindingPaso2.txtSeleccionarDoctorYFecha.visibility = View.GONE
                        val intervalos = it.morning + it.afternoon
                        val horas = ArrayList<String>()
                        intervalos.forEach { intervalo ->
                            horas.add(intervalo.start)
                        }
                        mostrarRadiosHoras(horas)
                    }
                }
            }

            override fun onFailure(call: Call<Horas>, t: Throwable) {
                Toast.makeText(
                    this@CrearCitaActivity,
                    getString(R.string.Error_Cargar_Horas),
                    Toast.LENGTH_SHORT
                ).show()
            }

        })
    }
}