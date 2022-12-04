package com.example.jetpackdemo.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.jetpackdemo.fragment.home.viewpager.DailyQuestionFragment
import com.example.jetpackdemo.fragment.home.viewpager.HomePageFragment
import com.example.jetpackdemo.fragment.home.viewpager.SquareFragment
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

private const val TAG = "ViewPagerAdapter"
class HomePageViewPagerAdapter @AssistedInject constructor(
    @Assisted("fragmentStringList") private val fragmentStringList: List<String>,
    @Assisted("fm") fm: FragmentManager,
    @Assisted("lifecycle") lifecycle: Lifecycle
) : FragmentStateAdapter(fm, lifecycle) {

    override fun getItemCount(): Int {
        return fragmentStringList.size
    }

    override fun createFragment(position: Int): Fragment {
        return when (fragmentStringList[position]) {
            "HomePageArticleFragment" -> HomePageFragment.newInstance()
            "DailyQuestionFragment" -> DailyQuestionFragment.newInstance()
            else -> SquareFragment.newInstance()
        }
    }
}