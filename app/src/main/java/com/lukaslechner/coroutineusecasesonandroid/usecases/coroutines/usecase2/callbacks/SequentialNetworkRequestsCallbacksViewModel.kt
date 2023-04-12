package com.lukaslechner.coroutineusecasesonandroid.usecases.coroutines.usecase2.callbacks

import com.lukaslechner.coroutineusecasesonandroid.base.BaseViewModel
import com.lukaslechner.coroutineusecasesonandroid.mock.AndroidVersion
import com.lukaslechner.coroutineusecasesonandroid.mock.VersionFeatures
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SequentialNetworkRequestsCallbacksViewModel(
    private val mockApi: CallbackMockApi = mockApi()
) : BaseViewModel<UiState>() {

    private var getAndroidVersionCall: Call<List<AndroidVersion>>? = null
    private var getAndroidFeatureCall: Call<VersionFeatures>? = null

    fun perform2SequentialNetworkRequest() {
        uiState.value = UiState.Loading
        getAndroidVersionCall = mockApi.getRecentAndroidVersions()
        getAndroidVersionCall!!.enqueue(object : Callback<List<AndroidVersion>> {
            override fun onResponse(
                call: Call<List<AndroidVersion>>,
                response: Response<List<AndroidVersion>>
            ) {
                if (response.isSuccessful) {
                    val mostRecentVersion = response.body()!!.last()
                    getAndroidFeatureCall =
                        mockApi.getAndroidVersionFeatures(mostRecentVersion.apiLevel)

                    getAndroidFeatureCall!!.enqueue(object : Callback<VersionFeatures> {
                        override fun onResponse(
                            call: Call<VersionFeatures>,
                            response: Response<VersionFeatures>
                        ) {
                            if (response.isSuccessful) {
                                val featureOfMostRecentAndroidVersion = response.body()!!
                                uiState.value = UiState.Success(featureOfMostRecentAndroidVersion)
                            } else {
                                uiState.value = UiState.Error("Network Request Failed")
                            }
                        }

                        override fun onFailure(call: Call<VersionFeatures>, t: Throwable) {
                            uiState.value = UiState.Error("Something Went Wrong")
                        }

                    })
                } else {
                    uiState.value = UiState.Error("Network Request Failed")
                }
            }

            override fun onFailure(call: Call<List<AndroidVersion>>, t: Throwable) {
                uiState.value = UiState.Error("Something Went Wrong")
            }

        })
    }

    override fun onCleared() {
        super.onCleared()
        getAndroidVersionCall?.cancel()
        getAndroidFeatureCall?.cancel()
    }
}