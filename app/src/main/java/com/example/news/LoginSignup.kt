package com.example.news

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.media.Image
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AlignmentSpan
import android.util.Log
import android.util.Patterns
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class LoginSignup : AppCompatActivity() {
    lateinit var login: TextView
    lateinit var signUp: TextView
    lateinit var existingAccount: TextView
    lateinit var registerYourself: TextView

    lateinit var loginButton: Button
    lateinit var signUpButton: Button

    lateinit var emailLogin: EditText
    lateinit var passwordLogin: EditText
    lateinit var nameSignUp: EditText
    lateinit var emailSignUp: EditText
    lateinit var passwordSignUp: EditText

    lateinit var googleIcon: ImageView
    lateinit var login_button: LoginButton

    lateinit var termsAndConditionCheckBox: CheckBox

    lateinit var progressBar: ProgressBar

    lateinit var loginCardView: CardView
    lateinit var signUpCardView: CardView

    lateinit var auth: FirebaseAuth
    lateinit var googleSignInClient: GoogleSignInClient

    var callbackManager = CallbackManager.Factory.create()

    var newsList = ArrayList<Article>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_signup)
        supportActionBar?.hide()

        login = findViewById(R.id.loginMain)
        signUp = findViewById(R.id.signUpMain)
        registerYourself = findViewById(R.id.registerYourself)
        existingAccount = findViewById(R.id.existingAccount)

        emailLogin = findViewById(R.id.emailLogin)
        passwordLogin = findViewById(R.id.passwordLogin)
        nameSignUp = findViewById(R.id.nameSignUp)
        emailSignUp = findViewById(R.id.emailSignUp)
        passwordSignUp = findViewById(R.id.passwordSignUp)

        googleIcon = findViewById(R.id.googleIcon)
        login_button = findViewById<LoginButton>(R.id.login_button)

        termsAndConditionCheckBox = findViewById(R.id.termAndConditionsCheckBox)

        progressBar = findViewById(R.id.progressBar)

        loginButton = findViewById(R.id.loginButton)
        signUpButton = findViewById(R.id.signUpButton)

        loginCardView = findViewById(R.id.loginCardView)
        signUpCardView = findViewById(R.id.signUpCardView)

        auth = FirebaseAuth.getInstance()
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        login_button.setOnClickListener {
            if(userIsLoggedIn()){
                auth.signOut()
            }
            else{
                LoginManager.getInstance().logInWithReadPermissions(this, listOf("public_profile", "email"))
            }
        }

        login_button.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                // ...
            }

            override fun onError(error: FacebookException) {
                // ...
            }
        })

        login.setOnClickListener {
            switchFromSignUpToLogin()
        }

        signUp.setOnClickListener {
            switchFromLoginToSignUp()
        }

        existingAccount.setOnClickListener {
            switchFromSignUpToLogin()
        }

        registerYourself.setOnClickListener {
            switchFromLoginToSignUp()
        }

        loginButton.setOnClickListener {
            loginUser()
        }

        signUpButton.setOnClickListener {
            signUpUser()
        }

        googleIcon.setOnClickListener {
            signInViaGoogle()
        }
    }

    private fun userIsLoggedIn(): Boolean {
        if (auth.currentUser != null && AccessToken.getCurrentAccessToken()!!.isExpired) {
            return true
        }
        return false
    }

    private fun signInViaGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result ->
        if(result.resultCode == Activity.RESULT_OK){
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResults(task)
        }
    }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if(task.isSuccessful){
            val account: GoogleSignInAccount? = task.result
            if(account != null){
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential).addOnCompleteListener {
                    if(it.isSuccessful){
                        showNews()
                    }
                }
            }
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser != null){
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun loginUser() {
        auth.signInWithEmailAndPassword(
            emailLogin.text.toString(),
            passwordLogin.text.toString()
        )
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    showNews()
                } else {
                    Toast.makeText(
                        baseContext, "Authentication failed. Try Again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                showNews()
                Toast.makeText(applicationContext, "Login Successful", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showNews() {
        progressBar.visibility = View.VISIBLE

        val url = "https://newsapi.org/v2/everything?q=tesla&from=2022-07-09&sortBy=publishedAt&apiKey=db9d1651d86044bf9dfacb755f316b80"
        apiCall(url)
    }

    fun signUpUser(){
        if(nameSignUp.text.toString().isEmpty()){
            Toast.makeText(applicationContext, "Please enter your Name to continue", Toast.LENGTH_SHORT).show()
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(emailSignUp.text.toString()).matches()){
            Toast.makeText(applicationContext, "Please enter valid Email to continue", Toast.LENGTH_SHORT).show()
        }

        if(emailSignUp.text.toString().isEmpty()){
            Toast.makeText(applicationContext, "Please enter your Email to continue", Toast.LENGTH_SHORT).show()
        }

        if(passwordSignUp.text.toString().isEmpty()){
            Toast.makeText(applicationContext, "Please enter your Password to continue", Toast.LENGTH_SHORT).show()
        }

        if(!termsAndConditionCheckBox.isChecked){
            val toast = "Please agree to the Terms & Conditions to continue further"
            val centeredText = SpannableString(toast);
            centeredText.setSpan(
                AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, toast.length - 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            Toast.makeText(this, centeredText, Toast.LENGTH_SHORT).show()
        }

        auth.createUserWithEmailAndPassword(emailSignUp.text.toString(), passwordSignUp.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    switchFromSignUpToLogin()
                } else {
                    Toast.makeText(baseContext, "Authentication failed. Try Again", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun switchFromLoginToSignUp(){
        login.setBackgroundResource(R.drawable.unselected_tab_style)
        signUp.setBackgroundResource(R.drawable.selected_tab_style)
        loginCardView.visibility = View.INVISIBLE
        loginButton.visibility = View.INVISIBLE
        signUpCardView.visibility = View.VISIBLE
        signUpButton.visibility = View.VISIBLE
    }

    private fun switchFromSignUpToLogin(){
        login.setBackgroundResource(R.drawable.selected_tab_style)
        signUp.setBackgroundResource(R.drawable.unselected_tab_style)
        loginCardView.visibility = View.VISIBLE
        loginButton.visibility = View.VISIBLE
        signUpCardView.visibility = View.INVISIBLE
        signUpButton.visibility = View.INVISIBLE
    }

    private fun apiCall(url: String) {
        val queue = Volley.newRequestQueue(this)
        val jsonObjectRequest = @RequiresApi(Build.VERSION_CODES.O)
        object: JsonObjectRequest(
            Request.Method.GET, url, null,
            {
                val jsonArray = it.getJSONArray("articles")

                for(i in 0 until jsonArray.length()){
                    val newsJsonObject: JSONObject = jsonArray.getJSONObject(i)

                    val sourceJsonObject: JSONObject = newsJsonObject.getJSONObject("source")
                    val time = getTime(newsJsonObject.getString("publishedAt"))

                    val news = Article(
                        newsJsonObject.getString("author"),
                        newsJsonObject.getString("description"),
                        time,
                        sourceJsonObject.getString("name"),
                        newsJsonObject.getString("title"),
                        newsJsonObject.getString("url"),
                        newsJsonObject.getString("urlToImage")
                    )

                    newsList.add(news)
                }

                val intent = Intent(this, News::class.java)
                intent.putExtra("News", newsList)
                startActivity(intent)
                finish()
            },
            {
                Toast.makeText(applicationContext, "Failed", Toast.LENGTH_SHORT).show()
            }
        )

        {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["User-Agent"] = "Mozilla/5.0"
                return headers
            }
        }
        queue.add(jsonObjectRequest)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getTime(time: String): String {
        val sdf = SimpleDateFormat("hh")

        var currentTime = sdf.format(Date())
        val oldTime = time.substring(11, 13)

        return (24 - (oldTime.toInt() - currentTime.toInt())).toString()
    }
}