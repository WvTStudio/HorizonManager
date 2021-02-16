package org.wvt.horizonmgr.ui.onlinemods

import androidx.lifecycle.ViewModel
import org.wvt.horizonmgr.DependenciesContainer

class NewOnlineViewModel(
    dependencies: DependenciesContainer
) : ViewModel() {
    private val chineseModRepository = dependencies.chineseModRepository
    private val cdnModRepository = dependencies.cdnModRepository

}