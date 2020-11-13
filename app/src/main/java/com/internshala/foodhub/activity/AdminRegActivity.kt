package com.internshala.foodhub.activity

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.internshala.foodhub.R
import com.internshala.foodhub.util.ConnectionManager
import com.internshala.foodhub.util.REGISTER
import com.internshala.foodhub.util.SessionManager
import com.internshala.foodhub.util.Validations
import org.json.JSONObject
import java.lang.Exception

class AdminRegActivity : AppCompatActivity() {

    lateinit var toolbar: Toolbar
    lateinit var btnRegister: Button
    lateinit var etName: EditText
    lateinit var etPhoneNumber: EditText
    lateinit var etPassword: EditText
    lateinit var etEmail: EditText
    lateinit var etConfirmPassword: EditText
    lateinit var progressBar: ProgressBar
    lateinit var rlRegister: RelativeLayout
    lateinit var sharedPreferences: SharedPreferences
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_reg)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Register Yourself"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setTitleTextAppearance(this, R.style.PoppinsTextAppearance)
        sessionManager = SessionManager(this@AdminRegActivity)
        sharedPreferences = this@AdminRegActivity.getSharedPreferences(sessionManager.PREF_NAME, sessionManager.PRIVATE_MODE)
        rlRegister = findViewById(R.id.rlRegister)
        etName = findViewById(R.id.etName)
        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        progressBar = findViewById(R.id.progressBar)

        rlRegister.visibility = View.VISIBLE
        progressBar.visibility = View.INVISIBLE


        btnRegister.setOnClickListener {
            rlRegister.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE


            if (Validations.validateNameLength(etName.text.toString())) {
                etName.error = null
                if (Validations.validateEmail(etEmail.text.toString())) {
                    etEmail.error = null
                    if (Validations.validateMobile(etPhoneNumber.text.toString())) {
                        etPhoneNumber.error = null
                        if (Validations.validatePasswordLength(etPassword.text.toString())) {
                            etPassword.error = null
                            if (Validations.matchPassword(
                                    etPassword.text.toString(),
                                    etConfirmPassword.text.toString()
                                )
                            ) {
                                etPassword.error = null
                                etConfirmPassword.error = null
                                if (ConnectionManager().isNetworkAvailable(this@AdminRegActivity)) {
                                    sendAdminRegisterRequest(
                                        etName.text.toString(),
                                        etPhoneNumber.text.toString(),
                                        etPassword.text.toString(),
                                        etEmail.text.toString()
                                    )
                                } else {
                                    rlRegister.visibility = View.VISIBLE
                                    progressBar.visibility = View.INVISIBLE
                                    Toast.makeText(this@AdminRegActivity, "No Internet Connection", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            } else {
                                rlRegister.visibility = View.VISIBLE
                                progressBar.visibility = View.INVISIBLE
                                etPassword.error = "Passwords don't match"
                                etConfirmPassword.error = "Passwords don't match"
                                Toast.makeText(this@AdminRegActivity, "Passwords don't match", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        } else {
                            rlRegister.visibility = View.VISIBLE
                            progressBar.visibility = View.INVISIBLE
                            etPassword.error = "Password should be more than or equal 4 digits"
                            Toast.makeText(
                                this@AdminRegActivity,
                                "Password should be more than or equal 4 digits",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        rlRegister.visibility = View.VISIBLE
                        progressBar.visibility = View.INVISIBLE
                        etPhoneNumber.error = "Invalid Mobile number"
                        Toast.makeText(this@AdminRegActivity, "Invalid Mobile number", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    rlRegister.visibility = View.VISIBLE
                    progressBar.visibility = View.INVISIBLE
                    etEmail.error = "Invalid Email"
                    Toast.makeText(this@AdminRegActivity, "Invalid Email", Toast.LENGTH_SHORT).show()
                }
            } else {
                rlRegister.visibility = View.VISIBLE
                progressBar.visibility = View.INVISIBLE
                etName.error = "Invalid Name"
                Toast.makeText(this@AdminRegActivity, "Invalid Name", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun sendAdminRegisterRequest(name: String, phone: String, password: String, email: String) {
        val queue = Volley.newRequestQueue(this)

        val jsonParams = JSONObject()
        jsonParams.put("name", name)
        jsonParams.put("mobile_number", phone)
        jsonParams.put("password", password)
        jsonParams.put("email", email)

        val jsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST,
            REGISTER,
            jsonParams,
            Response.Listener {
                try {
                    val data = it.getJSONObject("data")
                    val success = data.getBoolean("success")
                    if (success) {
                        val response = data.getJSONObject("data")
                        sharedPreferences.edit()
                            .putString("user_id", response.getString("user_id")).apply()
                        sharedPreferences.edit()
                            .putString("user_name", response.getString("name")).apply()
                        sharedPreferences.edit()
                            .putString(
                                "user_mobile_number",
                                response.getString("mobile_number")
                            )
                            .apply()
                        sharedPreferences.edit()
                            .putString("user_email", response.getString("email")).apply()
                        sessionManager.setLogin(true)
                        val regIntent = Intent(this@AdminRegActivity,AdminDashboard::class.java)
                        //intent line is added here.
                        startActivity(regIntent)
                        finish()
                    } else {
                        rlRegister.visibility = View.VISIBLE
                        progressBar.visibility = View.INVISIBLE
                        val errorMessage = data.getString("errorMessage")
                        Toast.makeText(
                            this@AdminRegActivity,
                            errorMessage,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception){
                    rlRegister.visibility = View.VISIBLE
                    progressBar.visibility = View.INVISIBLE
                    e.printStackTrace()
                }
            },
            Response.ErrorListener {
                Toast.makeText(this@AdminRegActivity, it.message, Toast.LENGTH_SHORT).show()
                rlRegister.visibility = View.VISIBLE
                progressBar.visibility = View.INVISIBLE
            }
        ){
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-type"] = "application/json"

                /*The below used token will not work, kindly use the token provided to you in the training*/
                headers["token"] = "76f8f7efe45b29"
                return headers
            }
        }
        queue.add(jsonObjectRequest)
    }

    override fun onSupportNavigateUp(): Boolean {
        Volley.newRequestQueue(this).cancelAll(this::class.java.simpleName)
        onBackPressed()
        return true
    }
}
