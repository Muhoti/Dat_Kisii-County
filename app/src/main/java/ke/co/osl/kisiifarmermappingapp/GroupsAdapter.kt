package ke.co.osl.kisiifarmermappingapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ke.co.osl.kisiifarmermappingapp.models.FarmerAssociationsBody

class GroupsAdapter(val context: Context, val userList: List<FarmerAssociationsBody>, id: String) :
    RecyclerView.Adapter<GroupsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView
        val type: TextView

        init {
            name = itemView.findViewById(R.id.name)
            type = itemView.findViewById(R.id.type)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.farmer_groups, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.name.text = userList[position].Name
        holder.type.text = userList[position].Type
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}