package ke.co.osl.kisiifarmermappingapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import ke.co.osl.kisiifarmermappingapp.api.ApiInterface
import ke.co.osl.kisiifarmermappingapp.models.AddValueChainBody
import ke.co.osl.kisiifarmermappingapp.models.Message
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddValueChain: AppCompatActivity() {
    var fId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addvaluechain)

        val back = findViewById<ImageView>(R.id.back)

        back.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        var id = intent.getStringExtra("FarmerID")
        System.out.println("THE ID IS " + id)

        if(id == null)
            id = ""
        postFormResources(id)
    }

    private fun postFormResources(id: String) {
        fId= id
        val error = findViewById<TextView>(R.id.error)
        val name = findViewById<Spinner>(R.id.name)
        val variety = findViewById<EditText>(R.id.variety)
        val productionUnit = findViewById<Spinner>(R.id.productionUnit)
        val acreage = findViewById<EditText>(R.id.acreage)
        val harvestProduction = findViewById<EditText>(R.id.harvestProduction)
        val yearlyProduction = findViewById<EditText>(R.id.yearlyProduction)
        val next = findViewById<Button>(R.id.submit)
        val progress = findViewById<ProgressBar>(R.id.progress)

        next.setOnClickListener {
            error.text = ""

            progress.visibility = View.VISIBLE
            val addValueChain = AddValueChainBody(
                id,
                name.selectedItem.toString().capitalize(),
                variety.text.toString().capitalize(),
                productionUnit.selectedItem.toString().capitalize(),
                acreage.text.toString().capitalize(),
                yearlyProduction.text.toString().capitalize(),
                harvestProduction.text.toString().capitalize()
            )

            System.out.println(addValueChain)
            val apiInterface = ApiInterface.create().farmervaluechains(addValueChain)
            apiInterface.enqueue( object : Callback<Message> {
                override fun onResponse(call: Call<Message>?, response: Response<Message>?) {
                    progress.visibility = View.GONE
                    if(response?.body()?.success !== null){
                        error.text = response?.body()?.success
                        val intent = Intent(this@AddValueChain,ValueChains::class.java)
                        intent.putExtra("FarmerID",fId)
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

}