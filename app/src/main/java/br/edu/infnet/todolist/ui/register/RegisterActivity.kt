package br.edu.infnet.todolist.ui.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.EditText
import android.widget.Toast
import br.edu.infnet.todolist.R
import br.edu.infnet.todolist.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    private val NAME_VALIDATION_MSG = "Entre com um nome válido"
    private val EMAIL_VALIDATION_MSG = "Entre com um email válido"
    private val PASSWORD_VALIDATION_MSG = "Entre com uma senha válida"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = Firebase.auth

        cadastroFinalizarBotao.setOnClickListener {
            finalizarCadastro()
        }
    }

    private fun finalizarCadastro() {
        var nome = cadastroNomeTextView.text.toString()
        var email = cadastroEmailTextView.text.toString()
        var password = cadastroSenha1TextView.text.toString()
        Log.d("SENHA RUIM", "finalizarCadastro: $password")


        if (nome.isEmpty()) {
            setError(cadastroNomeTextView, NAME_VALIDATION_MSG)
            cadastroNomeTextView.requestFocus()
        }

        if (email.isEmpty()) {
            setError(cadastroEmailTextView, EMAIL_VALIDATION_MSG)
            cadastroEmailTextView.requestFocus()
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            setError(cadastroEmailTextView, EMAIL_VALIDATION_MSG)
            cadastroEmailTextView.requestFocus()
        }

        if (password.isEmpty()) {
            setError(cadastroSenha1TextView, PASSWORD_VALIDATION_MSG)
            cadastroSenha1TextView.requestFocus()
        }

        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.createUserWithEmailAndPassword(
                email,
                password
            )
                .addOnCompleteListener(this@RegisterActivity) { task ->
                    if (task.isSuccessful) {
                        val profileUpdates = userProfileChangeRequest {
                            displayName = nome
                        }
                        auth.currentUser!!.updateProfile(profileUpdates)

                        startActivity(Intent(this, LoginActivity::class.java))
                        makeText("Cadastro realizado com sucesso!")
                        finish()
                    }
                }
                .addOnFailureListener {
                    makeText("Tamanho mínimo de senha: 6 caracteres.")
                }
        } else {
            makeText("Favor preencha todos os campos antes de finalizar o cadastro.")
        }
    }

    private fun makeText(text: String) {
        Toast.makeText(applicationContext, text, Toast.LENGTH_LONG).show()
    }

    private fun setError(str: EditText, error: String) {
        return str.setError(error)
    }
}