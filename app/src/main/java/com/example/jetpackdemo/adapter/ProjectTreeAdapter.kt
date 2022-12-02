package com.example.jetpackdemo.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.jetpackdemo.bean.ProjectTree
import com.example.jetpackdemo.databinding.ItemProjectBinding


private const val TAG = "ProjectFragment"

class ProjectTreeAdapter : ListAdapter<ProjectTree,ProjectTreeAdapter.ProjectViewHolder>(Project_TREE_DIFF_CALLBACK) {
    class ProjectViewHolder(private val binding: ItemProjectBinding)
        : RecyclerView.ViewHolder(binding.root) {

        private lateinit var projectTree: ProjectTree

        fun bind(projectTree: ProjectTree) {
            this.projectTree = projectTree
            binding.run {
                tvProjectTitle.text = projectTree.name
            }
        }

        fun bindItemClick(onClick : (id: Int,name: String) -> Unit) {
            Log.d(TAG,"absoluteAdapterPosition = $absoluteAdapterPosition")
            //设置 itemView 监听
            itemView.setOnClickListener{
                onClick(projectTree.id,projectTree.name)
                Log.d(TAG,"projectTree.id = ${projectTree.id} projectTree.name = ${projectTree.name}")

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        return ProjectViewHolder(
            ItemProjectBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        )
    }

    private lateinit var onClick: (id: Int,name: String) -> Unit
    fun setOnItemClickListener(onClick : (id: Int,name: String) -> Unit){
        this.onClick = onClick
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        Log.d(TAG, "inner onBindViewHolder")
        val projectTree = getItem(position)
        Log.d(TAG, "projectTree = $projectTree")
        holder.bind(projectTree)
        holder.bindItemClick(onClick)
    }

    companion object {
        private val Project_TREE_DIFF_CALLBACK = object : DiffUtil.ItemCallback<ProjectTree>() {
            override fun areItemsTheSame(oldItem: ProjectTree, newItem: ProjectTree): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ProjectTree, newItem: ProjectTree): Boolean =
                oldItem == newItem
        }
    }

}