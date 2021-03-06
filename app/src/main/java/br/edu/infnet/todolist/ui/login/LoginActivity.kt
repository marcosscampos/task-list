package br.edu.infnet.todolist.ui.login

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.edu.infnet.todolist.R
import br.edu.infnet.todolist.ui.main.MainActivity
import br.edu.infnet.todolist.ui.register.RegisterActivity
import com.facebook.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider


class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var callbackManager : CallbackManager

    private val EMAIL_VALIDATION_MSG = "Entre com um email válido"
    private val PASSWORD_VALIDATION_MSG = "Entre com uma senha válida"
    private val EMAIL_OR_PASSWORD_INCORRECT = "Email ou senha inválida, tente novamente."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginButton.setOnClickListener {
            logIn()
        }
        cadastroButton.setOnClickListener {
            cadastro()
        }

        login_button.setOnClickListener {
            setFacebookLogin()
        }

        auth = Firebase.auth
        callbackManager = CallbackManager.Factory.create()
    }

    private fun setFacebookLogin() {
        login_button.setReadPermissions("email")
        login_button.registerCallback(callbackManager, object: FacebookCallback<LoginResult>{
            override fun onSuccess(result: LoginResult?) {
                handleFacebookAccessToken(result?.accessToken!!)
            }

            override fun onCancel() {}

            override fun onError(error: FacebookException?) {
                makeText(error.toString())
            }
        })
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) {
                if(it.isSuccessful) {
                    makeText("Login feito com sucesso!")
                    val mainIntent = Intent(this, MainActivity::class.java)
                    startActivity(mainIntent)
                    finish()
                } else {
                    makeText("Erro ao logar com o facebook.")
                    Log.e("ERROR_FACEBOOK", "handleFacebookAccessToken: ${it.exception}", )
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun logIn() {
        val email = emailTextView.text.toString()
        val password = senhaTextView.text.toString()

        if (email.isEmpty()) {
            setError(emailTextView, EMAIL_VALIDATION_MSG)
            emailTextView.requestFocus()
        }

        if (password.isEmpty()) {
            setError(senhaTextView, PASSWORD_VALIDATION_MSG)
            senhaTextView.requestFocus()
        }

        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val mainIntent = Intent(this, MainActivity::class.java)
                        startActivity(mainIntent)
                        finish()
                    }
                }.addOnFailureListener {
                    makeText(EMAIL_OR_PASSWORD_INCORRECT)
                    Log.d("LOGIN_ACTIVITY", "logIn: ${it.message}")
                }
        }
    }

    private fun cadastro() {
        val cadastroIntent = Intent(this, RegisterActivity::class.java)
        startActivity(cadastroIntent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }


    private fun makeText(text: String) {
        Toast.makeText(applicationContext, text, Toast.LENGTH_LONG).show()
    }

    private fun setError(str: EditText, error: String) {
        return str.setError(error)
    }
}