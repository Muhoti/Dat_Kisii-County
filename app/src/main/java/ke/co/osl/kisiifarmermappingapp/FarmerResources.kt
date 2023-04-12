package ke.co.osl.kisiifarmermappingapp

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import ke.co.osl.kisiifarmermappingapp.api.ApiInterface
import ke.co.osl.kisiifarmermappingapp.models.FarmersResourcesBody
import ke.co.osl.kisiifarmermappingapp.models.Message
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FarmerResources: AppCompatActivity() {
    lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resources)

        val back = findViewById<ImageView>(R.id.back)

        dialog = Dialog(this)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.searchfarmer)

        back.setOnClickListener {
            startActivity(Intent (this, MainActivity::class.java))
        }

        var id = intent.getStringExtra("FarmerID")
        System.out.println("Farmer Resources ID IS " + id)

        var editing = intent.getBooleanExtra("editing", false)
        System.out.println("THE ID IS " + id)

        if(id == null)
            id = ""
        chooseAction (id, editing)
    }

    private fun chooseAction(checkId: String, editing: Boolean) {
        if(checkId !== "") {
            if(editing) {
                val apiInterface = ApiInterface.create().searchFarmerResources(checkId)
                apiInterface.enqueue( object : Callback<FarmersResourcesBody> {
                    override fun onResponse(call: Call<FarmersResourcesBody>, response: Response<FarmersResourcesBody> ) {
                        if(response?.body()?.FarmerID !== null) {
                            dialog.hide()
                            getFarmersResources(response.body()!!)
                        }else {
                          // error.text = "The farmer was not found!"
                        }
                    }
                    override fun onFailure(call: Call<FarmersResourcesBody>, t: Throwable) {
                        System.out.println(t)
                    }
                })
            }
            postFarmerResources()
        } else {
            showDialog()
        }
    }

    private fun showDialog() {
        searchFarmer(dialog)
        dialog.getWindow()?.setBackgroundDrawableResource(R.drawable.background_transparent);
        dialog.show()
    }

    private fun searchFarmer(d: Dialog) {
        val submit = d.findViewById<Button>(R.id.submit)
        val error = d.findViewById<TextView>(R.id.error)
        val farmerId = d.findViewById<EditText>(R.id.farmerId)
        val progress = d.findViewById<ProgressBar>(R.id.progress)

        submit.setOnClickListener {
            val apiInterface = ApiInterface.create().searchFarmerResources(farmerId.text.toString())

            apiInterface.enqueue( object : Callback<FarmersResourcesBody> {
                override fun onResponse(call: Call<FarmersResourcesBody>, response: Response<FarmersResourcesBody> ) {
                    if(response?.body()?.FarmerID !== null) {
                        progress.visibility = View.GONE
                        dialog.hide()
                        getFarmersResources(response.body()!!)
                    }else {
                        error.text = "The farmer was not found!"
//                        startActivity(Intent(this@FarmerResources, MainActivity::class.java))
                    }
                }
                override fun onFailure(call: Call<FarmersResourcesBody>, t: Throwable) {
                    progress.visibility = View.GONE
                    System.out.println(t)
                    error.text = "Connection to server failed"
                }
            })

        }
    }

    private fun postFarmerResources() {
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
            val id=intent.getStringExtra("FarmerID")
            System.out.println("The Farmer Resources ID now IS " + id)
            val farmersResourcesBody = FarmersResourcesBody(
                id!!,
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
                    System.out.println("submitted successfully")
                    if(response?.body()?.success !== null){
                        if (response?.body()?.success == "farmer exists"){
                            error.text = response?.body()?.success
                            chooseAction(id, false)
                        }
                        error.text = response?.body()?.success
                        val intent = Intent(this@FarmerResources,FarmerAssociations::class.java)
                        intent.putExtra("FarmerID", response.body()?.token)
                        startActivity(intent)
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

//            if (TextUtils.isEmpty(acreage.text.toString())) {
//                error.text = "Acreage cannot be empty!"
//                return@setOnClickListener
//            }
//
//            if (TextUtils.isEmpty(cropFarming.text.toString())) {
//                error.text = "Crop Farming cannot be empty!"
//                return@setOnClickListener
//            }
//
//            if (TextUtils.isEmpty(livestockFarming.text.toString())) {
//                error.text = "Livestock cannot be empty!"
//                return@setOnClickListener
//            }

            progress.visibility = View.VISIBLE
            val farmersResourcesBody = FarmersResourcesBody(
                body.FarmerID,
                acreage.text.toString(),
                cropFarming.text.toString(),
                livestockFarming.text.toString(),
                irrigation.selectedItem.toString(),
                farmOwnership.selectedItem.toString()
            )

            System.out.println(farmersResourcesBody)

            val apiInterface = ApiInterface.create().putFarmerResources(body.FarmerID,farmersResourcesBody)
            apiInterface.enqueue( object : Callback<Message> {
                override fun onResponse(call: Call<Message>?, response: Response<Message>?) {
                    progress.visibility = View.GONE
                    System.out.println(response?.body())
                    if(response?.body()?.success !== null){
                        error.text = response?.body()?.success
                        val intent = Intent(this@FarmerResources,FarmerAssociations::class.java)
                        intent.putExtra("FarmerID", response.body()?.token)
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