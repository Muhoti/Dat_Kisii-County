package ke.co.osl.kisiifarmermappingapp
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.auth0.android.jwt.JWT
import com.google.android.material.navigation.NavigationView
import ke.co.osl.kisiifarmermappingapp.api.ApiInterface
import ke.co.osl.kisiifarmermappingapp.models.FarmerAssociationsBody
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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


        navView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_home -> implementHome()
                R.id.nav_farmerdetails -> editFarmerDetails()
                R.id.nav_farmeraddress -> editFarmerAddress()
                R.id.nav_farmerassociations -> editFarmerAssociations()
                R.id.nav_farmerresources -> editFarmerResources()
                R.id.nav_valuechains -> editValueChains()
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
            System.out.println(user)
            val apiInterface = ApiInterface.create().showStatistics(user)

            apiInterface.enqueue(object : retrofit2.Callback<StatsStatus> {
                override fun onResponse(call: Call<StatsStatus>, response: Response<StatsStatus>) {
                    System.out.println(response.body()?.FarmerDetails?.total)
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
                    System.out.println(t.localizedMessage)
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

    private fun editValueChains() {
        val intent = Intent(this, ValueChains::class.java)
        intent.putExtra("editing", "search")
        startActivity(intent)
    }

    private fun editFarmerResources() {
        val intent = Intent(this, FarmerResources::class.java)
        intent.putExtra("editing", "search")
        startActivity(intent)
    }

    private fun editFarmerAddress() {
        val intent = Intent(this@MainActivity, FarmerAddress::class.java)
        intent.putExtra("editing", "search")
        startActivity(intent)
    }

    private fun editFarmerAssociations() {
        startActivity(Intent(this, FarmerAssociations::class.java))
        intent.putExtra("editing", "search")
    }

    private fun editFarmerDetails() {
        val intent = Intent(this,FarmerDetails::class.java)
        intent.putExtra("editing", "search")
        startActivity(intent)
    }

    private fun logout() {
        editor.remove("token")
        editor.commit()
        startActivity(Intent(this@MainActivity, LoginPage::class.java))
        finish()
    }



}

