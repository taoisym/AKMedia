import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.taoisym.akmedia.R
import com.taoisym.akmedia.render.FilterRender
import com.taoisym.akmedia.render.TextureRender

class Filters{
    data class Shader(val fs:String,val res:Int)
    companion object {
        val buildin = arrayOf(
                Shader("amaro.glsl", R.raw.original_ps),
                Shader("earlybird.glsl", R.raw.original_ps),
                Shader("hefe.glsl", R.raw.hefe),
                Shader("hudson.glsl", R.raw.hudson),
                Shader("mayfair.glsl", R.raw.original_ps),
                Shader("rise.glsl", R.raw.original_ps),
                Shader("toaster.glsl", R.raw.toaster),
                Shader("willow.glsl", R.raw.original_ps),
                Shader("xpro.glsl", R.raw.original_ps),
                Shader("fs_no_oes.glsl", 0)
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
                    val bmp = BitmapFactory.decodeResource(ctx.resources, R.raw.toaster)
                    return FilterRender(vs, fs, bmp)
                } finally {
                    bmp?.recycle()
                }
            }
        }
    }
}