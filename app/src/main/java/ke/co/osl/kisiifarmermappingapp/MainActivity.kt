package ke.co.osl.kisiifarmermappingapp
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.auth0.android.jwt.JWT
import com.google.android.material.navigation.NavigationView
import ke.co.osl.kisiifarmermappingapp.api.ApiInterface
import ke.co.osl.kisiifarmermappingapp.models.FarmerAssociationsBody
import ke.co.osl.kisiifarmermappingapp.models.FarmersDetailsGetBody
import ke.co.osl.kisiifarmermappingapp.models.FarmersLocationBody
import ke.co.osl.kisiifarmermappingapp.models.StatsStatus
import retrofit2.Call
import retrofit2.Response
import javax.security.auth.callback.Callback

class MainActivity : AppCompatActivity() {

    lateinit var back: ImageView
    lateinit var drawerLayout: DrawerLayout
    lateinit var actionBarToggle: ActionBarDrawerToggle
    lateinit var toolbar: Toolbar
    lateinit var navView: NavigationView
    lateinit var editor: SharedPreferences.Editor
    lateinit var preferences: SharedPreferences
    lateinit var farmerdetails_total: TextView
    lateinit var farmerdetails_count : TextView
    lateinit var farmeraddress_total : TextView
    lateinit var farmeraddress_count : TextView
    lateinit var farmergroups_total : TextView
    lateinit var farmergroups_count : TextView
    lateinit var valuechain_total : TextView
    lateinit var valuechain_count : TextView
    lateinit var mapFarmer: LinearLayout
    lateinit var dialog: Dialog
    lateinit var progress: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dialog = Dialog(this)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.searchfarmer)

        progress = findViewById(R.id.progress)

        farmerdetails_count = findViewById(R.id.farmerdetails_count)
        farmerdetails_total = findViewById(R.id.farmerdetails_total)

        farmeraddress_count = findViewById(R.id.farmeraddress_count)
        farmeraddress_total = findViewById(R.id.farmeraddress_total)

        farmergroups_count = findViewById(R.id.farmergroups_count)
        farmergroups_total = findViewById(R.id.farmergroups_total)

        valuechain_count = findViewById(R.id.valuechain_count)
        valuechain_total = findViewById(R.id.valuechain_total)

        toolbar = findViewById(R.id.appbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer)
        navView = findViewById(R.id.nav_view)

        actionBarToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, 0,0)

        drawerLayout.addDrawerListener(actionBarToggle)
        actionBarToggle.isDrawerIndicatorEnabled
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        actionBarToggle.syncState()

        preferences = this.getSharedPreferences("kisiiapp", MODE_PRIVATE)
        editor = preferences.edit()

        val nationalID = preferences.getString("NationalID","")

        if(nationalID !== ""){
            startActivity(Intent(this,Summary::class.java))
            finish()
        }


        navView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_home -> implementHome()
                R.id.edit -> editFarmer()
                R.id.logout -> logout()
            }

            true
        }

        mapFarmer = findViewById(R.id.mapfarmer)

        mapFarmer.setOnClickListener(){
            val intent = Intent(this@MainActivity,FarmerDetails::class.java)
            intent.putExtra("NationalID", "xyxyx")
            startActivity(intent)
        }

        displayStatistics()

    }


    private fun displayStatistics() {
        try {
            val jwt = JWT(preferences.getString("token", "")!!)
            val user = jwt.getClaim("Name").asString()!!
            progress.visibility = View.VISIBLE
            val apiInterface = ApiInterface.create().showStatistics(user)

            apiInterface.enqueue(object : retrofit2.Callback<StatsStatus> {
                override fun onResponse(call: Call<StatsStatus>, response: Response<StatsStatus>) {
                    progress.visibility = View.GONE
                    var data = response.body()

                    farmerdetails_total.text = "Total: " + data?.FarmerDetails?.total.toString()
                    farmerdetails_count.text =  data?.FarmerDetails?.unique.toString()

                    farmeraddress_total.text = "Total: " +data?.FarmerAddresses?.total.toString()
                    farmeraddress_count.text = data?.FarmerAddresses?.unique.toString()

                    farmergroups_total.text = "Total: " +data?.FarmerGroups?.total.toString()
                    farmergroups_count.text = data?.FarmerGroups?.unique.toString()

                    valuechain_total.text = "Total: " +data?.ValueChains?.total.toString()
                    valuechain_count.text = data?.ValueChains?.unique.toString()

                }

                override fun onFailure(call: Call<StatsStatus>, t: Throwable) {
                    progress.visibility = View.GONE
                }
            })
        }catch (e:Exception) {
            startActivity(Intent(this,LoginPage::class.java))
           finish()
        }

    }

    private fun implementHome() {
        startActivity(Intent(this, MainActivity::class.java))
    }


    private fun editFarmer() {
        drawerLayout.closeDrawer(GravityCompat.START)
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
            val apiInterface = ApiInterface.create().searchFarmerDetails(farmerId.text.toString())

            apiInterface.enqueue( object : retrofit2.Callback<List<FarmersDetailsGetBody>> {
                override fun onResponse(call: Call<List<FarmersDetailsGetBody>>, response: Response<List<FarmersDetailsGetBody>> ) {
                    if(response.body()!!.isNotEmpty()) {
                        val body = response.body()
                        progress.visibility = View.GONE
                        editor.putString("NationalID", body?.get(0)?.NationalID)
                        editor.apply()
                        dialog.hide()
                        startActivity(Intent(this@MainActivity,Summary::class.java))
                        finish()
                    }else {
                        error.text = "Farmer does not exist"
                    }
                }
                override fun onFailure(call: Call<List<FarmersDetailsGetBody>>, t: Throwable) {
                    progress.visibility = View.GONE
                    error.text = "Connection to server failed"
                }
            })
        }
    }

    private fun logout() {
        editor.remove("token")
        editor.commit()
        startActivity(Intent(this@MainActivity, LoginPage::class.java))
        finish()
    }



}

