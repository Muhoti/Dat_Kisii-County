package ke.co.osl.kisiifarmermappingapp

import android.Manifest
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import ke.co.osl.kisiifarmermappingapp.api.ApiInterface
import ke.co.osl.kisiifarmermappingapp.models.FarmersLocationBody
import ke.co.osl.kisiifarmermappingapp.models.Message
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FarmerAddress: AppCompatActivity() {
    lateinit var dialog: Dialog
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    lateinit var webView: WebView
    lateinit var accuracy :TextView
    lateinit var address : TextView
    lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback
    var lat: Double = 0.0
    var lng: Double = 0.0
    var acc: Float = 0f
    lateinit var preferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor


    val ip_URL = "http://185.215.180.181:7805/api/map/"
//    val ip_URL = "http://demo.osl.co.ke:444/"

    object AndroidJSInterface {
        @JavascriptInterface
        fun onClicked() {
            Log.d("HelpButton", "Help button clicked")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        preferences = this.getSharedPreferences("kisiiapp", MODE_PRIVATE)
        editor = preferences.edit()


        accuracy = findViewById(R.id.accuracy)
        address = findViewById(R.id.address)
        val refresh = findViewById<ImageView>(R.id.refresh)
        val myLocation = findViewById<ImageView>(R.id.location)

        val back = findViewById<ImageView>(R.id.back)

        dialog = Dialog(this)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setContentView(R.layout.searchfarmer)

        back.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        var id = preferences.getString("NationalID", "")

       // var id = intent.getStringExtra("FarmerID")
        var editing = intent.getBooleanExtra("editing", false)
        System.out.println("Farmer Address ID IS " + id)

        if(id == null)
            id = ""
        chooseAction(id,editing)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            requestLocationPermission ()
        }

        //Get user location
        getLocationUpdates()

        refresh.setOnClickListener{
            refreshMap()
            getLocationUpdates()
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        myLocation.setOnClickListener{
            if(lat !== 0.0 && lng !== 0.0){
                val txt = "Accuracy: " + acc.toString() + " m"
                accuracy.text = txt
                val txt1 = "Lat: " + lat.toString() + " Lng: " + lng.toString()
                address.text = txt1
                adjustMarker(lng, lat)
                getLocationUpdates()
            }else {
                getLocationUpdates()
                Toast.makeText(this, "Location not acquired! Please wait", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        val progDailog: ProgressDialog? = ProgressDialog.show(this, "Loading","Please wait...", true);
        progDailog?.setCancelable(false);
        webView = findViewById(R.id.addressMap)
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);

        webView.addJavascriptInterface(AndroidJSInterface, "Android")
        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                if (handler != null){
                    handler.proceed();
                } else {
                    super.onReceivedSslError(view, null, error);
                }
            }
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                progDailog?.show()
                view.loadUrl(url)
                return true
            }

            override fun onPageFinished(view: WebView, url: String) {
                progDailog?.dismiss()
                if (ActivityCompat.checkSelfPermission(
                        this@FarmerAddress,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this@FarmerAddress, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestLocationPermission()
                }
                try {
                    mFusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            val txt = "Accuracy: " + location?.accuracy.toString() + " m"
                            accuracy.text = txt
                            val txt1 =
                                "Lat: " + location?.latitude?.toString() + " Lng: " + location?.longitude?.toString()
                            address.text = txt1

                            adjustMarker(location?.longitude!!, location?.latitude!!)
                            lat = location!!.latitude
                            lng = location!!.longitude
                            acc = location!!.accuracy
                        }
                    }
                } catch (ex: IllegalStateException){
                }
            }
        }
        webView.loadUrl(ip_URL)
    }

    private fun refreshMap() {
        System.out.println(ip_URL)
        webView.loadUrl(ip_URL)
    }

    private fun getLocationUpdates(){
        locationRequest = LocationRequest()
        locationRequest.interval = 50
        locationRequest.fastestInterval = 50
        locationRequest.smallestDisplacement = 0.01f // 170 m = 0.1 mile
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY //set according to your app function

//        try {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return

                if (locationResult.locations.isNotEmpty()) {
                    val lc = locationResult.lastLocation
                    val txt = "Accuracy: " + lc.accuracy.toString() + " m"
                    accuracy.text = txt
                    val txt1 = "Lat: " + lc?.latitude.toString() + " Lng: " + lc?.longitude!!.toString()
                    address.text = txt1
                    adjustMarker(lc?.longitude!!,lc?.latitude!!)
                    lat = lc.latitude
                    lng = lc.longitude
                    acc = lc.accuracy
                }

            }
        }
    }

    private fun adjustMarker(x:Double,y:Double) {
        webView.loadUrl(
            "javascript:(adjustMarker($x,$y))"
        )
    }

    //start location updates
    fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                requestLocationPermission ()
            }
            else Toast.makeText(this,"Location not acquired", Toast.LENGTH_LONG).show()
        }
        else mFusedLocationClient.requestLocationUpdates(locationRequest,locationCallback,null)
    }

    // stop location updates
    fun stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // Request User To Permit Location
    private   fun requestLocationPermission (){
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                when {
                    permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    }
                    permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    } else -> {
                }
                }
            }
        }

        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    private fun chooseAction(checkId: String,editing: Boolean) {
        if(checkId !== "") {
            // POST
           if(editing) {
               val apiInterface = ApiInterface.create().searchFarmerAddress(checkId)
               apiInterface.enqueue( object : Callback<FarmersLocationBody> {
                   override fun onResponse( call: Call<FarmersLocationBody>, response: Response<FarmersLocationBody> ) {
                       if(response?.body()?.FarmerID !== null) {
                           System.out.println(response.body())
                           dialog.hide()
                           getFarmersAddress(response.body()!!)
                       }else {
                         //  error.text = "The farmer was not found!"
                       }
                   }
                   override fun onFailure(call: Call<FarmersLocationBody>, t: Throwable) {

                       System.out.println(t)
                      // error.text = "Connection to server failed"
                   }
               })

           }
            else {
               postFarmerDetails()
           }
        } else {
            // GET
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
            val apiInterface = ApiInterface.create().searchFarmerAddress(farmerId.text.toString())

            apiInterface.enqueue( object : Callback<FarmersLocationBody> {
                override fun onResponse( call: Call<FarmersLocationBody>, response: Response<FarmersLocationBody> ) {
                    if(response?.body()?.FarmerID !== null) {
                        System.out.println(response.body())
                        progress.visibility = View.GONE
                        dialog.hide()
                        getFarmersAddress(response.body()!!)
                    }else {
                        error.text = "The farmer was not found!"
                    }
                }
                    override fun onFailure(call: Call<FarmersLocationBody>, t: Throwable) {
                        progress.visibility = View.GONE
                        System.out.println(t)
                        error.text = "Connection to server failed"
                }
            })

        }
    }

    private fun postFarmerDetails() {
        val next = findViewById<Button>(R.id.next)
        val error = findViewById<TextView>(R.id.error)
        val county = findViewById<Spinner>(R.id.county)
        val subCounty = findViewById<Spinner>(R.id.subCounty)
        val ward = findViewById<Spinner>(R.id.ward)
        val village = findViewById<EditText>(R.id.village)
        val progress = findViewById<ProgressBar>(R.id.progress)

        next.setOnClickListener {
            error.text = ""

            if (TextUtils.isEmpty(village.text.toString())) {
                error.text = "Village cannot be empty!"
                return@setOnClickListener
            }

            if(acc.toString() <= "20"){
                error.text = "Too low accuracy to display correct location. Refresh map to try again!!"
                return@setOnClickListener
            }

            progress.visibility = View.VISIBLE
            val id=intent.getStringExtra("FarmerID")
            val farmersLocationBody = FarmersLocationBody(
                id!!,
                county.selectedItem.toString(),
                subCounty.selectedItem.toString(),
                ward.selectedItem.toString(),
                lat.toString(),
                lng.toString(),
                village.text.toString().capitalize()
            )

            System.out.println(farmersLocationBody)

            val apiInterface = ApiInterface.create().postFarmerLocation(farmersLocationBody)
            apiInterface.enqueue( object : Callback<Message> {
                override fun onResponse(call: Call<Message>?, response: Response<Message>?) {
                    progress.visibility = View.GONE
                    System.out.println(response?.body())
                    if(response?.body()?.success !== null){
                        if (response?.body()?.success == "farmer exists"){
                            error.text = response?.body()?.success
                            chooseAction(id, false)
                        }
                        error.text = response.body()?.success
                        val intent = Intent(this@FarmerAddress,FarmerResources::class.java)
                       System.out.println(response.body())
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

    private fun getFarmersAddress(body : FarmersLocationBody) {
        val next = findViewById<Button>(R.id.next)
        val error = findViewById<TextView>(R.id.error)
        val county = findViewById<Spinner>(R.id.county)
        val subCounty = findViewById<Spinner>(R.id.subCounty)
        val ward = findViewById<Spinner>(R.id.ward)
        val village = findViewById<EditText>(R.id.village)
        val progress = findViewById<ProgressBar>(R.id.progress)
        next.text = "Update"

        updateSpinner(county,body.County)
        updateSpinner(subCounty,body.SubCounty)
        updateSpinner(ward,body.Ward)
        village.setText(body.Village)


        next.setOnClickListener {
            error.text = ""

            if (TextUtils.isEmpty(village.text.toString())) {
                error.text = "Village cannot be empty!"
                return@setOnClickListener
            }

            progress.visibility = View.VISIBLE
            val farmersLocationBody = FarmersLocationBody(
                body.FarmerID,
                county.selectedItem.toString(),
                subCounty.selectedItem.toString(),
                ward.selectedItem.toString(),
                body.Latitude,
                body.Longitude,
                village.text.toString().capitalize()
                )

            System.out.println(farmersLocationBody)

            val apiInterface = ApiInterface.create().putFarmerAddress(body.FarmerID, farmersLocationBody)
            apiInterface.enqueue( object : Callback<Message> {
                override fun onResponse(call: Call<Message>?, response: Response<Message>?) {
                    progress.visibility = View.GONE
                    System.out.println(response?.body()!!)
                    if(response?.body()?.success !== null){
                        error.text = response?.body()?.success
                        val intent = Intent(this@FarmerAddress, FarmerResources::class.java)
                        intent.putExtra("FarmerID", response.body()?.token)
                        intent.putExtra("editing",true)
                        startActivity(intent)
                    }

                    else {
                        error.text = response.body()?.error
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

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
        webView.loadUrl(ip_URL)
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }

    override fun onStart() {
        super.onStart()
        startLocationUpdates()
    }

    override fun onStop() {
        super.onStop()
        stopLocationUpdates()
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