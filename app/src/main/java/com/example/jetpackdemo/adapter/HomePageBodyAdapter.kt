package com.example.jetpackdemo.adapter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.jetpackdemo.R
import com.example.jetpackdemo.databinding.ItemArticleBinding
import com.example.jetpackdemo.bean.Article

private const val TAG = "ArticleAdapter"

class HomePageBodyAdapter: PagingDataAdapter<Article, HomePageBodyAdapter.ArticleViewHolder>(ARTICLE_DIFF_CALLBACK){
    class ArticleViewHolder (
        private val binding: ItemArticleBinding
    ) : RecyclerView.ViewHolder(binding.root){

        // 需存储一下 collect 状态，否则每次回传的数据不能保证是正确的
        var collect: Boolean = false

        // 把数据和视图的绑定工作都放在Holder里处理
        fun bind(article: Article) {
            this.collect = article.collect
            Log.d(TAG, "inner bind article.collect = ${article.collect}" +
                    "collect = ${this.collect}")

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

        fun bindImageViewClick(article: Article,onImageViewClick: (id: Int,collect: Boolean) -> Unit) {
            //设置 favoriteImageView 监听
            binding.favoriteImageView.setOnClickListener{
                // bindingAdapterPosition 返回的是Item所在Adapter的位置. absoluteAdapterPosition 返回的是合并后Item所在的位置.
                Log.d(TAG, "inner OnClick Article.id = ${article.id} " +
                        "collect = $collect " +
                        "article.collect = ${article.collect} " +
                        "article.title = ${article.title}"
                )
                // 将 id 和 collect 回传 用于调用 api
                // 不能直接回传 article.collect 这个数据不准
              //  onImageViewClick(article.id,article.collect,bindingAdapterPosition)
                onImageViewClick(article.id,collect)
                // 无网络情况 不修改图标，应弹出无网络提示

                // 设置后立即更新 UI 防止无网络的情况下 点击后无反应
               // if (article.collect){
               /* if (collect){
                    Log.d(TAG,"inner OnClick before setImage ic_favorited")
                    binding.favoriteImageView.setImageResource(R.drawable.ic_favorited)
                }else{
                    Log.d(TAG,"inner OnClick before setImage ic_favorite")
                    binding.favoriteImageView.setImageResource(R.drawable.ic_favorite)
                }*/
            }
        }

        fun updateImageView(collect: Boolean){
            // 存储 collect
            this.collect = collect
            Log.d(TAG,"inner updateImageView collect = $collect")
            if (collect){
                Log.d(TAG,"before setImage ic_favorited")
                binding.favoriteImageView.setImageResource(R.drawable.ic_favorited)
            }else{
                Log.d(TAG,"before setImage ic_favorite")
                binding.favoriteImageView.setImageResource(R.drawable.ic_favorite)
            }
        }

    }


    private lateinit var onClick: (link: String,title: String)  -> Unit
    fun setOnItemClickListener(onClick : (link: String,title: String)  -> Unit){
        this.onClick = onClick
    }

    private lateinit var onImageViewClick: (id: Int,collect: Boolean) -> Unit
    fun setImageViewClickListener(onImageViewClick: (id: Int,collect: Boolean) -> Unit){
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
            val payload: Bundle  = payloads[0] as Bundle
            Log.d(TAG,"payload = $payload")
            holder.updateImageView(payload.getBoolean("collect"))

        }
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        // 通过 getItem 获取数据
        val article = getItem(position)
        if (article != null) {
            Log.d(TAG,"inner onBindViewHolder  article.id = ${article.id} article.collect = ${article.collect}")
            holder.bind(article)
            holder.bindItemClick(article,onClick)
            holder.bindImageViewClick(article,onImageViewClick)
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

    companion object {
        private val ARTICLE_DIFF_CALLBACK = object : DiffUtil.ItemCallback<Article>() {
            override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
                return  oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean{
                Log.d(TAG, " newItem = ${newItem.collect}" + " return ${oldItem == newItem}")
                return oldItem == newItem
            }

            //局部刷新
            override fun getChangePayload(oldItem: Article, newItem: Article): Any? {
                val bundle = Bundle()
                // onBindViewHolder  实现三个参数  payloads   第一个数据为  封装的bundle
                Log.d(TAG,"inner getChangePayload collect =  ${oldItem.collect} " +
                        "collect = ${newItem.collect} ")
                if (oldItem.collect != newItem.collect){
                    bundle.putBoolean("collect",newItem.collect)
                }
                return bundle
            }
        }
    }

}