package com.example.jetpackdemo.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.jetpackdemo.bean.ProjectContent
import com.example.jetpackdemo.databinding.ItemProjectDetailBinding

private const val TAG = "ProjectDetailAdapter"

class ProjectDetailAdapter: PagingDataAdapter<ProjectContent, ProjectDetailAdapter.ProjectDetailViewHolder>(PROJECT_CONTENT_DIFF_CALLBACK){
    class ProjectDetailViewHolder (
        private val binding: ItemProjectDetailBinding
    ) : RecyclerView.ViewHolder(binding.root){
        private lateinit var projectContent: ProjectContent

        // 把数据和视图的绑定工作都放在Holder里处理
        fun bind(projectContent: ProjectContent) {
            this.projectContent = projectContent
            with(binding){
                tvProjectName.text = projectContent.title
                tvSubName.text = projectContent.desc
                Glide.with(itemView)
                    .load(projectContent.envelopePic)
                    .into(ivProjectIcon)
            }

        }

        fun bindItemClick(onClick : (link: String,title: String) -> Unit) {
            Log.d(TAG,"absoluteAdapterPosition = $absoluteAdapterPosition")
            //设置 itemView 监听
            itemView.setOnClickListener{
                onClick(this.projectContent.link,this.projectContent.title)
                Log.d(TAG,"projectTree.title = ${this.projectContent.title}")
            }
        }
    }

    companion object {
        private val PROJECT_CONTENT_DIFF_CALLBACK = object : DiffUtil.ItemCallback<ProjectContent>() {
            override fun areItemsTheSame(oldItem: ProjectContent, newItem: ProjectContent): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ProjectContent, newItem: ProjectContent): Boolean =
                oldItem == newItem
        }
    }

    private lateinit var onClick: (link: String,title: String)  -> Unit
    fun setOnItemClickListener(onClick : (link: String,title: String)  -> Unit){
        this.onClick = onClick
    }

    override fun onBindViewHolder(holder: ProjectDetailViewHolder, position: Int) {
        // 通过 getItem 获取数据
        val projectContent = getItem(position)
        if (projectContent != null) {
            holder.bind(projectContent)
            holder.bindItemClick(onClick)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectDetailViewHolder {
        return ProjectDetailViewHolder(
            ItemProjectDetailBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        )
    }

}