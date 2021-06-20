package br.edu.infnet.todolist.ui.update

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import br.edu.infnet.todolist.R
import br.edu.infnet.todolist.domain.`interface`.IPaisService
import br.edu.infnet.todolist.domain.models.pais.Paises
import br.edu.infnet.todolist.domain.models.task.Task
import br.edu.infnet.todolist.domain.service.RetrofitService
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_create.*
import kotlinx.android.synthetic.main.activity_update.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class UpdateActivity : AppCompatActivity() {
    private var TAG = "UpdateActivity"
    private lateinit var db: FirebaseFirestore
    private var toolbar: Toolbar? = null
    private var userId = ""
    private var task_id = ""
    private lateinit var task: Task
    private lateinit var spinner: Spinner
    private lateinit var progressbar: ProgressBar

    private var NAME_VALIDATION_MSG = "Campo título não pode ficar em branco."
    private var DESCRIPTION_VALIDATION_MSG = "Campo descrição não pode ficar em branco."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update)

        toolbar = findViewById(R.id.toolbarUpdate)
        spinner = findViewById(R.id.spinnerUpdate)
        progressbar = findViewById(R.id.progressbarUpdateActivity)
        task = Task()

        task_id = intent.extras?.getString("id")!!

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

                task.countryId = position.toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }


        val user = Firebase.auth.currentUser
        for (profile in user?.providerData!!) {
            userId = profile.uid
        }

        db = FirebaseFirestore.getInstance()
        configActionBar()
        getCountries()
        returnDataFromFirestore(task_id)

        updateTask.setOnClickListener {
            update()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun getCountries() = CoroutineScope(Dispatchers.IO).launch {
        progressbar.visibility = View.VISIBLE

        try {
            val call = RetrofitService.getInstance().create(IPaisService::class.java)
            call.getAllCountries().enqueue(object : Callback<ArrayList<Paises>> {
                override fun onResponse(
                    call: Call<ArrayList<Paises>>,
                    response: Response<ArrayList<Paises>>
                ) {
                    val paises = response.body()!!
                    val nome: ArrayList<String> = ArrayList()

                    paises.forEach {
                        nome.add(it.nome.abreviado)
                    }

                    spinner.adapter = ArrayAdapter(
                        this@UpdateActivity,
                        R.layout.support_simple_spinner_dropdown_item,
                        nome
                    )
                    spinner.setSelection(task.countryId.toInt())
                    progressbar.visibility = View.GONE
                }

                override fun onFailure(call: Call<ArrayList<Paises>>, t: Throwable) {
                    makeText("Falha ao listar os países.")
                    Log.e(TAG, "onFailure: ${t.message}")
                    progressbar.visibility = View.GONE
                }
            })
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Log.d(TAG, "getCountries: ${e.message}")
                makeText(e.message.toString())
            }
        }
    }

    private fun update() {
        task.title = editTitleUpdate.text.toString()
        task.description = editDescriptionUpdate.text.toString()
        task.country = spinner.selectedItem.toString()

        if (editTitleUpdate.text.isEmpty()) {
            setError(editTitleUpdate, NAME_VALIDATION_MSG)
            editTitleUpdate.requestFocus()
        }

        if (editDescriptionUpdate.text.isEmpty()) {
            setError(editDescriptionUpdate, DESCRIPTION_VALIDATION_MSG)
            editDescriptionUpdate.requestFocus()
        }

        if (task.title.isNotEmpty() && task.description.isNotEmpty()) {
            updateTask(task_id)
        }
    }

    private fun configActionBar() {
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun makeText(text: String) {
        Toast.makeText(applicationContext, text, Toast.LENGTH_LONG).show()
    }

    private fun setError(str: EditText, error: String) {
        return str.setError(error)
    }

    private fun returnDataFromFirestore(id: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            db.collection(userId).document(id).get().addOnSuccessListener {
                if (it.exists()) {
                    task.title = it.getString("title").toString()
                    task.description = it.getString("description").toString()
                    task.countryId = it.getString("countryId").toString()

                    editTitleUpdate.setText(task.title)
                    editDescriptionUpdate.setText(task.description)
                }
            }.addOnFailureListener {
                makeText("Erro ao retornar os dados.")
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                makeText(e.message.toString())
            }
        }
    }

    private fun updateTask(id: String) = CoroutineScope(Dispatchers.IO).launch {
        val taskMap: HashMap<String, Any> = HashMap()
        taskMap["title"] = task.title
        taskMap["description"] = task.description
        taskMap["country"] = spinner.selectedItem.toString()
        taskMap["countryId"] = task.countryId
        taskMap["date"] = task.date

        try{
            db.collection(userId).document(id).set(taskMap)
                .addOnSuccessListener {
                    makeText("Tarefa Atualizada!")
                    finish()
                }
                .addOnFailureListener {
                    makeText(it.message.toString())
                }


        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                makeText(e.message.toString())
            }
        }
    }
}