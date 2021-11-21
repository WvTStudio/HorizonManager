package org.wvt.horizonmgr.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnlineModDetailViewModel @Inject constructor(
    val savedStateHandle: SavedStateHandle
) : ViewModel() {
}