package com.example.jetpackdemo.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.jetpackdemo.R
import com.example.jetpackdemo.bean.Article
import com.example.jetpackdemo.bean.Collect
import com.example.jetpackdemo.databinding.ItemArticleBinding

private const val TAG = "CollectionAdapter"

class CollectionAdapter: PagingDataAdapter<Collect, CollectionAdapter.ArticleViewHolder>(ARTICLE_DIFF_CALLBACK){

    class ArticleViewHolder (
        private val binding: ItemArticleBinding
    ) : RecyclerView.ViewHolder(binding.root){

        // 把数据和视图的绑定工作都放在Holder里处理
        fun bind(collect: Collect) {
            Log.d(TAG,"inner bind collect.id = ${collect.id}")
            with(binding){
                articleTitleTextView.text = collect.title
                favoriteImageView.setImageResource(R.drawable.ic_favorited)
                authorNameTextView.text = collect.author
                dateTextView.text = collect.niceDate
            }

        }

        fun bindItemClick(collect: Collect,onClick: (link: String,title: String) -> Unit){
            //设置 itemView 监听
            itemView.setOnClickListener{
                onClick(collect.link,collect.title)
                Log.d(TAG,"article.title = ${collect.title}")
            }
        }

        fun bindImageViewClick(collect: Collect,onImageViewClick: (id: Int,originId: Int,position: Int) -> Unit) {
            //设置 favoriteImageView 监听
            binding.favoriteImageView.setOnClickListener{
                // 将 id 和 collect 回传 用于调用 api
                onImageViewClick(collect.id,collect.originId,absoluteAdapterPosition)
                Log.d(TAG,"inner OnClick Article.id = ${collect.id} absoluteAdapterPosition = $absoluteAdapterPosition")
                // 设置后立即更新 UI 防止无网络的情况下 点击后无反应
                Log.d(TAG,"inner OnClick before setImage ic_favorite")
                binding.favoriteImageView.setImageResource(R.drawable.ic_favorite)

            }
        }

        fun updateImageView(collect: Boolean){
            if (collect){
                binding.favoriteImageView.setImageResource(R.drawable.ic_favorited)
            }else{
                binding.favoriteImageView.setImageResource(R.drawable.ic_favorite)
            }
        }

    }

    companion object {
        private val ARTICLE_DIFF_CALLBACK = object : DiffUtil.ItemCallback<Collect>() {
            override fun areItemsTheSame(oldItem: Collect, newItem: Collect): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Collect, newItem: Collect): Boolean {
                return oldItem == newItem
            }
        }
    }

    private lateinit var onClick: (link: String,title: String)  -> Unit
    fun setOnItemClickListener(onClick : (link: String,title: String)  -> Unit){
        this.onClick = onClick
    }

    private lateinit var onImageViewClick: (id: Int,originId: Int,position: Int) -> Unit
    fun setImageViewClickListener(onImageViewClick: (id: Int,originId: Int,position: Int) -> Unit){
        this.onImageViewClick = onImageViewClick
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int, payloads: List<Any>) {
        Log.d(TAG,"inner onBindViewHolder payloads")
        if (payloads.isEmpty()) {
            Log.d(TAG,"payloads isEmpty")
            // payloads为空，说明是更新整个ViewHolder
            onBindViewHolder(holder, position);
        } else {
            // payloads不为空，这只更新需要更新的View即可。
            val payload: Boolean = payloads[0] as Boolean
            Log.d(TAG,"payload = $payload")
            holder.updateImageView(payload)

        }
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        // 通过 getItem 获取数据
        val collect = getItem(position)
        if (collect != null) {
            Log.d(TAG,"inner onBindViewHolder  collect.id = ${collect.id}")
            holder.bind(collect)
            holder.bindItemClick(collect,onClick)
            holder.bindImageViewClick(collect,onImageViewClick)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        return ArticleViewHolder(
            ItemArticleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        )
    }

}