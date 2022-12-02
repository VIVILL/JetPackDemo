package com.example.jetpackdemo.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.jetpackdemo.R
import com.example.jetpackdemo.bean.Article
import com.example.jetpackdemo.databinding.ItemArticleBinding

private const val TAG = "SquareAdapter"

class SquareAdapter: PagingDataAdapter<Article, SquareAdapter.SquareViewHolder>(Square_DIFF_CALLBACK){
    class SquareViewHolder (
        private val binding: ItemArticleBinding,
        ) : RecyclerView.ViewHolder(binding.root){
        // 需存储一下 collect 状态，否则每次回传的数据不能保证是正确的
        var collect: Boolean = false

        // 把数据和视图的绑定工作都放在Holder里处理
        fun bind(article: Article) {
            this.collect = article.collect

            Log.d(TAG,"inner bind article.collect = ${article.collect}")
            with(binding){
                articleTitleTextView.text = article.title
                if (article.collect){
                    favoriteImageView.setImageResource(R.drawable.ic_favorited)
                }else{
                    favoriteImageView.setImageResource(R.drawable.ic_favorite)
                }
                if(article.author.isEmpty()){
                    authorNameTextView.text = article.shareUser
                }else{
                    authorNameTextView.text = article.author
                }
                dateTextView.text = article.niceDate
            }
        }

        fun bindItemClick(article: Article,onClick: (link: String,title: String) -> Unit){
            //设置 itemView 监听
            itemView.setOnClickListener{
                onClick(article.link,article.title)
                Log.d(TAG,"article.title = ${article.title}")
            }
        }

        fun bindImageViewClick(article: Article,onImageViewClick: (id: Int,collect: Boolean,position: Int) -> Unit) {
            //设置 favoriteImageView 监听
            binding.favoriteImageView.setOnClickListener{
                Log.d(TAG, "inner OnClick Article.id = ${article.id} " +
                        "collect = $collect " +
                        "article.collect = ${article.collect} " +
                        "article.title = ${article.title}"
                )
                // 将 id 和 collect 回传 用于调用 api
                // onImageViewClick(article.id, article.collect, absoluteAdapterPosition)
                onImageViewClick(article.id, collect, absoluteAdapterPosition)

            }
        }

        fun updateImageView(collect: Boolean){
            // 存储
            this.collect = collect
            Log.d(TAG,"inner updateImageView collect = $collect")
            if (collect){
                binding.favoriteImageView.setImageResource(R.drawable.ic_favorited)
            }else{
                binding.favoriteImageView.setImageResource(R.drawable.ic_favorite)
            }
        }

    }

    private lateinit var onClick: (link: String,title: String)  -> Unit
    fun setOnItemClickListener(onClick : (link: String,title: String)  -> Unit){
        this.onClick = onClick
    }

    private lateinit var onImageViewClick: (id: Int,collect: Boolean,position: Int) -> Unit
    fun setImageViewClickListener(onImageViewClick: (id: Int,collect: Boolean,position: Int) -> Unit){
        this.onImageViewClick = onImageViewClick
    }


    override fun onBindViewHolder(holder: SquareViewHolder,position: Int, payloads: List<Any>) {
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

    override fun onBindViewHolder(holder: SquareViewHolder, position: Int) {
        Log.d(TAG,"inner onBindViewHolder")
        // 通过 getItem 获取数据
        val square = getItem(position)
        if (square != null) {
            Log.d(TAG,"inner bind square.collect = ${square.collect}")
            holder.bind(square)
            holder.bindItemClick(square,onClick)
            holder.bindImageViewClick(square,onImageViewClick)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SquareViewHolder {
        Log.d(TAG,"inner onCreateViewHolder")
        return SquareViewHolder(
            ItemArticleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        )
    }

    companion object {
        private val Square_DIFF_CALLBACK = object : DiffUtil.ItemCallback<Article>() {
            override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
                Log.d(TAG, " newItem = ${newItem.collect}" + " return ${oldItem == newItem}")
                return oldItem == newItem
            }
        }
    }
}