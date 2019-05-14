package app.runchallenge.view.view

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.ColorRes
import app.runchallenge.R
import app.runchallenge.model.data.game.Player
import kotlinx.android.synthetic.main.player_ready_card_view.view.*


class PlayerReadyCard : FrameLayout {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    val defaultCardColor: ColorStateList

    init {
        val view = View.inflate(context, R.layout.player_ready_card_view, null)
        addView(view)

        defaultCardColor = ready_card_view.cardBackgroundColor

    }


    fun setPlayer(player: Player) {

        if (player.isDisconnected) {
            val notConnectedColor = context.getColor(R.color.red_disconnected)
            ready_card_view.setCardBackgroundColor(notConnectedColor)
        } else {
            ready_card_view.setCardBackgroundColor(defaultCardColor)
        }

        if (player.hasLocationAvailable) {
            ready_card_gps.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_gps_fixed_24px, 0, 0, 0)
            ready_card_gps.compoundDrawableTintList = getColorStateList(R.color.success)
        } else {
            ready_card_gps.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_gps_not_fixed_24px, 0, 0, 0)
            ready_card_gps.compoundDrawableTintList = getColorStateList(R.color.onSurfaceColor)
        }

        ready_card_ready_check.isChecked = player.isReady
    }
}

fun View.getColor(@ColorRes id: Int): Int {
    return context.getColor(id)
}


fun View.getColorStateList(@ColorRes id: Int): ColorStateList {
    return context.getColorStateList(id)
}