package org.telegram.ui;

import android.webkit.WebView;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps a short-lived in-process link between the Mini App WebView and the
 * internal Yuurigram DevTools Activity. No bot token, network listener, or
 * exported component is involved.
 */
public final class YuurigramDevToolsSession {
    private static final Map<String, WeakReference<WebView>> SESSIONS = new ConcurrentHashMap<>();

    private YuurigramDevToolsSession() {
    }

    public static String register(WebView webView) {
        String id = UUID.randomUUID().toString();
        SESSIONS.put(id, new WeakReference<>(webView));
        return id;
    }

    public static WebView get(String id) {
        if (id == null) {
            return null;
        }
        WeakReference<WebView> reference = SESSIONS.get(id);
        WebView webView = reference == null ? null : reference.get();
        if (webView == null) {
            SESSIONS.remove(id);
        }
        return webView;
    }

    public static void unregister(String id) {
        if (id != null) {
            SESSIONS.remove(id);
        }
    }
}
