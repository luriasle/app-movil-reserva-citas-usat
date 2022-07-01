package usat.reservacitas.com.iu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager

import usat.reservacitas.com.R
import usat.reservacitas.com.databinding.ItemMisCitasBinding
import usat.reservacitas.com.model.MisCitas

class MisCitasAdapter :
    RecyclerView.Adapter<MisCitasAdapter.ViewHolder>() {

    var miscitas = ArrayList<MisCitas>()

    // Crear viewholder y devuelve objeto de esta clase (se crea la lista)
    override fun onCreateViewHolder(parent: ViewGroup, viewParent: Int): ViewHolder {
        val binding =
            ItemMisCitasBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(
            binding
        )
    }

    // Retorna cantidad de elementos
    override fun getItemCount() = miscitas.size

    // Enlaza la data con la vista
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val context = holder.itemView.context
        with(holder) {
            val miCita = miscitas[position]
            binding.txtMisCitasId.text = context.getString(R.string.item_micita_id, miCita.id)
            binding.txtDoctorNombre.text = miCita.doctor.name
            binding.txtDiaCita.text =
                context.getString(R.string.item_micita_fecha, miCita.fechaCita)
            binding.txtHoraCita.text = context.getString(R.string.item_micita_hora, miCita.horaCita)

            binding.txtEspecialidad.text = miCita.specialty.name
            binding.txtDescripcion.text = miCita.description
            binding.txtEstado.text = miCita.status
            binding.txtTipoConsulta.text = miCita.type
            binding.txtCreadoEl.text =
                context.getString(R.string.Label_Creado_El, miCita.fechaCreacionCita)

            binding.btnExpander.setOnClickListener {
                TransitionManager.beginDelayedTransition(
                    itemView.parent as ViewGroup,
                    AutoTransition()
                )

                if (binding.layoutDetalleCita.visibility == View.VISIBLE) {
                    binding.layoutDetalleCita.visibility = View.GONE
                    binding.btnExpander.setImageResource(R.drawable.ic_ver_mas)
                } else {
                    binding.layoutDetalleCita.visibility = View.VISIBLE
                    binding.btnExpander.setImageResource(R.drawable.ic_ver_menos)
                }
            }
        }

    }

    inner class ViewHolder(val binding: ItemMisCitasBinding) :
        RecyclerView.ViewHolder(binding.root)

}




