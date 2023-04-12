package ke.co.osl.kisiifarmermappingapp

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import ke.co.osl.kisiifarmermappingapp.fragments.CollectData
import ke.co.osl.kisiifarmermappingapp.fragments.UpdateData

class MyAdapter(private val myContext: Context, fm: FragmentManager, internal var totalTabs: Int) : FragmentPagerAdapter(fm) {

    // this is for fragment tabs
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> CollectData()
            1 -> UpdateData()
            else -> getItem(position)
//            else -> throw IllegalStateException("position $position is invalid for this viewpager")
        }
    }

    // this counts total number of tabs
    override fun getCount(): Int {
        return totalTabs
    }
}