package br.edu.infnet.todolist.domain.models.task

import com.google.firebase.firestore.DocumentId
import com.google.gson.annotations.SerializedName
import java.util.*

data class Task(
    @DocumentId
    var id: String = "",

    @SerializedName("title")
    var title: String = "",

    @SerializedName("description")
    var description: String = "",

    @SerializedName("country")
    var country: String = "",

    @SerializedName("countryId")
    var countryId: String = "1",

    @SerializedName("date")
    var date: Date = Calendar.getInstance().time
)