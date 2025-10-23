package com.academic.softwarelearning.ui.gallery

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.academic.softwarelearning.R
import com.academic.softwarelearning.ui.android.adapter.GalleryAdapter
import com.academic.softwarelearning.databinding.FragmentGalleryBinding
import java.io.File

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)

        val files = getImageFilesFromCache()
        val adapter = GalleryAdapter(files)

        binding.galleryRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.galleryRecyclerView.adapter = adapter

        return binding.root
    }

    @SuppressLint("ResourceType")
    private fun getImageFilesFromCache(): List<File> {
        val cacheDir = requireContext().cacheDir
        val imageFiles = cacheDir.listFiles { file ->
            file.extension.lowercase() in listOf("jpg", "jpeg", "png")
        }?.toList() ?: emptyList()

        if (imageFiles.isEmpty()) {
            val mockImages = mutableListOf<File>()
            for (i in 1..6) {
                // Gera nome único com timestamp para evitar cache duplicado
                val uniqueName = "exemplo_mock_${System.currentTimeMillis()}_$i.png"
                val mockImage = File(cacheDir, uniqueName)

                val inputStream = resources.openRawResource(R.drawable.softwarelearning)
                val outputStream = mockImage.outputStream()
                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()

                mockImages.add(mockImage)

                Thread.sleep(10)
            }
            return mockImages
        }

        return imageFiles
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
