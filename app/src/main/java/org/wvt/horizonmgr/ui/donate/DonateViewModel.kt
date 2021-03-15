package org.wvt.horizonmgr.ui.donate

import android.util.Log
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.DependenciesContainer
import org.wvt.horizonmgr.webapi.NetworkException
import kotlin.math.log

private const val TAG = "DonateVMLogger"

class DonateViewModel(
    dependencies: DependenciesContainer
) : ViewModel() {
    private val mgrInfo = dependencies.mgrInfo

    data class DonateItem(
        val name: String,
        val size: TextUnit
    )

    private val _donates = MutableStateFlow(emptySet<DonateItem>())
    val donates: StateFlow<Set<DonateItem>> = _donates.asStateFlow()

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = mutableSetOf<DonateItem>()
            try {
                mgrInfo.getDonateList().forEach {
                    result.add(
                        DonateItem(
                            name = it.donorName,
                            size = (log((it.money / 100f) + 1f, 1.5f) * 2).sp
                        )
                    )
                }
            } catch (e: NetworkException) {
                // TODO: 2021/2/8 添加网络错误提示
            } catch (e: Exception) {
                Log.e(TAG, "获取捐赠列表失败", e)
                return@launch
            }
            _donates.emit(result)
        }
    }
}