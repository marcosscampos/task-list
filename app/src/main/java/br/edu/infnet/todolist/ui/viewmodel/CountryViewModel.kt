package br.edu.infnet.todolist.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.infnet.todolist.domain.`interface`.IPaisService
import br.edu.infnet.todolist.domain.exception.PaisError
import br.edu.infnet.todolist.domain.models.pais.Paises
import br.edu.infnet.todolist.domain.service.RetrofitService
import kotlinx.coroutines.launch

class CountryViewModel : ViewModel() {
    private val _countryList = MutableLiveData<ArrayList<Paises>>()
    val countryList: LiveData<ArrayList<Paises>>
        get() = _countryList

    private val _progressBar = MutableLiveData<Boolean>()
    val progressBar: LiveData<Boolean>
        get() = _progressBar

    private val _toast = MutableLiveData<String>()
    val toast: LiveData<String>
        get() = _toast

    suspend fun init() {
        launchDataLoad { getAllCountries() }
    }

    suspend fun getAllCountries() {
        viewModelScope.launch {
            RetrofitService.getInstance().create(IPaisService::class.java).getAllCountries().let {
                _countryList.value = it
            }
        }
    }

    fun onToastShown() {
        _toast.value = ""
    }

    private fun launchDataLoad(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                _progressBar.value = true
                block()
            } catch (error: PaisError) {
                _toast.value = error.message
            } finally {
                _progressBar.value = false
            }
        }
    }
}