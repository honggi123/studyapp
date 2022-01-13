package com.coworkerteam.coworker.ui.search

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.model.api.StudySearchResponse
import com.coworkerteam.coworker.data.model.other.SearchStudy
import com.coworkerteam.coworker.databinding.FragmentGroupStudySerarchBinding
import com.coworkerteam.coworker.ui.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [GroupStudySerarchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GroupStudySerarchFragment :
    BaseFragment<FragmentGroupStudySerarchBinding, StudySearchViewModel>() {
    var TAG = "GroupStudySerarchFragment"

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var studySearchResponse: StudySearchResponse

    override val layoutResourceID: Int
        get() = R.layout.fragment_group_study_serarch
    override val viewModel: StudySearchViewModel by viewModel()

    lateinit var pagingStudySearchAdapter: StudySearchPagingAdapter

    override fun initStartView() {
        pagingStudySearchAdapter = StudySearchPagingAdapter()
        val rv_Search = view?.findViewById<RecyclerView>(R.id.fragment_group_study_rv)
        rv_Search?.adapter = pagingStudySearchAdapter

        viewModel.getStudySearchData("group")
    }

    override fun initDataBinding() {
        StudySearchActivity.StudySearchLiveData.observe(this, androidx.lifecycle.Observer {
            //검색결과를 성공적으로 반환
            Log.d(TAG,"검색결과를 성공적으로 반환")
            searchEvent(it)
        })
        viewModel.StudySearchPagingData.observe(this,androidx.lifecycle.Observer {
            pagingStudySearchAdapter.submitData(lifecycle,it)
        })
    }

    override fun initAfterBinding() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate")
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")
        return inflater.inflate(R.layout.fragment_group_study_serarch, container, false)
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
    }

    fun newInstant(): GroupStudySerarchFragment {
        Log.d(TAG, "newInstant")
        val args = Bundle()
        val frag = GroupStudySerarchFragment()
        frag.arguments = args
        return frag
    }

    fun searchEvent(studyInfo : SearchStudy){
        pagingStudySearchAdapter.refresh()
    }

}