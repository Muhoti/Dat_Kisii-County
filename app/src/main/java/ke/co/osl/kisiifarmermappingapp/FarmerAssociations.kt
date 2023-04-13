package ke.co.osl.kisiifarmermappingapp

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ke.co.osl.kisiifarmermappingapp.api.ApiInterface
import ke.co.osl.kisiifarmermappingapp.models.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FarmerAssociations: AppCompatActivity() {
    lateinit var gdialog: Dialog
    lateinit var myAdapter: GroupsAdapter
    lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var recyclerlist: RecyclerView
    lateinit var pages: TextView
    var offset = 0
    var total = 0
    var fId = ""
    var fname = ""
    lateinit var preferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_associations)

        preferences = this.getSharedPreferences("kisiiapp", MODE_PRIVATE)
        editor = preferences.edit()

        recyclerlist = findViewById(R.id.recycler_list)
        recyclerlist.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(this)
        recyclerlist.layoutManager = linearLayoutManager

        val back = findViewById<ImageView>(R.id.back)
        val add = findViewById<Button>(R.id.add)
        val proceed = findViewById<Button>(R.id.proceed)

        add.setOnClickListener {
            showGroupsDialog(fId)
        }

        gdialog = Dialog(this)
        gdialog.setCancelable(true)
        gdialog.setCanceledOnTouchOutside(false)
        gdialog.setContentView(R.layout.add_farmer_assoc)

        back.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        proceed.setOnClickListener {
            val intent =Intent(this, ValueChains::class.java)
            //intent.putExtra("FarmerID", fId )
            intent.putExtra("FarmerName", fname)
            startActivity(intent)
            finish()
        }

        var id = preferences.getString("NationalID", "")
        //var id = intent.getStringExtra("FarmerID")

        if(id == null)
            id = ""
        chooseAction(id)
    }

    private fun chooseAction(checkId: String) {
        if(checkId !== "") {
            val id=preferences.getString("NationalID", "")
            System.out.println("Farmer Association ID IS " + id)
            fId = id!!
            displayFarmerGroups(id)
        } else {
            //showSearchDialog()
        }
    }

    private fun showGroupsDialog(id: String) {
        getFarmerGroups(gdialog,id)
        gdialog.getWindow()?.setBackgroundDrawableResource(R.drawable.background_transparent);
        gdialog.show()
    }

    private fun getFarmerGroups(d: Dialog,id: String) {
        val submit = d.findViewById<Button>(R.id.submit)
        val groupname = d.findViewById<EditText>(R.id.groupname)
        val grouptype = d.findViewById<Spinner>(R.id.grouptype)
        val error = d.findViewById<TextView>(R.id.error)
        val hide = d.findViewById<ConstraintLayout>(R.id.parent)
        val progress = d.findViewById<ProgressBar>(R.id.progress)

        submit.setOnClickListener {
            error.text = ""

            progress.visibility = View.VISIBLE

            val groupsBody = FarmerAssociationsBody (
                id,
                groupname.text.toString().capitalize(),
                grouptype.selectedItem.toString()
            )

            System.out.println(groupsBody)

            val apiInterface = ApiInterface.create().postFarmerAssociations(groupsBody)
            apiInterface.enqueue( object : Callback<Message> {
                override fun onResponse(call: Call<Message>?, response: Response<Message>?) {
                    progress.visibility = View.GONE
                    System.out.println(response?.body())
                    if(response?.body()?.success !== null){
                        gdialog.hide()
                        displayFarmerGroups(response?.body()?.token!!)
                    }

                    else {
                        error.text = "Data not sent to api: " + response?.body()?.error
                    }
                }

                override fun onFailure(call: Call<Message>?, t: Throwable?) {
                    System.out.println(t)
                    error.text = "Connection to server failed"
                }
            })

        }

    }



    private fun searchFarmer(d: Dialog) {
        val submit = d.findViewById<Button>(R.id.submit)
        val error = d.findViewById<TextView>(R.id.error)
        val farmerId = d.findViewById<EditText>(R.id.farmerId)
        val progress = d.findViewById<ProgressBar>(R.id.progress)

//        submit.setOnClickListener {
//            val apiInterface = ApiInterface.create().searchFarmerDetails(farmerId.text.toString())
//
//            apiInterface.enqueue( object : Callback<List<FarmersDetailsGetBody>> {
//                override fun onResponse(call: Call<List<FarmersDetailsGetBody>>, response: Response<List<FarmersDetailsGetBody>>?) =
//                    if (response?.body()?.size!! > 0){
//                        fId = response?.body()?.get(0)?.NationalID!!
//                        progress.visibility = View.GONE
//                        System.out.println("The Farmer ID is " + response?.body()?.get(0)?.NationalID )
//                        dialog.hide()
//                        displayFarmerGroups(response.body()?.get(0)?.NationalID!! )
//                    }else {
//                        error.text = "The farmer was not found!"
//                    }
//
//                override fun onFailure(call: Call<List<FarmersDetailsGetBody>>?, t: Throwable?) {
//                    progress.visibility = View.GONE
//                    System.out.println(t)
//                    error.text = "Connection to server failed"
//                }
//            })
//
//        }
    }

    private fun displayFarmerGroups(id: String) {
        val progress = findViewById<ProgressBar>(R.id.progress)
        progress.visibility = View.VISIBLE
        val apiInterface = ApiInterface.create().showFarmerGroups(id)
        apiInterface.enqueue(object : Callback<List<FarmerAssociationsBody>> {
            override fun onResponse(call: Call<List<FarmerAssociationsBody>>,response: Response<List<FarmerAssociationsBody>>) {
                progress.visibility = View.GONE
                val responseBody = response.body()!!
                System.out.println((responseBody))
                myAdapter = GroupsAdapter(baseContext, responseBody,id)
                myAdapter.notifyDataSetChanged()
                recyclerlist.adapter = myAdapter
                total = responseBody.size
                val pgs = Math.ceil((responseBody.size /10.0)).toInt()
                val txt = if(offset == 0){
                    1.toString() + "/" + pgs.toString()
                }else ((offset/10)+1).toString() + "/" + pgs.toString()
            }

            override fun onFailure(call: Call<List<FarmerAssociationsBody>>, t: Throwable) {
                progress.visibility = View.GONE
                System.out.println(t)
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val i = Intent(this, FarmerResources::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        finish()
    }

}


