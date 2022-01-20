package com.coworkerteam.coworker.ui.search

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coworkerteam.coworker.R
import com.coworkerteam.coworker.data.model.api.StudySearchResponse
import com.coworkerteam.coworker.ui.main.StudyCategoryAdapter
import com.coworkerteam.coworker.utils.ScreenSizeUtils

class StudySearchPagingAdapter :
    PagingDataAdapter<StudySearchResponse.Result.Study, StudySearchPagingAdapter.ViewHolder>(
        differ
    ) {
    lateinit var context: Context

    companion object {
        private val differ =
            object : DiffUtil.ItemCallback<StudySearchResponse.Result.Study>() {
                override fun areItemsTheSame(
                    oldItem: StudySearchResponse.Result.Study,
                    newItem: StudySearchResponse.Result.Study
                ): Boolean {
                    return false
                }

                override fun areContentsTheSame(
                    oldItem: StudySearchResponse.Result.Study,
                    newItem: StudySearchResponse.Result.Study
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_main_other_study, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)

        // 간격 설정
        val layoutParams = holder.itemView.layoutParams
        layoutParams.width = (ScreenSizeUtils().getScreenWidthSize(context as Activity)/2.3).toInt()
        holder.itemView.requestLayout()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val img: ImageView = itemView.findViewById(R.id.item_my_study_img)
        private val leader: TextView = itemView.findViewById(R.id.item_my_study_leader)
        private val pw: ImageView = itemView.findViewById(R.id.txt_item_main_new_study_pw)
        private val studyName: TextView = itemView.findViewById(R.id.item_my_study_txt_name)
        private val studyNum: TextView = itemView.findViewById(R.id.item_my_study_txt_num)
        private val rvCategory: RecyclerView = itemView.findViewById(R.id.item_my_study_rv_category)

        fun bind(item: StudySearchResponse.Result.Study?) {
            Glide.with(context).load(item!!.img).into(img)

            if (!item.isLeader) {
                leader.visibility = View.GONE
            }

            if(!item.isPw){
                pw.visibility = View.GONE
            }

            studyName.text = item.name
            studyNum.text = "참여인원 " + item.userNum.toString() + "/" + item.maxNum.toString()

            var studyCategoryAdapter: StudyCategoryAdapter = StudyCategoryAdapter(context)
            studyCategoryAdapter.datas = item.category.split("|").toMutableList()
            rvCategory.adapter = studyCategoryAdapter
        }
    }
}