package ke.co.osl.kisiifarmermappingapp.models

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ke.co.osl.kisiifarmermappingapp.R
import ke.co.osl.kisiifarmermappingapp.UpdateProduce

class ValueChainAdapter(val context: Context, val userList: List<GetValueChainBody>, id: String) :

    RecyclerView.Adapter<ValueChainAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.valuechainName)
        val avg: TextView = itemView.findViewById(R.id.avg)
        val updated: TextView = itemView.findViewById(R.id.updated)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.display_valuechains, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text = userList.get(position).Name
        holder.avg.text = "Yearly Produce: " + userList.get(position).AvgHarvestProduction + " " +  userList.get(position).Unit
        holder.updated.text = userList.get(position).updatedAt.toString().substringBefore("T")


        holder.itemView.setOnClickListener {
            val intent = Intent(context, UpdateProduce::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            intent.putExtra("ValueChainID", userList.get(position).ValueChainID )
            intent.putExtra("Name", userList.get(position).Name )
            intent.putExtra("FarmerID", userList.get(position).FarmerID )
            intent.putExtra("Unit", userList.get(position).Unit )
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }
}