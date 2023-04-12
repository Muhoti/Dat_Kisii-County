package ke.co.osl.kisiifarmermappingapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ke.co.osl.kisiifarmermappingapp.models.dataBody

class RecyclerAdapter (val context: Context, val userList: List<dataBody>, val offset: Int) :
    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val valuechainName: TextView
        val editValuechain: TextView

        init {
            valuechainName = itemView.findViewById(R.id.valuechainName)
            editValuechain = itemView.findViewById(R.id.editValuechain)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.display_valuechains, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       holder.valuechainName.text = userList.get(position).Name
    }
}