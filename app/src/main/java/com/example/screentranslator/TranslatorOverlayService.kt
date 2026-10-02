package com.example.screentranslator

import android.app.*
import android.content.*
import android.graphics.Color
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.*
import android.view.*
import android.widget.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

class TranslatorOverlayService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var wm: WindowManager
    private var bubble: View? = null
    private var panel: LinearLayout? = null
    private var projection: MediaProjection? = null
    private var virtual: VirtualDisplay? = null
    private var reader: ImageReader? = null
    private val cache = ConcurrentHashMap<String,String>()
    private val engine: TranslationEngine = MlKitTranslationEngine()
    private var target = "fa"
    private var busy = false

    override fun onCreate() { super.onCreate(); wm = getSystemService(WINDOW_SERVICE) as WindowManager; startNotification(); addBubble() }
    override fun onStartCommand(i: Intent?, flags: Int, startId: Int): Int {
        val result = i?.getIntExtra("resultCode", 0) ?: 0; val data = i?.getParcelableExtra<Intent>("data")
        if (data != null && projection == null) setupProjection(result, data)
        return START_STICKY
    }
    private fun startNotification() { val channel = "translator"; val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager; nm.createNotificationChannel(NotificationChannel(channel,"Screen Translator",NotificationManager.IMPORTANCE_LOW)); startForeground(7, Notification.Builder(this,channel).setContentTitle("Screen Translator").setContentText("Floating translator is active").setSmallIcon(android.R.drawable.ic_menu_search).build()) }
    private fun lp(type: Int) = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, type, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, PixelFormat.TRANSLUCENT)
    private fun addBubble() { val b = TextView(this).apply { text="文"; textSize=20f; gravity=Gravity.CENTER; setTextColor(Color.WHITE); setBackgroundColor(Color.rgb(91,92,226)); setOnClickListener { togglePanel() } }; bubble=b; val p=lp(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY).apply { gravity=Gravity.RIGHT or Gravity.CENTER_VERTICAL; width=64; height=64 }; wm.addView(b,p) }
    private fun togglePanel() { if(panel==null) showPanel() else hidePanel() }
    private fun showPanel() { val root=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(28,28,20,28); setBackgroundColor(Color.WHITE) }; val title=TextView(this).apply { text="Screen Translator"; textSize=20f; setTextColor(Color.DKGRAY) }; val controls=LinearLayout(this); val scan=Button(this).apply { text="Translate screen"; setOnClickListener { captureAndTranslate() } }; val clear=Button(this).apply { text="Clear"; setOnClickListener { root.findViewWithTag<LinearLayout>("results")?.removeAllViews() } }; controls.addView(scan); controls.addView(clear); root.addView(title); root.addView(controls); val results=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; tag="results" }; val scroll=ScrollView(this).apply { addView(results) }; root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f)); panel=root; val p=lp(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY).apply { gravity=Gravity.RIGHT or Gravity.CENTER_VERTICAL; width=(resources.displayMetrics.widthPixels*0.72).toInt(); height=(resources.displayMetrics.heightPixels*0.82).toInt() }; wm.addView(root,p) }
    private fun hidePanel(){ panel?.let { wm.removeView(it) }; panel=null }
    private fun setupProjection(code:Int,data:Intent){ val mgr=getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager; projection=mgr.getMediaProjection(code,data); val dm=resources.displayMetrics; reader=ImageReader.newInstance(dm.widthPixels,dm.heightPixels,android.graphics.PixelFormat.RGBA_8888,2); virtual=projection!!.createVirtualDisplay("translator",dm.widthPixels,dm.heightPixels,dm.densityDpi,DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,reader!!.surface,null,null); }
    private fun captureAndTranslate(){ if(busy || reader==null) return; busy=true; val r=reader!!; val img=r.acquireLatestImage() ?: run { busy=false; return }; scope.launch(Dispatchers.Default) { val plane=img.planes[0]; val buf=plane.buffer; val bitmap=android.graphics.Bitmap.createBitmap(img.width,img.height,android.graphics.Bitmap.Config.ARGB_8888); bitmap.copyPixelsFromBuffer(buf); img.close(); val input=InputImage.fromBitmap(bitmap,0); val recognizer=TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS); recognizer.process(input).addOnSuccessListener { result -> scope.launch { renderTranslations(result.text) } }.addOnFailureListener { busy=false } } }
    private suspend fun renderTranslations(text:String){ val out=panel?.findViewWithTag<LinearLayout>("results") ?: run { busy=false; return }; withContext(Dispatchers.Main){ out.removeAllViews() }; val lines=text.lines().filter{it.isNotBlank()}.take(40); for(line in lines){ val translated=cache[line] ?: try { engine.translate(line,"en",target).also{cache[line]=it} } catch(_:Throwable){ "[Translation unavailable] $line" }; withContext(Dispatchers.Main){ val card=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(16,12,16,12);setBackgroundColor(Color.rgb(245,245,247)); addView(TextView(this@TranslatorOverlayService).apply{text=line;textSize=14f;setTextColor(Color.GRAY)});addView(TextView(this@TranslatorOverlayService).apply{text=translated;textSize=17f;setTextColor(Color.DKGRAY)})}; out.addView(card,LinearLayout.LayoutParams(-1,-2).apply{setMargins(0,0,0,12)}) } }; busy=false }
    override fun onDestroy(){ scope.cancel(); virtual?.release(); reader?.close(); projection?.stop(); bubble?.let{wm.removeView(it)}; panel?.let{wm.removeView(it)}; super.onDestroy() }
    override fun onBind(intent:Intent?)=null
}
