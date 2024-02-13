package com.example.texttospeech.ui.readAndListen.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReadAndListenViewModel : ViewModel() {

    private val _currentPage = MutableLiveData<Int>()
    val currentPage: LiveData<Int> = _currentPage

    private val _isPaused = MutableLiveData<Boolean>()
    val isPaused: LiveData<Boolean> = _isPaused

    private val _stopIndex = MutableLiveData<Int>()
    val stopIndex: LiveData<Int> = _stopIndex

    var pageNumbers = 0

    var currentPageText = ""

    init {
        _currentPage.value = 1
        _isPaused.value = false
        _stopIndex.value = 0
    }

    fun prevPage(pdfReader: PdfReader) {
        if (currentPage.value!! > 1) {
            _currentPage.value = _currentPage.value?.minus(1)
            setPageContent(pdfReader)
            updateIsPaused(false)
        }
    }

    fun nextPage(pdfReader: PdfReader) {
        if (currentPage.value!! < pageNumbers) {
            _currentPage.value = _currentPage.value?.plus(1)
            setPageContent(pdfReader)
            updateIsPaused(false)
        }
    }

    fun updateIsPaused(isPaused : Boolean){
        viewModelScope.launch(Dispatchers.Main){
            _isPaused.value = isPaused
        }
    }

    fun updateStopIndex(stopIndex : Int){
        viewModelScope.launch(Dispatchers.Main){
            _stopIndex.value = stopIndex
        }
    }

    private fun setPageContent(pdfReader: PdfReader) {
        currentPageText = PdfTextExtractor.getTextFromPage(pdfReader, _currentPage.value!!).trim()
    }
}