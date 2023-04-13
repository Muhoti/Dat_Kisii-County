package ke.co.osl.kisiifarmermappingapp

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ke.co.osl.kisiifarmermappingapp.api.ApiInterface
import ke.co.osl.kisiifarmermappingapp.models.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ValueChains: AppCompatActivity() {
    lateinit var myAdapter: ValueChainAdapter
    lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var recyclerlist: RecyclerView
    var offset = 0
    var total = 0
    var fId = ""
    lateinit var preferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_valuechain)

        preferences = this.getSharedPreferences("kisiiapp", MODE_PRIVATE)
        editor = preferences.edit()

        recyclerlist = findViewById(R.id.recycler_list)
        recyclerlist.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(this)
        recyclerlist.layoutManager = linearLayoutManager

        val back = findViewById<ImageView>(R.id.back)
        val add = findViewById<Button>(R.id.add)
        val finish = findViewById<Button>(R.id.finish)

        back.setOnClickListener {
            startActivity(Intent (this, MainActivity::class.java))
        }

        add.setOnClickListener {
            val intent =Intent(this, AddValueChain::class.java)
            //intent.putExtra("FarmerID", fId )
            startActivity(intent)
            finish()
        }

        finish.setOnClickListener {
            finish()
            val intent =Intent(this, Summary::class.java)
            startActivity(intent)
        }

        var id = preferences.getString("NationalID", "")
        //var id = intent.getStringExtra("FarmerID")
        System.out.println("Value Chain ID is " + id)

        if(id == null)
            id = ""
        chooseAction(id)

    }

    private fun chooseAction(checkId: String) {

            val id = preferences.getString("NationalID", "")
           // val id=intent.getStringExtra("FarmerID")
            System.out.println("Running Value Chain ID as " + id)
            fId = id!!
            displayValueChains(id)

    }

    private fun displayValueChains(id: String) {
        val progress = findViewById<ProgressBar>(R.id.progress)
        progress.visibility = View.VISIBLE
        val apiInterface = ApiInterface.create().showValueChains(id)
        apiInterface.enqueue(object : Callback<List<GetValueChainBody>> {
            override fun onResponse(call: Call<List<GetValueChainBody>>,response: Response<List<GetValueChainBody>> ) {
                progress.visibility = View.GONE
                val responseBody = response.body()!!
                System.out.println((responseBody))
                if(response.body()!! !== null){

                    myAdapter = ValueChainAdapter(baseContext, responseBody, id)
                    myAdapter.notifyDataSetChanged()
                    recyclerlist.adapter = myAdapter
                    total = responseBody.size
                    val pgs = Math.ceil((responseBody.size / 10.0)).toInt()
                    val txt = if (offset == 0) {
                        1.toString() + "/" + pgs.toString()
                    } else ((offset / 10) + 1).toString() + "/" + pgs.toString()
                }
            }

            override fun onFailure(call: Call<List<GetValueChainBody>>, t: Throwable) {
                progress.visibility = View.GONE
                System.out.println(t)
            }
        })

    }

}