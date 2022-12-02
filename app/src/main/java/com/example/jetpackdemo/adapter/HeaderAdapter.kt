package com.example.jetpackdemo.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.jetpackdemo.databinding.ItemParentHeaderBinding
import com.example.jetpackdemo.util.MyPagerHelper

private const val TAG = "HeaderAdapter"

class HeaderAdapter(private var adapter: HeaderItemAdapter) :
    RecyclerView.Adapter<HeaderAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemParentHeaderBinding) : RecyclerView.ViewHolder(binding.root){

        private lateinit var onViewAttachedListener: () -> Unit
        private lateinit var onViewDetachedListener: ()  -> Unit
        private lateinit var onTouchEventListener: (ev: MotionEvent)  -> Unit

        fun setOnViewAttachedListener(onViewAttachedListener: () -> Unit) {
            this.onViewAttachedListener = onViewAttachedListener
        }

        fun setOnViewDetachedListener(onViewDetachedListener : ()  -> Unit){
            this.onViewDetachedListener = onViewDetachedListener
        }

        fun setTouchEventListener(onTouchEventListener : (ev: MotionEvent)  -> Unit){
            this.onTouchEventListener = onTouchEventListener
        }

        init {
            itemView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewDetachedFromWindow(v: View?) {
                    //   itemView.removeOnAttachStateChangeListener(this)
                    Log.d(TAG,"inner onViewAttachedToWindow position = ${binding.viewPager.bannerPosition}")
                    onViewDetachedListener()
                }

                override fun onViewAttachedToWindow(v: View?) {
                    Log.d(TAG,"inner onViewAttachedToWindow position = ${binding.viewPager.bannerPosition}")
                    binding.viewPager.setPosition(binding.viewPager.bannerPosition)
                    onViewAttachedListener()
                }
            })
        }

        fun bind(adapter: HeaderItemAdapter) {
            Log.d(TAG,"inner bind")
            binding.viewPager.setAdapter(adapter)
            binding.viewPager.createCircle(adapter.itemCount - 3)
            // 设置 position
            binding.viewPager.setPosition(binding.viewPager.bannerPosition)

            binding.viewPager.setTouchEventListener(onTouchEventListener)
        }

    }

    lateinit var binding: ItemParentHeaderBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        binding = ItemParentHeaderBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    private lateinit var onViewAttachedListener: () -> Unit
    private lateinit var onViewDetachedListener: ()  -> Unit
    private lateinit var onTouchEventListener: (ev: MotionEvent)  -> Unit

    fun setOnViewAttachedListener(onViewAttachedListener: () -> Unit) {
        this.onViewAttachedListener = onViewAttachedListener
    }

    fun setOnViewDetachedListener(onViewDetachedListener : ()  -> Unit){
        this.onViewDetachedListener = onViewDetachedListener
    }

    fun setTouchEventListener(onTouchEventListener : (ev: MotionEvent)  -> Unit){
        this.onTouchEventListener = onTouchEventListener
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setOnViewAttachedListener(onViewAttachedListener)
        holder.setOnViewDetachedListener(onViewDetachedListener)
        holder.setTouchEventListener(onTouchEventListener)

        holder.bind(adapter)
    }

    override fun getItemCount(): Int = 1

    // 通过设置动画，实现 自动滑动时 平滑滚动
    fun setCurrentItem(){
        if (::binding.isInitialized){
            MyPagerHelper.setCurrentItem(
                binding.viewPager.getViewPager2(),
                binding.viewPager.getViewPager2().currentItem + 1,
                300
            )
        }
    }

/*
    class ViewHolder(private val binding: ItemHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(adapter: HeaderItemAdapter) {
            val context = binding.root.context
            binding.headerList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.headerList.adapter = adapter
        }
    }

    private lateinit var binding: ItemHeaderBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        binding = ItemHeaderBinding.inflate(layoutInflater, parent, false)
        isAbleScroll = true
        // 确保一次只能滑动一个数据，停止的时候图片的位置正确
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(binding.headerList)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(adapter)
    }

    override fun getItemCount(): Int = 1

    var isAbleScroll: Boolean = false
    fun smoothScrollToPosition(position: Int){
        if (isAbleScroll){
            binding.headerList.smoothScrollToPosition(position)
        }
    }*/
}