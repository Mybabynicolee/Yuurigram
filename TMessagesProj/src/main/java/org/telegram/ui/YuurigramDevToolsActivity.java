package org.telegram.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONObject;
import org.telegram.messenger.FileLog;

/**
 * On-device DevTools screen for the Mini App WebView.
 *
 * The inspector is intentionally a separate screen inside Yuurigram while the
 * target WebView stays alive in the previous Telegram screen. The bridge is
 * process-local and does not open a port, contact Telegram, or embed a bot
 * token. The bundled UI is compatible with the DevTools-style workflow and is
 * designed as the host surface for the Chromium DevTools frontend adapter.
 */
public class YuurigramDevToolsActivity extends Activity {
    public static final String EXTRA_SESSION_ID = "yuurigram_devtools_session_id";

    private WebView inspector;
    private WebView target;
    private String sessionId;

    public static void open(Context context, WebView target) {
        if (context == null || target == null) {
            return;
        }
        String sessionId = YuurigramDevToolsSession.register(target);
        Intent intent = new Intent(context, YuurigramDevToolsActivity.class);
        intent.putExtra(EXTRA_SESSION_ID, sessionId);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionId = getIntent().getStringExtra(EXTRA_SESSION_ID);
        target = YuurigramDevToolsSession.get(sessionId);
        if (target == null) {
            finish();
            return;
        }

        inspector = new WebView(this);
        inspector.setBackgroundColor(Color.rgb(32, 33, 36));
        WebSettings settings = inspector.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        inspector.setWebViewClient(new WebViewClient());
        inspector.addJavascriptInterface(new InspectorBridge(), "YuurigramDevTools");
        setContentView(inspector, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        inspector.loadUrl("file:///android_asset/devtools/yuurigram_devtools.html");
        installConsoleCapture();
    }

    private void installConsoleCapture() {
        if (target == null) {
            return;
        }
        target.evaluateJavascript("(function(){if(window.__yuurigramDevToolsInstalled)return true;window.__yuurigramDevToolsInstalled=true;window.__yuurigramDevToolsLogs=[];var levels=['log','info','warn','error','debug'];levels.forEach(function(level){var original=console[level];console[level]=function(){try{window.__yuurigramDevToolsLogs.push({level:level,time:new Date().toISOString(),args:Array.prototype.slice.call(arguments).map(function(v){try{return typeof v==='string'?v:JSON.stringify(v)}catch(e){return String(v)}})});if(window.__yuurigramDevToolsLogs.length>500)window.__yuurigramDevToolsLogs.shift()}catch(e){}return original.apply(console,arguments)}});return true})()", null);
    }

    private void sendToInspector(String requestId, String payload) {
        if (inspector == null || payload == null) {
            return;
        }
        final String safeRequestId = JSONObject.quote(requestId == null ? "" : requestId);
        inspector.evaluateJavascript("window.YuurigramDevToolsReceive(" + safeRequestId + "," + payload + ")", null);
    }

    private void evaluateTarget(String requestId, String expression) {
        if (target == null) {
            sendToInspector(requestId, "{\"error\":\"Target WebView is no longer available\"}");
            return;
        }
        target.evaluateJavascript(expression, value -> sendToInspector(requestId, value == null ? "null" : value));
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy() {
        if (inspector != null) {
            inspector.removeJavascriptInterface("YuurigramDevTools");
            inspector.stopLoading();
            inspector.destroy();
            inspector = null;
        }
        YuurigramDevToolsSession.unregister(sessionId);
        target = null;
        super.onDestroy();
    }

    private class InspectorBridge {
        @JavascriptInterface
        public void ready() {
            installConsoleCapture();
        }

        @JavascriptInterface
        public void snapshot(String requestId) {
            evaluateTarget(requestId, "(function(){var s={};s.url=location.href;s.title=document.title;s.html=document.documentElement?document.documentElement.outerHTML:'';s.text=document.body?document.body.innerText:'';s.query=location.search;s.hash=location.hash;s.storage={local:{},session:{}};try{for(var i=0;i<localStorage.length;i++){var k=localStorage.key(i);s.storage.local[k]=localStorage.getItem(k)}}catch(e){s.storage.localError=String(e)}try{for(var j=0;j<sessionStorage.length;j++){var q=sessionStorage.key(j);s.storage.session[q]=sessionStorage.getItem(q)}}catch(e){s.storage.sessionError=String(e)}return s})()");
        }

        @JavascriptInterface
        public void console(String requestId) {
            evaluateTarget(requestId, "(window.__yuurigramDevToolsLogs||[])");
        }

        @JavascriptInterface
        public void network(String requestId) {
            evaluateTarget(requestId, "(function(){try{return performance.getEntriesByType('resource').map(function(e){return{name:e.name,initiatorType:e.initiatorType,duration:e.duration,size:e.transferSize||0,startTime:e.startTime}})}catch(x){return {error:String(x)}}})()");
        }

        @JavascriptInterface
        public void evaluate(String requestId, String source) {
            if (source == null) {
                sendToInspector(requestId, "{\"error\":\"Empty expression\"}");
                return;
            }
            evaluateTarget(requestId, "(function(){try{return {ok:true,value:(" + source + ")}}catch(e){return {ok:false,error:String(e),stack:e&&e.stack?String(e.stack):''}}})()");
        }

        @JavascriptInterface
        public void clearConsole(String requestId) {
            evaluateTarget(requestId, "(window.__yuurigramDevToolsLogs=[])");
        }

        @JavascriptInterface
        public void reload() {
            if (target != null) {
                target.post(() -> target.reload());
            }
        }
    }
}
