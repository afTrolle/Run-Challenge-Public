package app.runchallenge.view.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import app.runchallenge.R

class PlayerReadyView : FrameLayout {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

    }

    init {
        val view = View.inflate(context, R.layout.ready_view, null)
        addView(view)
    }


}