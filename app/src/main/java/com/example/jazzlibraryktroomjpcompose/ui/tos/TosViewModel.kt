// app/src/main/java/com/example/jazzlibraryktroomjpcompose/ui/tos/TosViewModel.kt

package com.example.jazzlibraryktroomjpcompose.ui.tos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jazzlibraryktroomjpcompose.data.TosPolicyManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TosViewModel @Inject constructor(
    private val tosPolicyManager: TosPolicyManager
) : ViewModel() {

    val isTosAccepted: StateFlow<Boolean> = tosPolicyManager.isTosAccepted
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val isPrivacyPolicyAccepted: StateFlow<Boolean> = tosPolicyManager.isPrivacyPolicyAccepted
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val isYoutubeTosAccepted: StateFlow<Boolean> = tosPolicyManager.isYoutubeTosAccepted
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun acceptAllPolicies() {
        viewModelScope.launch {
            tosPolicyManager.acceptAllPolicies()
        }
    }

    fun rejectPolicies() {
        viewModelScope.launch {
            tosPolicyManager.rejectPolicies()
        }
    }

    fun resetAcceptance() {
        viewModelScope.launch {
            tosPolicyManager.resetAcceptance()
        }
    }
}