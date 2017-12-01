import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.taoisym.akmedia.R
import com.taoisym.akmedia.render.FilterRender
import com.taoisym.akmedia.render.TextureRender

class Filters {
    data class Shader(val fs: String, val res: Int)
    companion object {
        val buildin = arrayOf(
                Shader("fs_no_oes.glsl", 0),
                Shader("amaro.glsl", R.raw.filter2),
                Shader("earlybird.glsl", R.raw.filter2),
                Shader("hefe.glsl", R.raw.hefe),
                Shader("hudson.glsl", R.raw.hudson),
                Shader("mayfair.glsl", R.raw.filter2),
                Shader("rise.glsl", R.raw.filter2),
                Shader("toaster.glsl", R.raw.toaster),
                Shader("willow.glsl", 0),
                Shader("xpro.glsl", R.raw.filter2),


                Shader("amaro.glsl", R.raw.toaster),
                Shader("earlybird.glsl", R.raw.toaster),
                Shader("hefe.glsl", R.raw.toaster),
                Shader("hudson.glsl", R.raw.toaster),
                Shader("mayfair.glsl", R.raw.toaster),
                Shader("rise.glsl", R.raw.toaster),
                Shader("xpro.glsl", R.raw.toaster)
        )
        val size = buildin.size
        fun get(ctx: Context, i: Int): TextureRender {
            val shader = buildin.get(i)
            val vs = String(ctx.assets.open("shader/vs_shader.glsl").readBytes())
            val fs = String(ctx.assets.open("shader/" + shader.fs).readBytes())
            if (shader.res == 0) {
                return TextureRender(vs, fs)
            } else {
                val bmp: Bitmap? = null
                try {
                    val bmp = BitmapFactory.decodeResource(ctx.resources, shader.res)
                    return FilterRender(vs, fs, bmp)
                } finally {
                    bmp?.recycle()
                }
            }
        }
    }
}