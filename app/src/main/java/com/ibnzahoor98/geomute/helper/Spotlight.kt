package com.ibnzahoor98.geomute.helper

import android.content.Context
import android.graphics.Color
import androidx.core.content.res.ResourcesCompat
import com.ibnzahoor98.geomute.R
import com.wooplr.spotlight.SpotlightConfig

class Spotlight {

    companion object{

        fun init(context: Context):SpotlightConfig{
            val config = SpotlightConfig()
            config.introAnimationDuration = 200
            config.isRevealAnimationEnabled = true
            config.isPerformClick = true
            config.fadingTextDuration = 200
            config.headingTvColor = (Color.parseColor("#ffffff"))
            config.headingTvSize = (32)
            config.subHeadingTvColor = (Color.parseColor("#ffffff"))
            config.subHeadingTvSize = (16)
            config.maskColor = (Color.parseColor("#dc000000"))
            config.lineAnimationDuration = (200)
            config.lineAndArcColor = (Color.parseColor("#ffffff"))
            config.isDismissOnTouch = (true)
            config.isDismissOnBackpress = (true)
            config.setTypeface(ResourcesCompat.getFont(context, R.font.u_semi_bold))
            return config
        }

    }
}