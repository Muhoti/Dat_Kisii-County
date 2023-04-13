package ke.co.osl.kisiifarmermappingapp

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import ke.co.osl.kisiifarmermappingapp.api.ApiInterface
import ke.co.osl.kisiifarmermappingapp.models.FarmersLocationBody
import ke.co.osl.kisiifarmermappingapp.models.FarmersResourcesBody
import ke.co.osl.kisiifarmermappingapp.models.Message
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FarmerResources: AppCompatActivity() {
    lateinit var dialog: Dialog
    lateinit var preferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resources)

        preferences = this.getSharedPreferences("kisiiapp", MODE_PRIVATE)
        editor = preferences.edit()

        val back = findViewById<ImageView>(R.id.back)

        dialog = Dialog(this)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.searchfarmer)

        back.setOnClickListener {
            startActivity(Intent (this, MainActivity::class.java))
        }

        var nationalID = preferences.getString("NationalID","")!!

        if (nationalID !== ""){
            val type = intent.getStringExtra("editing")
            System.out.println(type)
            if (type == "editing"){
                fetchFarmerResources(nationalID)
            }else {
                postFarmerResources(nationalID)
            }
        }else {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }



    private fun fetchFarmerResources(id:String){
        val apiInterface = ApiInterface.create().searchFarmerResources(id)
        apiInterface.enqueue( object : Callback<List<FarmersResourcesBody>> {
            override fun onResponse(call: Call<List<FarmersResourcesBody>>, response: Response<List<FarmersResourcesBody>> ) {
                val bdy = response?.body()!!
                if(bdy.isNotEmpty()) {
                    getFarmersResources(bdy[0])
                }else {
                    startActivity(Intent(this@FarmerResources,MainActivity::class.java))
                    finish()
                }
            }
            override fun onFailure(call: Call<List<FarmersResourcesBody>>, t: Throwable) {
                startActivity(Intent(this@FarmerResources,MainActivity::class.java))
                finish()
            }
        })
    }

    private fun postFarmerResources(id:String) {
        val acreage = findViewById<EditText>(R.id.acreage)
        val error = findViewById<TextView>(R.id.error)
        val cropFarming = findViewById<EditText>(R.id.cropFarming)
        val livestockFarming = findViewById<EditText>(R.id.livestockFarming)
        val irrigation = findViewById<Spinner>(R.id.irrigation)
        val farmOwnership = findViewById<Spinner>(R.id.farmOwnership)
        val next = findViewById<Button>(R.id.submit)
        val progress = findViewById<ProgressBar>(R.id.progress)

        next.setOnClickListener {
            error.text = ""

            progress.visibility = View.VISIBLE
            val farmersResourcesBody = FarmersResourcesBody(
                id,
                acreage.text.toString(),
                cropFarming.text.toString(),
                livestockFarming.text.toString(),
                irrigation.selectedItem.toString(),
                farmOwnership.selectedItem.toString()
            )

            System.out.println(farmersResourcesBody)

            val apiInterface = ApiInterface.create().postFarmerResources(farmersResourcesBody)
            apiInterface.enqueue( object : Callback<Message> {
                override fun onResponse(call: Call<Message>?, response: Response<Message>?) {
                    progress.visibility = View.GONE
                    if(response?.body()?.success !== null){
                        error.text = response?.body()?.success
                        val intent = Intent(this@FarmerResources,FarmerAssociations::class.java)
                        startActivity(intent)
                    }
                    else {
                        error.text = response?.body()?.error
                    }
                }

                override fun onFailure(call: Call<Message>?, t: Throwable?) {
                    progress.visibility = View.GONE
                    error.text = "Connection to server failed"
                }
            })

        }
    }

    private fun getFarmersResources(body : FarmersResourcesBody) {
        val acreage = findViewById<EditText>(R.id.acreage)
        val error = findViewById<TextView>(R.id.error)
        val cropFarming = findViewById<EditText>(R.id.cropFarming)
        val livestockFarming = findViewById<EditText>(R.id.livestockFarming)
        val irrigation = findViewById<Spinner>(R.id.irrigation)
        val farmOwnership = findViewById<Spinner>(R.id.farmOwnership)
        val next = findViewById<Button>(R.id.submit)
        val progress = findViewById<ProgressBar>(R.id.progress)
        next.text = "Update"

        acreage.setText(body.TotalAcreage)
        cropFarming.setText(body.CropAcreage)
        livestockFarming.setText(body.LivestockAcreage)
        updateSpinner(irrigation,body.IrrigationType)
        updateSpinner(farmOwnership,body.FarmOwnership)

        next.setOnClickListener {
            error.text = ""

            progress.visibility = View.VISIBLE
            val farmersResourcesBody = FarmersResourcesBody(
                body.FarmerID,
                acreage.text.toString(),
                cropFarming.text.toString(),
                livestockFarming.text.toString(),
                irrigation.selectedItem.toString(),
                farmOwnership.selectedItem.toString()
            )



            val apiInterface = ApiInterface.create().putFarmerResources(body.FarmerID,farmersResourcesBody)
            apiInterface.enqueue( object : Callback<Message> {
                override fun onResponse(call: Call<Message>?, response: Response<Message>?) {
                    progress.visibility = View.GONE
                    System.out.println(response?.body())
                    if(response?.body()?.success !== null){
                        error.text = response?.body()?.success
                        val intent = Intent(this@FarmerResources,MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    else {
                        error.text = response?.body()?.error
                    }
                }

                override fun onFailure(call: Call<Message>?, t: Throwable?) {
                    progress.visibility = View.GONE
                    System.out.println(t)
                    error.text = "Connection to server failed"
                }
            })

        }
    }

    fun updateSpinner(spinner: Spinner, value: String) {
        val myAdap: ArrayAdapter<String> =
            spinner.getAdapter() as ArrayAdapter<String>
        val spinnerPosition = myAdap.getPosition(value)
        spinner.setSelection(spinnerPosition);
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val i = Intent(this, FarmerResources::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        finish()
    }

}