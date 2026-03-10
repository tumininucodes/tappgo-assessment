package com.kotlinspinwheel

import android.content.Context
import android.widget.FrameLayout
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp

class SpinWheelViewManager(
    private val reactContext: Context
) : SimpleViewManager<SpinWheelContainer>() {

    override fun getName(): String {
        return "SpinWheelView"
    }

    override fun createViewInstance(reactContext: ThemedReactContext): SpinWheelContainer {
        return SpinWheelContainer(reactContext)
    }

    @ReactProp(name = "configUrl")
    fun setConfigUrl(view: SpinWheelContainer, configUrl: String?) {
        println("SpinWheel: Setting config URL: $configUrl")
        view.setConfigUrl(configUrl)
    }
}

// A wrapper container that holds our Custom Canvas View
class SpinWheelContainer(context: Context) : FrameLayout(context) {
    
    private val spinWheelView = SpinWheelView(context)

    init {
        addView(spinWheelView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    fun setConfigUrl(url: String?) {
        if (!url.isNullOrEmpty()) {
            spinWheelView.loadConfig(url)
        }
    }
}
