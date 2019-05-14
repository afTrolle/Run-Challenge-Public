package app.runchallenge.view.view

import android.content.Context
import android.util.AttributeSet
import app.runchallenge.R
import kotlinx.android.synthetic.main.count_down_view.view.*

class CountDownView : BaseView {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context, attrs, defStyleAttr, defStyleRes)



    override val viewLayout: Int get() = R.layout.count_down_view


    //assuming count down start 60

    fun setTime(currentTime : Double, totalTime : Double)  {

       val remainingTime =  totalTime - currentTime
        val remainingPercent =  totalTime / remainingTime

        guideline.setGuidelinePercent(remainingPercent.toFloat())
        count_down_text_clock.text = currentTime.toString()


    }

}