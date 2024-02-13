package com.example.texttospeech.ui.home.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.texttospeech.data.Attachment
import com.example.texttospeech.databinding.FragmentHomeBinding
import com.example.texttospeech.utilites.FileUtils
import com.example.texttospeech.utilites.observe
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor

class HomeFragment : Fragment(), TextToSpeech.OnInitListener {

    private lateinit var binding: FragmentHomeBinding

    private lateinit var tts: TextToSpeech

    private var attachment: Attachment = Attachment()

    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.apply {
                    attachment = FileUtils.uploadFile(this, requireContext())
                    binding.bookName.text = "The picked book is ${attachment.attachmentFileName}"
                }
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tts.stop()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        subscribeUi()
    }

    private fun subscribeUi() {
        observe(binding.selectPdf, ::openFilePiker)
        observe(binding.listen){speakFromPdf(attachment.attachmentUri ?: "".toUri())}
        observe(binding.read){
            if (attachment.attachmentUri == null){
                Toast.makeText(requireContext(), "Please pick file to read and listen first", Toast.LENGTH_SHORT).show()
            }else{
                navigateToReadAndListen(attachment)
            }
        }
    }

    private fun initUi() {
        tts = TextToSpeech(requireContext(), this)
    }

    private fun openFilePiker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        filePickerLauncher.launch(intent)
    }

    private fun speakFromPdf(fileUri: Uri) {
        try {
            val pdfReader = PdfReader(requireActivity().contentResolver.openInputStream(fileUri))

            val numberOfPages = pdfReader.numberOfPages

            speakNextPage(pdfReader, 0, numberOfPages)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error processing PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun speakNextPage(pdfReader: PdfReader, currentPage: Int, totalNumberOfPages: Int) {
        try {
            if (currentPage < totalNumberOfPages) {
                val currentPageText = PdfTextExtractor.getTextFromPage(pdfReader, currentPage + 1).trim()
                tts.speak(currentPageText, TextToSpeech.QUEUE_FLUSH, null, "pdfReader")
                tts.setOnUtteranceProgressListener(
                    object : UtteranceProgressListener() {
                        override fun onStart(p0: String?) {
                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(requireContext(), "Start Reading Page Number ${currentPage + 1}", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onStop(utteranceId: String?, interrupted: Boolean) {
                            super.onStop(utteranceId, interrupted)
                        }

                        override fun onDone(p0: String?) {
                            speakNextPage(pdfReader, currentPage + 1, totalNumberOfPages)
                        }

                        override fun onError(utteranceId: String?) {
                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(requireContext(), "Error $utteranceId", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onInit(status: Int) {
    }

    // navigation
    private fun navigateToReadAndListen(attachment: Attachment){
        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToReadAndListenFragment(attachment))
    }
}

