package br.edu.infnet.todolist.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import br.edu.infnet.todolist.R
import br.edu.infnet.todolist.domain.adapter.GetAllTasksAdapter
import br.edu.infnet.todolist.domain.models.task.Task
import br.edu.infnet.todolist.ui.create.CreateActivity
import br.edu.infnet.todolist.ui.login.LoginActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.header_layout.*
import kotlinx.android.synthetic.main.task_card.*

class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var recyclerView: RecyclerView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var progressBar: ProgressBar
    private var userId = ""

    private var drawer: DrawerLayout? = null
    private var toolbar: Toolbar? = null
    private var TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerViewMain)
        layoutManager = LinearLayoutManager(applicationContext)
        drawer = findViewById(R.id.drawerLayout)
        toolbar = findViewById(R.id.toolbar)
        progressBar = findViewById(R.id.progressbarMainActivity)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val user = Firebase.auth.currentUser
        for (profile in user?.providerData!!) {
            userId = profile.uid
        }

        setNavigationView()
        getTasks()

        floating.setOnClickListener {
            val intent = Intent(applicationContext, CreateActivity::class.java)
            startActivity(intent)
        }

        swipe.setOnRefreshListener {
            getTasks()
            swipe.isRefreshing = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                drawer?.openDrawer(GravityCompat.START)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun logout() {
        auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun setNavigationView() {
        val user = auth.currentUser

        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)

        val navigationView = findViewById<NavigationView>(R.id.navigation)

        for (profile in user?.providerData!!) {
            val name = profile.displayName
            val email = profile.email

            val nName = navigationView.getHeaderView(0).findViewById<TextView>(R.id.txtName)
            val nEmail = navigationView.getHeaderView(0).findViewById<TextView>(R.id.txtEmail)

            nName.text = name
            nEmail.text = email
        }

        val toggle = ActionBarDrawerToggle(this, drawer, R.string.open, R.string.close)
        drawer?.addDrawerListener(toggle)
        drawer?.isClickable = true
        toggle.isDrawerIndicatorEnabled = true
        toggle.syncState()

        navigationView.setCheckedItem(R.id.home)

        navigationView.setNavigationItemSelectedListener {
            it.isChecked = true
            drawer?.closeDrawers()

            when (it.itemId) {
                R.id.home -> {
                    return@setNavigationItemSelectedListener true
                }
                R.id.logout -> {
                    logout()
                    return@setNavigationItemSelectedListener true
                }
                else -> {
                    return@setNavigationItemSelectedListener false
                }
            }
        }
    }

    private fun getTasks() {
        progressBar.visibility = View.VISIBLE
        swipe.isRefreshing = true

        db.collection(userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val tarefas = ArrayList<Task>()

                for (item in result) {
                    val tarefa = item.toObject(Task::class.java)
                    tarefas.add(tarefa)
                }

                recyclerView.layoutManager = layoutManager
                recyclerView.adapter = GetAllTasksAdapter(this@MainActivity, tarefas, db)
                swipe.isRefreshing = false

                progressBar.visibility = View.GONE
            }
            .addOnFailureListener { exception ->
                makeText("Erro ao retornar os dados do Firestore.")
                Log.e(TAG, "getTasks: ${exception.message}")
            }
    }

    private fun makeText(text: String) {
        Toast.makeText(applicationContext, text, Toast.LENGTH_LONG).show()
    }

    override fun onRefresh() {
        getTasks()
    }

    override fun onStart() {
        super.onStart()
        getTasks()
    }
}
