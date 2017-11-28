package com.taoisym.akmedia.ui.binding

import android.widget.TextView
import com.taoisym.akmedia.std.Emitter
import com.taoisym.akmedia.std.emitter



inline fun <T> emitter(value: T, view: TextView): Emitter<T> {
    return emitter(value, { view.setText(value.toString()) })
}
