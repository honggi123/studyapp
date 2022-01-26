package com.coworkerteam.coworker.ui.study.make

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.coworkerteam.coworker.data.UserRepository
import com.coworkerteam.coworker.data.model.api.ApiRequest
import com.coworkerteam.coworker.data.model.api.EnterCamstudyResponse
import com.coworkerteam.coworker.data.model.api.StudyRequest
import com.coworkerteam.coworker.ui.base.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Response

class MakeStudyViewModel(private val model: UserRepository) : BaseViewModel() {
    val TAG = "MakeStudyViewModel"

    //스터디 만들기
    private val _MakeStudyResponseLiveData = MutableLiveData<Response<StudyRequest>>()
    val MakeStudyResponseLiveData: LiveData<Response<StudyRequest>>
        get() = _MakeStudyResponseLiveData

    //스터디 입장전 데이터
    private val _EnterCamstudyResponseLiveData = MutableLiveData<Response<EnterCamstudyResponse>>()
    val EnterCamstudyResponseLiveData: LiveData<Response<EnterCamstudyResponse>>
        get() = _EnterCamstudyResponseLiveData

    fun setMakeStudyData(studyType: String, name: String, category: String, imgUrl: String, pw: String?, maxNum: Int, introduce: String) {
        val accessToken = model.getAccessToken()
        val reqType = "make"

        if (!accessToken.isNullOrEmpty()) {
            addDisposable(
                model.setMakeStudyData(accessToken, reqType, studyType, name, category,imgUrl, pw, maxNum, introduce)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.run {
                            _MakeStudyResponseLiveData.postValue(this)
                            Log.d(TAG, "meta : " + it.toString())
                            it.errorBody().toString()
                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        } else {
            Log.d(TAG, "getCamstduyLeaveData:: accessTokenr값 또는 nickname 값이 없습니다.")
        }
    }

    fun getEnterCamstduyData(studyIdx: Int, password: String?) {
        val accessToken = model.getAccessToken()

        if (!accessToken.isNullOrEmpty()) {
            addDisposable(
                model.getEnterCamStudyData(accessToken, studyIdx, password)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        it.run {
                            if (isSuccessful) {
                                it.body()!!.result.studyInfo.idx = studyIdx
                                _EnterCamstudyResponseLiveData.postValue(it)
                            } else if (it.code() == 403) {
                                _EnterCamstudyResponseLiveData.postValue(it)
                            }
                            Log.d(TAG, "meta : " + it.toString())
                        }
                    }, {
                        Log.d(TAG, "response error, message : ${it.message}")
                    })
            )
        } else {
            Log.d(TAG, "getEnterCamstduyData:: accessToken 값이 없습니다.")
        }
    }
}