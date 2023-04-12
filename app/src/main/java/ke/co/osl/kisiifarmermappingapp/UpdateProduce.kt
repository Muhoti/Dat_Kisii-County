package ke.co.osl.kisiifarmermappingapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import ke.co.osl.kisiifarmermappingapp.api.ApiInterface
import ke.co.osl.kisiifarmermappingapp.models.Message
import ke.co.osl.kisiifarmermappingapp.models.UpdateProduceBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class UpdateProduce: AppCompatActivity() {
    lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_produce)

        val back = findViewById<ImageView>(R.id.back)
        val next = findViewById<Button>(R.id.submit)

        back.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        next.setOnClickListener {
            startActivity(Intent(this,ValueChains::class.java))
            //intent.putExtra("ValueChainID", fId )
            startActivity(intent)
            finish()
        }


        updateProduce()
    }

    private fun updateProduce() {
        val error = findViewById<TextView>(R.id.error)
        val produce = findViewById<EditText>(R.id.produce)
        val valueChain = findViewById<TextView>(R.id.valueChain)
        val harvestDate = findViewById<CalendarView>(R.id.harvestDate)
        val farmingPeriod = findViewById<Spinner>(R.id.farmingPeriod)
        val next = findViewById<Button>(R.id.submit)
        val progress = findViewById<ProgressBar>(R.id.progress)

        val vname = intent.getStringExtra("Name")

        valueChain.setText(vname)
        System.out.println("The Valuechain is: " + vname)

        harvestDate.setOnDateChangeListener{ view, year, month, dayOfMonth ->
                val Date = (dayOfMonth.toString() + "-"
                        + (month + 1) + "-" + year)
                System.out.println(Date)
            }

        next.setOnClickListener {
            error.text = ""

            val id =intent.getStringExtra("ValueChainID")
            val fId =intent.getStringExtra("FarmerID")
            System.out.println("The produce id has been put as " + id)
            progress.visibility = View.VISIBLE

            val mill = harvestDate?.date!!

            val updateProduceBody = UpdateProduceBody(
                id!!,
                valueChain.text.toString().capitalize(),
                fId!!,
                produce.text.toString(),
                getDate(mill,"yyyy-MM-dd"),
                farmingPeriod.selectedItem.toString()
            )


            val apiInterface = ApiInterface.create().updatevaluechains(updateProduceBody)
            apiInterface.enqueue( object : Callback<Message> {
                override fun onResponse(call: Call<Message>?, response: Response<Message>?) {
                    progress.visibility = View.GONE
                    System.out.println(response?.body())
                    if(response?.body()?.success !== null){
                        error.text = response.body()?.success
                        val intent = Intent(this@UpdateProduce,ValueChains::class.java)
                        intent.putExtra("ValueChainID",id)
                        intent.putExtra("FarmerID",fId)
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

    fun getDate(milliSeconds: Long, dateFormat: String): String {
        val formatter = SimpleDateFormat(dateFormat)
        return formatter.format(Date(milliSeconds))
    }

}