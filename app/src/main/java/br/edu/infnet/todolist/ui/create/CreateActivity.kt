package br.edu.infnet.todolist.ui.create

import android.content.Context
import android.os.Bundle
import android.util.Log
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateActivity : AppCompatActivity() {
    private var TAG = "CreateActivity"
    private lateinit var db: FirebaseFirestore
    private var toolbar: Toolbar? = null
    private var userId = ""
    private lateinit var progressbar: ProgressBar
    private lateinit var task: Task
    private lateinit var autoCompleteText: AutoCompleteTextView

    private var NAME_VALIDATION_MSG = "Campo título não pode ficar em branco."
    private var DESCRIPTION_VALIDATION_MSG = "Campo descrição não pode ficar em branco."
    private var AUTOCOMPLETE_VALIDATION_MSG = "Campo País não pode ficar em branco."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)
        toolbar = findViewById(R.id.toolbarDetail)
        progressbar = findViewById(R.id.progressbarCreateActivity)
        autoCompleteText = findViewById(R.id.autoComplete)
        task = Task()

        val user = Firebase.auth.currentUser
        for (profile in user?.providerData!!) {
            userId = profile.uid
        }

        val mViewModel = ViewModelProvider.NewInstanceFactory()
            .create(CountryViewModel::class.java)

        db = FirebaseFirestore.getInstance()
        configActionBar()
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

        createTask.setOnClickListener {
            registerTask()
        }
    }

    private fun initCountryList(mViewModel: CountryViewModel) {
        lifecycleScope.launch {
            mViewModel.init()
            initAdapter(mViewModel)
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
                this,
                R.layout.list_item,
                paises
            )
        )
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun registerTask() {
        task.title = editTitle.text.toString()
        task.description = editDescription.text.toString()
        task.country = autoCompleteText.text.toString()

        if (editTitle.text.isEmpty()) {
            setError(editTitle, NAME_VALIDATION_MSG)
            editTitle.requestFocus()
        }

        if (editDescription.text.isEmpty()) {
            setError(editDescription, DESCRIPTION_VALIDATION_MSG)
            editDescription.requestFocus()
        }

        if (autoCompleteText.text.isNullOrEmpty()) {
            setError(autoComplete, AUTOCOMPLETE_VALIDATION_MSG)
            autoComplete.requestFocus()
        }

        if (task.title.isNotEmpty() && task.description.isNotEmpty() && autoCompleteText.text.isNotEmpty()) {
            createTask(task)
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

    private fun createTask(task: Task) = CoroutineScope(Dispatchers.IO).launch {
        try {
            db.collection(userId).document().set(task)
                .addOnSuccessListener {
                    makeText("Task criada com sucesso!")
                    finish()
                }
                .addOnFailureListener { e ->
                    makeText("Erro ao criar a task.")
                    Log.e(TAG, "createTask: ${e.message}")
                }
        } catch (e: FirebaseError) {
            withContext(Dispatchers.Main) {
                throw FirebaseError("Erro ao criar a task.", e)
            }
        }
    }
}