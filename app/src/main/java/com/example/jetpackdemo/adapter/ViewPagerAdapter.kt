package com.example.jetpackdemo.adapter

import android.util.Log
import android.view.MotionEvent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.jetpackdemo.fragment.home.viewpager.HomePageArticleFragment
import com.example.jetpackdemo.fragment.home.viewpager.DailyQuestionFragment
import com.example.jetpackdemo.fragment.home.viewpager.SquareFragment
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

private const val TAG = "ViewPagerAdapter"
class ViewPagerAdapter @AssistedInject constructor(
    @Assisted("fragmentStringList") private val fragmentStringList: List<String>,
    @Assisted("fm") fm: FragmentManager,
    @Assisted("lifecycle") lifecycle: Lifecycle
) : FragmentStateAdapter(fm, lifecycle) {

    override fun getItemCount(): Int {
        return fragmentStringList.size
    }

    override fun createFragment(position: Int): Fragment {
        return when (fragmentStringList[position]) {
            "HomePageArticleFragment" -> HomePageArticleFragment.newInstance("","")
            "DailyQuestionFragment" -> DailyQuestionFragment.newInstance("","")
            else -> SquareFragment.newInstance("","")
        }
    }
}