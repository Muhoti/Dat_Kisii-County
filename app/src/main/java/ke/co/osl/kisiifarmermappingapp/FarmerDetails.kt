package ke.co.osl.kisiifarmermappingapp

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.auth0.android.jwt.JWT
import ke.co.osl.kisiifarmermappingapp.api.ApiInterface
import ke.co.osl.kisiifarmermappingapp.models.FarmersDetailsBody
import ke.co.osl.kisiifarmermappingapp.models.FarmersDetailsGetBody
import ke.co.osl.kisiifarmermappingapp.models.Message
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FarmerDetails: AppCompatActivity() {
    lateinit var dialog: Dialog
    lateinit var user:TextView
    lateinit var preferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        preferences = this.getSharedPreferences("kisiiapp", MODE_PRIVATE)
        editor = preferences.edit()

        val back = findViewById<ImageView>(R.id.back)

        dialog = Dialog(this)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.searchfarmer)

        user = findViewById(R.id.user)

        val jwt = JWT(preferences.getString("token","")!!)
        user.text = jwt.getClaim("Name").asString()

        back.setOnClickListener {
            startActivity(Intent(this,MainActivity::class.java))
        }

        var id = intent.getStringExtra("NationalID")
        System.out.println(id)

        if(id == null)
            id = ""
        chooseAction (id)
    }

    private fun chooseAction(checkId: String) {

        if(checkId == "xyxyx") {
            createFarmerDetails()
            System.out.println(checkId)

        }
        else if(checkId !== "" && checkId !== "xyxyx") {
            val apiInterface = ApiInterface.create().searchFarmerDetails(checkId)
            apiInterface.enqueue( object : Callback<List<FarmersDetailsGetBody>> {
                override fun onResponse( call: Call<List<FarmersDetailsGetBody>>, response: Response<List<FarmersDetailsGetBody>>?) {
                    if(response?.body()?.size!! > 0) {
                        getFarmersDetails(response?.body()?.get(0)!!)
                    }else {
                    }
                }
                override fun onFailure(call: Call<List<FarmersDetailsGetBody>>, t: Throwable) {
                    System.out.println(t)
                }
            })

        }
        else if (checkId === "") {
            //PUT
            showDialog()
        }
    }

    private fun showDialog() {
        searchFarmer(dialog)
        dialog.show()
    }

    private fun searchFarmer(d: Dialog) {
        val submit = d.findViewById<Button>(R.id.submit)
        val error = d.findViewById<TextView>(R.id.error)
        val farmerId = d.findViewById<EditText>(R.id.farmerId)
        val progress = d.findViewById<ProgressBar>(R.id.progress)

        submit.setOnClickListener {
            val apiInterface = ApiInterface.create().searchFarmerDetails(farmerId.text.toString())

            apiInterface.enqueue( object : Callback<List<FarmersDetailsGetBody>> {
                override fun onResponse( call: Call<List<FarmersDetailsGetBody>>, response: Response<List<FarmersDetailsGetBody>>?) {
                    if(response?.body()?.size!! > 0) {
                        System.out.println(response?.body())
                        progress.visibility = View.GONE
                        dialog.hide()
                        getFarmersDetails(response?.body()?.get(0)!!)
                    }else {
                        error.text = "The farmer was not found!"
                    }
                }

                override fun onFailure(call: Call<List<FarmersDetailsGetBody>>, t: Throwable) {
                    progress.visibility = View.GONE
                    System.out.println(t)
                    error.text = "Connection to server failed"
                }
            })

        }
    }

    private fun createFarmerDetails() {
        val next = findViewById<Button>(R.id.next)
        val error = findViewById<TextView>(R.id.error)
        val nationalId = findViewById<EditText>(R.id.nationalId)
        val name = findViewById<EditText>(R.id.name)
        val phone = findViewById<EditText>(R.id.phone)
        val gender = findViewById<Spinner>(R.id.gender)
        val age = findViewById<Spinner>(R.id.age)
        val farming = findViewById<Spinner>(R.id.farming)
        val progress = findViewById<ProgressBar>(R.id.progress)

        System.out.println("post")

        next.setOnClickListener {
            error.text = ""

            if (TextUtils.isEmpty(nationalId.text.toString())) {
                error.text = "farmerId cannot be empty!"
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(name.text.toString())) {
                error.text = "Name cannot be empty!"
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(phone.text.toString())) {
                error.text = "phone cannot be empty!"
                return@setOnClickListener
            }

            if(phone.text.toString().length !== 10) {
                error.text = "Phone needs to be 10 digits!"
                return@setOnClickListener
            }

            progress.visibility = View.VISIBLE

            val farmerDetailsBody = FarmersDetailsBody(
                user.text.toString(),
                nationalId.text.toString(),
                name.text.toString().capitalize(),
                phone.text.toString(),
                gender.selectedItem.toString(),
                age.selectedItem.toString(),
                farming.selectedItem.toString()
            )

            System.out.println(farmerDetailsBody)

            val apiInterface = ApiInterface.create().postFarmerDetails(farmerDetailsBody)
            apiInterface.enqueue( object : Callback<Message> {
                override fun onResponse(call: Call<Message>?, response: Response<Message>?) {
                    progress.visibility = View.GONE
                    if(response?.body()?.success !== null){
                        if (response?.body()?.success == "farmer exists"){
                            error.text = response?.body()?.success
                            chooseAction(nationalId.toString())
                        }
                        error.text = response?.body()?.success
                        editor.putString("NationalID", farmerDetailsBody.NationalID)
                        editor.apply()
                        System.out.println("national id is ${farmerDetailsBody.NationalID}")
                        val intent = Intent(this@FarmerDetails,FarmerAddress::class.java)
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

    private fun getFarmersDetails(body : FarmersDetailsGetBody) {
        val next = findViewById<Button>(R.id.next)
        val error = findViewById<TextView>(R.id.error)
        val nationalId = findViewById<EditText>(R.id.nationalId)
        val name = findViewById<EditText>(R.id.name)
        val phone = findViewById<EditText>(R.id.phone)
        val gender = findViewById<Spinner>(R.id.gender)
        val age = findViewById<Spinner>(R.id.age)
        val farming = findViewById<Spinner>(R.id.farming)
        val progress = findViewById<ProgressBar>(R.id.progress)
        next.text = "Update"

        //Receive data
        user.text.toString()
        nationalId.setText(body.NationalID)
        name.setText(body.Name)
        phone.setText(body.Phone)
        updateSpinner(gender,body.Gender)
        updateSpinner(age,body.AgeGroup)
        updateSpinner(farming,body.FarmingType)
        next.setOnClickListener {

            error.text = ""

            if (TextUtils.isEmpty(name.text.toString())) {
                error.text = "Acreage cannot be empty!"
                return@setOnClickListener
            }

            if (TextUtils.isEmpty(phone.text.toString())) {
                error.text = "Crop Farming cannot be empty!"
                return@setOnClickListener
            }

            progress.visibility = View.VISIBLE
            val farmerDetailsBody = FarmersDetailsBody(
                user.text.toString(),
                nationalId.text.toString(),
                name.text.toString().capitalize(),
                phone.text.toString(),
                gender.selectedItem.toString(),
                age.selectedItem.toString(),
                farming.selectedItem.toString()
            )
            val apiInterface = ApiInterface.create().putFarmerDetails(body.ID,farmerDetailsBody)
            apiInterface.enqueue( object : Callback<Message> {
                override fun onResponse(call: Call<Message>?, response: Response<Message>?) {
                    progress.visibility = View.GONE
                    System.out.println(response?.body())
                    if(response?.body()?.success !== null){
                        error.text = response?.body()?.success
                        val intent = Intent(this@FarmerDetails,FarmerAddress::class.java)
                        intent.putExtra("FarmerID",body.NationalID!!)
                        intent.putExtra("editing",true)
                        startActivity(intent)
                        finish()
                    }
                    else {
                        error.text = response?.body()?.error
                    }
                }
                override fun onFailure(call: Call<Message>?, t: Throwable?) {
                    System.out.println(t)
                    progress.visibility = View.GONE
                    error.text = "Connection to server failed"
                }
            })
        }

    }

    private fun updateSpinner(spinner: Spinner, value: String?) {
        val myAdap: ArrayAdapter<String> =
            spinner.getAdapter() as ArrayAdapter<String>
        val spinnerPosition = myAdap.getPosition(value)
        spinner.setSelection(spinnerPosition);
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val i = Intent(this, MainActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        finish()
    }

}
