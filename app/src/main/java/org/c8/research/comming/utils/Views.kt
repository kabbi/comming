package org.c8.research.comming.utils

import android.widget.Button
import org.c8.research.comming.R

fun Button.setProgressIndicator(enabled: Boolean) {
    isEnabled = !enabled
    if (enabled) {
        setTag(R.id.prev_drawable_tag, text)
        setText(R.string.progress_indicator_label)
    } else {
        text = getTag(R.id.prev_drawable_tag) as? String
    }
}

