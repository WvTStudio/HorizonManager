package org.wvt.horizonmgr.viewmodel

import android.util.Log
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.webapi.NetworkException
import org.wvt.horizonmgr.webapi.mgrinfo.MgrInfoModule
import javax.inject.Inject
import kotlin.math.log

private const val TAG = "DonateVMLogger"

@HiltViewModel
class DonateViewModel @Inject constructor(
    private val mgrInfoModule: MgrInfoModule
) : ViewModel() {

    data class DonateItem(
        val name: String,
        val size: TextUnit
    )

    private val _donates = MutableStateFlow(emptySet<DonateItem>())
    val donates: StateFlow<Set<DonateItem>> = _donates.asStateFlow()

    val alipayQrCodeURL = mgrInfoModule.getAlipayQRCodeURL()
    val wechatQrCodeURL = mgrInfoModule.getWechatQRCodeURL()

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = mutableSetOf<DonateItem>()
            try {
                mgrInfoModule.getDonateList().forEach {
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