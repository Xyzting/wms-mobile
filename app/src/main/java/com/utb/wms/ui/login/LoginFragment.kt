package com.utb.wms.ui.login

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.utb.wms.R
import com.utb.wms.databinding.FragmentLoginBinding
import com.utb.wms.ui.common.appContainer
import kotlinx.coroutines.launch

class LoginFragment : Fragment(R.layout.fragment_login) {

    private var binding: FragmentLoginBinding? = null

    private val viewModel: LoginViewModel by viewModels {
        LoginViewModel.factory(appContainer.authRepository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tampilan = FragmentLoginBinding.bind(view)
        binding = tampilan

        tampilan.inputUsername.doAfterTextChanged { teks ->
            viewModel.onUsernameChange(teks?.toString().orEmpty())
        }
        tampilan.inputPassword.doAfterTextChanged { teks ->
            viewModel.onPasswordChange(teks?.toString().orEmpty())
        }
        tampilan.tombolMasuk.setOnClickListener { masuk() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    tampilan.tombolMasuk.isEnabled = state.dapatMasuk
                    tampilan.progres.visibility =
                        if (state.sedangMemuat) View.VISIBLE else View.GONE
                    tampilan.textGalat.text = state.galat.orEmpty()
                    tampilan.textGalat.visibility =
                        if (state.galat == null) View.GONE else View.VISIBLE
                }
            }
        }
    }

    private fun masuk() {
        viewModel.login {
            findNavController().navigate(R.id.action_login_to_dashboard)
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
