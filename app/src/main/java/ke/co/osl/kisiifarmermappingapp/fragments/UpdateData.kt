package ke.co.osl.kisiifarmermappingapp.fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import ke.co.osl.kisiifarmermappingapp.FarmerDetails
import ke.co.osl.kisiifarmermappingapp.R
import ke.co.osl.kisiifarmermappingapp.api.ApiInterface
import ke.co.osl.kisiifarmermappingapp.models.FarmersDetailsGetBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UpdateData : Fragment() {
    lateinit var dialog: Dialog
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
// Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.update_data, container, false)
        val next = v.findViewById<Button>(R.id.next)
        val next2 = v.findViewById<ImageView>(R.id.update_data_img)


        dialog = Dialog(requireActivity())
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.searchfarmer)

        next.setOnClickListener {
            //Implement function for Updating Data in Value Chains Below
            showSearchFarmerDialog(dialog)
        }

        next2.setOnClickListener {
            //Implement function for Updating Data in Value Chains Below
            showSearchFarmerDialog(dialog)
        }
        return v
    }

    private fun showSearchFarmerDialog(d: Dialog) {
        searchFarmer(d)

//        val hide = d.findViewById<ConstraintLayout>(R.id.parent)
//        hide.setOnClickListener {
//            d.hide()
//        }

        d.getWindow()?.setBackgroundDrawableResource(R.drawable.background_transparent);
        d.setCancelable(true);
        val window: Window = d.getWindow()!!
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        d.show()
        d.setCanceledOnTouchOutside(true);
    }

    private fun searchFarmer(d: Dialog) {
        val farmerId = d.findViewById<EditText>(R.id.farmerId)
        val error = d.findViewById<TextView>(R.id.error)
        val submit = d.findViewById<Button>(R.id.submit)

        submit.setOnClickListener {
            val apiInterface = ApiInterface.create().searchFarmerDetails(farmerId.text.toString())

            apiInterface.enqueue( object : Callback<List<FarmersDetailsGetBody>> {
                override fun onResponse(call: Call<List<FarmersDetailsGetBody>>, response: Response<List<FarmersDetailsGetBody>>?) {
                    System.out.println(response?.body())
                                    if(response?.body()?.size!! > 0) {
                    val intent = Intent(activity,FarmerDetails::class.java)
                    intent.putExtra("NationalID",response?.body()?.get(0)?.NationalID!!)
                    startActivity(intent)
                }else {
                    error.text = "The farmer was not found!"
                }
                }

                override fun onFailure(call: Call<List<FarmersDetailsGetBody>>, t: Throwable) {
                    System.out.println(t)
                    error.text = "Connection to server failed"
                }
            })
        }


    }
}
