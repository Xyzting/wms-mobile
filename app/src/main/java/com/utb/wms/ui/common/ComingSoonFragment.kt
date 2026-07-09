package com.utb.wms.ui.common

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.utb.wms.R
import com.utb.wms.databinding.FragmentComingSoonBinding

class ComingSoonFragment : Fragment(R.layout.fragment_coming_soon) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentComingSoonBinding.bind(view)
        val judulRes = arguments?.getInt(ARG_JUDUL) ?: 0
        val penanggungJawab = arguments?.getString(ARG_PENANGGUNG_JAWAB).orEmpty()

        binding.textJudul.text =
            if (judulRes != 0) getString(judulRes) else getString(R.string.app_name)
        binding.textPenanggungJawab.text =
            getString(R.string.penanggung_jawab, penanggungJawab)
    }

    private companion object {
        const val ARG_JUDUL = "judul"
        const val ARG_PENANGGUNG_JAWAB = "penanggungJawab"
    }
}
