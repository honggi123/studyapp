package com.coworkerteam.coworker.ui.camstudy.cam

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.coworkerteam.coworker.data.UserRepository
import com.coworkerteam.coworker.data.model.api.CamstudyLeaveResponse
import com.coworkerteam.coworker.ui.base.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class CamStudyViewModel(private val model: UserRepository) : BaseViewModel() {
    val TAG = "CamStudyViewModel"

    //스터디 퇴장 데이터
    private val _CamstduyLeaveResponseLiveData = MutableLiveData<Response<CamstudyLeaveResponse>>()
    val CamstduyLeaveResponseLiveData: LiveData<Response<CamstudyLeaveResponse>>
        get() = _CamstduyLeaveResponseLiveData

    fun getCamstduyLeaveData(studyIdx: Int, studyTime: Int, restTime: Int) {
        val accessToken = model.getAccessToken()
        val nickname = model.getCurrentUserName()

        if (!accessToken.isNullOrEmpty() && !nickname.isNullOrEmpty()) {
            addDisposable(
                model.getCamStudyLeaveData(accessToken, nickname, studyIdx, studyTime, restTime)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.run {
                            Log.d(TAG, "meta : $it")
                            when {
                                it.code() == 401 -> {
                                    //액세스토큰이 만료된 경우
                                    Log.d(TAG, "액세스토큰이 만료된 경우")

                                    //액세스 토큰 재발급
                                    getReissuanceToken(TAG,model,getCamstduyLeaveData(studyIdx, studyTime, restTime))
                                }
                                it.code() > 500 -> {
                                    //서비스 서버에 문제가 있을 경우
                                    setServiceError(TAG, it.errorBody())
                                }
                                else -> {
                                    //그 외에는 값 Activity에 전달 ( 200, 400번대의 경우 )
                                    _CamstduyLeaveResponseLiveData.postValue(this)
                                }
                            }
                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        }else{
            Log.d(TAG,"getCamstduyLeaveData:: accessTokenr값 또는 nickname 값이 없습니다.")
        }
    }

    fun getNickName(): String? {
        return model.getCurrentUserName()
    }

    fun setGoalIsSuccess(goalIsSuccess: Boolean,goalSuccesstime : Int,goalPostIsWrite : Boolean ){
        model.setPreferenceIsSuccess(goalIsSuccess,goalSuccesstime,goalPostIsWrite)
    }

}