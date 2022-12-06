package com.example.jetpackdemo.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.jetpackdemo.bean.Banner
import com.example.jetpackdemo.databinding.ItemImageBinding

private const val TAG = "HomePageHeaderItemAdapter"
class HomePageHeaderItemAdapter(var bannerList: List<Banner>, private val onClick: (link: String,title: String)  -> Unit):
    RecyclerView.Adapter<HomePageHeaderItemAdapter.BannerViewHolder> () {
    class BannerViewHolder(private val binding: ItemImageBinding)
        : RecyclerView.ViewHolder(binding.root){

        fun bind(banner: Banner, onClick: (link: String,title: String)  -> Unit) {
            Glide.with(itemView)
                .load(banner.imagePath)
                .into(binding.itemImage)
            binding.itemImage.setOnClickListener {
                Log.d(TAG,"inner setOnClickListener")
                onClick(banner.url,banner.title)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        return BannerViewHolder(
            ItemImageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        )
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        // 通过 getItem 获取数据
        if (bannerList.isEmpty()){
            return
        }
        val banner = bannerList[position]
        holder.bind(banner,onClick)

    }


    override fun getItemCount(): Int {
        return bannerList.size

    }
}
