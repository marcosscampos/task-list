package br.edu.infnet.todolist.domain.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import br.edu.infnet.todolist.R
import br.edu.infnet.todolist.domain.models.task.Task
import br.edu.infnet.todolist.ui.update.UpdateActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GetAllTasksAdapter(
    private val context: Context,
    private var tasks: ArrayList<Task>,
    private val db: FirebaseFirestore
) :
    RecyclerView.Adapter<GetAllTasksAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GetAllTasksAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_card, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: GetAllTasksAdapter.ViewHolder, position: Int) {
        val task = tasks[position]
        holder.txtTitle.text = task.title
        holder.txtDescription.text = task.description
        holder.txtCountry.text = task.country

        holder.cardView.setOnClickListener {
            readTask(task.id, task)
        }

        holder.deleteTask.setOnClickListener {
            deleteTask(task.id, position)
        }

        holder.editTask.setOnClickListener {
            val intent = Intent(context, UpdateActivity::class.java)
            intent.putExtra("id", task.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = tasks.size

    inner class ViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        internal var txtTitle: TextView
        internal var txtDescription: TextView
        internal var txtCountry: TextView
        internal var cardView: CardView
        internal var deleteTask: MaterialButton
        internal var editTask: MaterialButton

        init {
            txtTitle = view.findViewById(R.id.txtTitle)
            txtDescription = view.findViewById(R.id.txtDescription)
            txtCountry = view.findViewById(R.id.txtCountry)
            cardView = view.findViewById(R.id.cardView)
            deleteTask = view.findViewById(R.id.removeTask)
            editTask = view.findViewById(R.id.editTask)
        }
    }

    private fun readTask(id: String, task: Task) {
        val user = Firebase.auth.currentUser
        var userId = ""
        for (profile in user?.providerData!!) {
            userId = profile.uid
        }

        val builder = AlertDialog.Builder(context)
        builder.setTitle(task.title)
        builder.setMessage(getString(task))
        db.collection(userId).document(id).get().addOnSuccessListener {
            if (it.exists()) {
                task.title = it.getString("title").toString()
                task.description = it.getString("description").toString()
                task.country = it.getString("country").toString()
            }
        }.addOnFailureListener {
            makeText("Erro ao retornar os dados.")
        }

        builder.setNegativeButton("Fechar") { _, _ -> {} }
        builder.show()
    }


    private fun deleteTask(id: String, position: Int) {
        val user = Firebase.auth.currentUser
        var userId = ""
        for (profile in user?.providerData!!) {
            userId = profile.uid
        }

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Deletar tarefa")
        builder.setMessage("Tem certeza que deseja deletar essa tarefa?")

        builder.setPositiveButton("Sim") { _, _ ->
            db.collection(userId).document(id).delete()
                .addOnSuccessListener {
                    makeText("Tarefa excluída com sucesso!")

                    tasks.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, tasks.size)
                }
                .addOnFailureListener { e ->
                    makeText("Erro ao excluir tarefa.")
                    Log.e("ACTIVITY_ERROR", "deleteTask: ${e.message}")
                }
        }
        builder.setNegativeButton("Não") { _, _ -> {} }
        builder.show()
    }

    private fun makeText(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }

    private fun getString(task: Task): String {
        return ("Descrição: "
                + "\n"
                + task.description
                + "\n\n"
                + "País: " + task.country)
    }
}