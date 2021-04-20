package tw.edu.pu.csim.tcyang.handshape

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import org.tensorflow.lite.support.image.TensorImage
import tw.edu.pu.csim.tcyang.handshape.ml.Shapes

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn.setOnClickListener(object:View.OnClickListener{
            override fun onClick(p0: View?) {
                handv.path.reset()
                handv.invalidate()
            }
        })

        handv.setOnTouchListener(object:View.OnTouchListener{
            override fun onTouch(p0: View?, event: MotionEvent): Boolean {
                var xPos = event.getX()
                var yPos = event.getY()
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> handv.path.moveTo(xPos, yPos)
                    MotionEvent.ACTION_MOVE -> handv.path.lineTo(xPos, yPos)

                    MotionEvent.ACTION_UP -> {
                        //將handv轉成Bitmap
                        val b = Bitmap.createBitmap(handv.measuredWidth, handv.measuredHeight,
                                Bitmap.Config.ARGB_8888)
                        val c = Canvas(b)
                        handv.draw(c)
                        classifyDrawing(b)
                    }
                }
                handv.invalidate()
                return true
            }
        })
    }


    fun classifyDrawing(bitmap : Bitmap) {
        val model = Shapes.newInstance(this)

        // Creates inputs for reference.
        val image = TensorImage.fromBitmap(bitmap)

        // Runs model inference and gets result.
        /*
        val outputs = model.process(image)
        val probability = outputs.probabilityAsCategoryList
         */
        val outputs = model.process(image)
                .probabilityAsCategoryList.apply {
                    sortByDescending { it.score } // 排序，高匹配率優先
                }.take(1)  //取最高的1個

        var Result:String = ""
        when (outputs[0].label) {
            "circle" -> Result = "圓形"
            "square" -> Result = "方形"
            "star" -> Result = "星形"
            "triangle" -> Result = "三角形"
        }
        Result += ": " + String.format("%.1f%%", outputs[0].score * 100.0f)

        // Releases model resources if no longer used.
        model.close()
        Toast.makeText(this, Result, Toast.LENGTH_SHORT).show()
    }


}