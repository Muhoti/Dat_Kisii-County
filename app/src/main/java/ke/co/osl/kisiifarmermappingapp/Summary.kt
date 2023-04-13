package ke.co.osl.kisiifarmermappingapp

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import ke.co.osl.kisiifarmermappingapp.api.ApiInterface
import ke.co.osl.kisiifarmermappingapp.models.CheckForm
import retrofit2.Call
import retrofit2.Response

class Summary : AppCompatActivity() {
    lateinit var editor: SharedPreferences.Editor
    lateinit var preferences: SharedPreferences

    lateinit var name:TextView
    lateinit var id:TextView
    lateinit var phone:TextView
    lateinit var user:TextView

    lateinit var fdCheck: ImageView
    lateinit var faCheck: ImageView
    lateinit var frCheck: ImageView
    lateinit var fgCheck: ImageView
    lateinit var vcCheck: ImageView

    lateinit var finish:Button

    lateinit var fd:CardView
    lateinit var fa:CardView
    lateinit var fr:CardView
    lateinit var fg:CardView
    lateinit var vc:CardView

    lateinit var progress:ProgressBar

    lateinit var nationalID:String

    lateinit var bdy:CheckForm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        progress = findViewById(R.id.progress)

        name = findViewById(R.id.name)
        phone = findViewById(R.id.phone)
        id = findViewById(R.id.id)
        user = findViewById(R.id.user)

        fdCheck = findViewById(R.id.fd_check)
        faCheck = findViewById(R.id.fa_check)
        frCheck = findViewById(R.id.fr_check)
        fgCheck = findViewById(R.id.fg_check)
        vcCheck = findViewById(R.id.vc_check)


        preferences = this.getSharedPreferences("kisiiapp", MODE_PRIVATE)
        editor = preferences.edit()

        nationalID = preferences.getString("NationalID", "")!!

        System.out.println(nationalID)

        if(nationalID === ""){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        finish = findViewById(R.id.finish)
        finish.isEnabled = false
        finish.setOnClickListener {
            editor.putString("NationalID","")
            editor.apply()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        fd = findViewById(R.id.fd)
        fd.isEnabled = false
        fd.setOnClickListener {
            if (bdy !== null){
                if (bdy.FarmerDetails > 0){
                    val intent = Intent(this, FarmerDetails::class.java)
                    intent.putExtra("editing", "editing")
                    startActivity(intent)
                    finish()
                }else {
                    val intent = Intent(this, FarmerDetails::class.java)
                    intent.putExtra("editing", "editing")
                    startActivity(intent)
                    finish()
                }
            }
        }

        fa = findViewById(R.id.fa)
        fa.setOnClickListener {
            val intent = Intent(this, FarmerAddress::class.java)
            intent.putExtra("editing", "editing")
            startActivity(intent)
            finish()
        }

        fr = findViewById(R.id.fr)
        fr.setOnClickListener {
            val intent = Intent(this, FarmerResources::class.java)
            intent.putExtra("editing", "editing")
            startActivity(intent)
            finish()
        }

        fg = findViewById(R.id.fg)
        fg.setOnClickListener {
            val intent = Intent(this, FarmerAssociations::class.java)
            intent.putExtra("editing", "editing")
            startActivity(intent)
            finish()
        }

        vc = findViewById(R.id.vc)
        vc.setOnClickListener {
            val intent = Intent(this, ValueChains::class.java)
            intent.putExtra("editing", "editing")
            startActivity(intent)
            finish()
        }

        checkForms()
    }

    private fun checkForms() {
        try {
            progress.visibility = View.VISIBLE
            val apiInterface = ApiInterface.create().checkForms(nationalID)

            apiInterface.enqueue(object : retrofit2.Callback<CheckForm> {
                override fun onResponse(call: Call<CheckForm>, response: Response<CheckForm>) {
                    val body = response.body()
                    progress.visibility = View.GONE
                    if (body !== null) {
                        bdy = body
                        fd.isEnabled = true
                        if(body.FarmerGroups > 0 && body.FarmerDetails > 0
                            && body.FarmerAddress > 0 && body.FarmerResources > 0 && body.FarmerValueChains > 0){
                            finish.isEnabled = true
                        }
                        name.text = body.Data.Name
                        phone.text = body.Data.Phone
                        id.text = body.Data.NationalID
                        user.text = "Mapped by: " + body.Data.User

                        if (body.FarmerDetails > 0){
                           fdCheck.setBackgroundResource(R.drawable.ic_form_check)
                        }else fdCheck.setBackgroundResource(R.drawable.ic_form_unchecked)
                        if (body.FarmerAddress > 0){
                            faCheck.setBackgroundResource(R.drawable.ic_form_check)
                        }else faCheck.setBackgroundResource(R.drawable.ic_form_unchecked)
                        if (body.FarmerResources > 0){
                            frCheck.setBackgroundResource(R.drawable.ic_form_check)
                        }else frCheck.setBackgroundResource(R.drawable.ic_form_unchecked)
                        if (body.FarmerGroups > 0){
                            fgCheck.setBackgroundResource(R.drawable.ic_form_check)
                        }else fgCheck.setBackgroundResource(R.drawable.ic_form_unchecked)
                        if (body.FarmerValueChains > 0){
                            vcCheck.setBackgroundResource(R.drawable.ic_form_check)
                        }else vcCheck.setBackgroundResource(R.drawable.ic_form_unchecked)
                    }
                }
                override fun onFailure(call: Call<CheckForm>, t: Throwable) {
                    progress.visibility = View.GONE
                }
            })
        }catch (e:Exception) {
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
    }

}