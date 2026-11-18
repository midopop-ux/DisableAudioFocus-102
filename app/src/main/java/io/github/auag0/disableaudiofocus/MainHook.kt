package io.github.auag0.disableaudiofocus

import android.media.AudioAttributes
import android.media.AudioManager
import io.github.libxposed.api.XposedInterface.ExceptionMode
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageLoadedParam
import io.github.libxposed.api.XposedModuleInterface.SystemServerStartingParam
import java.lang.reflect.Method

class MainHook : XposedModule() {

    private var isHooked = false

    override fun onModuleLoaded(param: ModuleLoadedParam) {
        super.onModuleLoaded(param)
    }

    override fun onSystemServerStarting(param: SystemServerStartingParam) {
        super.onSystemServerStarting(param)
        initHooks(param.classLoader)
    }

    override fun onPackageLoaded(param: PackageLoadedParam) {
        super.onPackageLoaded(param)
        if (param.packageName == "android" || param.packageName == "system") {
            initHooks(param.defaultClassLoader)
        }
    }

    @Synchronized
    private fun initHooks(classLoader: ClassLoader) {
        if (isHooked) return

        val mediaFocusControlClass = try {
            Class.forName("com.android.server.audio.MediaFocusControl", true, classLoader)
        } catch (_: Throwable) {
            return
        }

        val methods = mediaFocusControlClass.declaredMethods.filter {
            it.name == "requestAudioFocus"
        }

        for (method in methods) {
            hookRequestAudioFocus(method)
        }

        val duckMethods = mediaFocusControlClass.declaredMethods.filter {
            it.name == "duckPlayers" || it.name == "unduckPlayers"
        }
        for (method in duckMethods) {
            hookNoOp(method)
        }

        try {
            val pamClass = Class.forName("com.android.server.audio.PlaybackActivityMonitor", true, classLoader)
            val pamDuckMethods = pamClass.declaredMethods.filter {
                it.name == "duckPlayers" || it.name == "unduckPlayers"
            }
            for (method in pamDuckMethods) {
                hookNoOp(method)
            }
        } catch (_: Throwable) {}

        isHooked = true
    }

    private fun hookRequestAudioFocus(method: Method) {
        try {
            deoptimize(method)
        } catch (_: Throwable) {
            // Some runtime engines don't require or support explicit deoptimization
        }

        try {
            hook(method)
                .setExceptionMode(ExceptionMode.PROTECTIVE)
                .intercept {
                    AudioManager.AUDIOFOCUS_REQUEST_GRANTED
                }
        } catch (_: Throwable) {
            // Hook failure fallback
        }
    }

    private fun hookNoOp(method: Method) {
        try {
            deoptimize(method)
        } catch (_: Throwable) {}

        try {
            hook(method)
                .setExceptionMode(ExceptionMode.PROTECTIVE)
                .intercept {
                    val returnType = method.returnType
                    when {
                        returnType == java.lang.Boolean.TYPE -> false
                        returnType == java.lang.Integer.TYPE -> 0
                        else -> null
                    }
                }
        } catch (_: Throwable) {}
    }
}