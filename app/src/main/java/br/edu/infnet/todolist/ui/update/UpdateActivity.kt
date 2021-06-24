package br.edu.infnet.todolist.ui.update

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import br.edu.infnet.todolist.R
import br.edu.infnet.todolist.domain.exception.FirebaseError
import br.edu.infnet.todolist.domain.models.task.Task
import br.edu.infnet.todolist.ui.viewmodel.CountryViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_create.*
import kotlinx.android.synthetic.main.activity_update.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class UpdateActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private var toolbar: Toolbar? = null
    private var userId = ""
    private var task_id = ""
    private lateinit var task: Task
    private lateinit var progressbar: ProgressBar
    private lateinit var autoCompleteText: AutoCompleteTextView

    private var NAME_VALIDATION_MSG = "Campo título não pode ficar em branco."
    private var DESCRIPTION_VALIDATION_MSG = "Campo descrição não pode ficar em branco."
    private var AUTOCOMPLETE_VALIDATION_MSG = "Campo País não pode ficar em branco."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update)

        toolbar = findViewById(R.id.toolbarUpdate)
        progressbar = findViewById(R.id.progressbarUpdateActivity)
        task = Task()

        task_id = intent.extras?.getString("id")!!
        autoCompleteText = findViewById(R.id.autoCompleteUpdate)

        val mViewModel = ViewModelProvider.NewInstanceFactory()
            .create(CountryViewModel::class.java)
        initCountryList(mViewModel)
        setKeyboard()

        mViewModel.progressBar.observe(this) { isShown ->
            if (isShown) {
                progressbar.visibility = View.VISIBLE
            } else {
                progressbar.visibility = View.GONE
            }
        }

        mViewModel.toast.observe(this) {
            it?.let {
                makeText(it)
            }
        }

        val user = Firebase.auth.currentUser
        for (profile in user?.providerData!!) {
            userId = profile.uid
        }

        db = FirebaseFirestore.getInstance()
        configActionBar()
        returnDataFromFirestore(task_id)

        updateTask.setOnClickListener {
            update()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun initCountryList(mViewModel: CountryViewModel) {
        lifecycleScope.launch {
            mViewModel.init()
            delay(500)
            initAdapter(mViewModel)
        }
    }

    private fun setKeyboard() {
        autoCompleteText.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val input: InputMethodManager = getSystemService(
                    Context.INPUT_METHOD_SERVICE) as InputMethodManager
                input.hideSoftInputFromWindow(view.applicationWindowToken, 0)
            }
    }

    private fun initAdapter(mViewModel: CountryViewModel) {
        val paises = ArrayList<String>()
        mViewModel.countryList.observe(this) { it ->
            it.forEach {
                paises.add(it.name)
            }
        }
        autoCompleteText.setAdapter(
            ArrayAdapter(
                this@UpdateActivity,
                R.layout.list_item,
                paises
            )
        )
    }

    private fun update() {
        task.title = editTitleUpdate.text.toString()
        task.description = editDescriptionUpdate.text.toString()
        task.country = autoCompleteText.text.toString()

        if (editTitleUpdate.text.isEmpty()) {
            setError(editTitleUpdate, NAME_VALIDATION_MSG)
            editTitleUpdate.requestFocus()
        }

        if (editDescriptionUpdate.text.isEmpty()) {
            setError(editDescriptionUpdate, DESCRIPTION_VALIDATION_MSG)
            editDescriptionUpdate.requestFocus()
        }

        if (autoCompleteText.text.isNullOrEmpty()) {
            setError(autoCompleteUpdate, AUTOCOMPLETE_VALIDATION_MSG)
            autoCompleteUpdate.requestFocus()
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
                    task.country = it.getString("country").toString()

                    editTitleUpdate.setText(task.title)
                    editDescriptionUpdate.setText(task.description)
                    autoCompleteUpdate.setText(task.country)
                }
            }.addOnFailureListener {
                makeText("Erro ao retornar os dados.")
            }
        } catch (e: FirebaseError) {
            withContext(Dispatchers.Main) {
                throw FirebaseError("Erro ao retornar os dados.", e)
            }
        }
    }

    private fun updateTask(id: String) = CoroutineScope(Dispatchers.IO).launch {
        val taskMap: HashMap<String, Any> = HashMap()
        taskMap["title"] = task.title
        taskMap["description"] = task.description
        taskMap["country"] = task.country
        taskMap["date"] = task.date

        try {
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