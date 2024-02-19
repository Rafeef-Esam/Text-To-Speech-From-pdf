package com.example.texttospeech.ui.readAndListen.view

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.method.ScrollingMovementMethod
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.atech.tts_engine.tts.builder.TextToSpeechHelper
import com.example.texttospeech.databinding.FragmentReadAndListenBinding
import com.example.texttospeech.ui.readAndListen.viewModel.ReadAndListenViewModel
import com.example.texttospeech.utilites.observe
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ReadAndListenFragment : Fragment() {

    private lateinit var binding: FragmentReadAndListenBinding
    private lateinit var readAndListenViewModel: ReadAndListenViewModel
    private lateinit var pdfReader : PdfReader

    private val args: ReadAndListenFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentReadAndListenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
        subscribeUi()
    }

    private fun subscribeUi() {
        observe(binding.prePage) {
            readAndListenViewModel.updateStopIndex(0)
            pause()
            readAndListenViewModel.prevPage(pdfReader)
            binding.pageContent.text = readAndListenViewModel.currentPageText
        }
        observe(binding.nextPage){
            readAndListenViewModel.updateStopIndex(0)
            pause()
            readAndListenViewModel.nextPage(pdfReader)
            binding.pageContent.text = readAndListenViewModel.currentPageText

        }
        observe(binding.playOrPause){
            if (readAndListenViewModel.isPaused.value == true){
                readAndListenViewModel.updateIsPaused(readAndListenViewModel.isPaused.value!!.not())
                pause()
            }else {
                readAndListenViewModel.updateIsPaused(readAndListenViewModel.isPaused.value!!.not())
                listen(readAndListenViewModel.currentPageText, readAndListenViewModel.stopIndex.value ?: 0)
            }
        }
        observe(readAndListenViewModel.stopIndex){
            if (it == readAndListenViewModel.currentPageText.length) {
                lifecycleScope.launch {
                    delay(1000)
                    binding.pageContent.text = readAndListenViewModel.currentPageText
                    readAndListenViewModel.updateIsPaused(false)
                    readAndListenViewModel.updateStopIndex(0)
                }
            }
        }
        observe(binding.back, findNavController()::navigateUp)
    }

    private fun initUi() {
        binding.lifecycleOwner = this
        readAndListenViewModel = ViewModelProvider(this)[ReadAndListenViewModel::class.java]
        binding.viewModel = readAndListenViewModel
        binding.pageContent.movementMethod = ScrollingMovementMethod()
        pdfReader = PdfReader(requireActivity().contentResolver.openInputStream(args.attachment.attachmentUri ?: "".toUri()))
        readAndListenViewModel.pageNumbers = pdfReader.numberOfPages
        val currentText = PdfTextExtractor.getTextFromPage(pdfReader, readAndListenViewModel.currentPage.value ?: 1).trim()
        readAndListenViewModel.currentPageText = currentText
        binding.pageContent.text = currentText
    }

    private fun listen(message : String, startIndex: Int) {
        TextToSpeechHelper
            .getInstance(requireActivity())
            .registerLifecycle(viewLifecycleOwner)
            .speak(message.drop(startIndex))
            .highlight()
            .onHighlight { pair ->
                readAndListenViewModel.updateStopIndex(pair.second + startIndex)
                val spannableString = SpannableString(message)
                spannableString.setSpan(
                    RelativeSizeSpan(1.5f),
                    pair.first + startIndex,
                    pair.second + startIndex,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                binding.pageContent.text = spannableString
            }
    }

    private fun pause(){
        TextToSpeechHelper.getInstance(requireActivity())
            .destroy()
    }

}