package ke.co.osl.kisiifarmermappingapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import ke.co.osl.kisiifarmermappingapp.FarmerDetails
import ke.co.osl.kisiifarmermappingapp.R

class CollectData : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.collect_data, container, false)
        val next = v.findViewById<Button>(R.id.next)
        val next2 = v.findViewById<ImageView>(R.id.collect_data_img)

        next.setOnClickListener {
            val intent = Intent(activity, FarmerDetails::class.java)
            intent.putExtra("NationalID", "xyxyx")
            activity?.startActivity(intent)
        }

        next2.setOnClickListener {
            val intent = Intent(activity, FarmerDetails::class.java)
            intent.putExtra("NationalID", "xyxyx")
            activity?.startActivity(intent)
        }
        return v
    }

}
