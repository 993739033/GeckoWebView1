package com.scode.geckowebview1;

import static org.mozilla.geckoview.GeckoRuntimeSettings.ALLOW_ALL;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.webkit.WebMessagePort;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.geckoview.ContentBlocking;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoRuntimeSettings;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoSessionSettings;
import org.mozilla.geckoview.GeckoView;
import org.mozilla.geckoview.WebExtension;
import org.mozilla.geckoview.WebExtensionController;

public class MainActivity extends AppCompatActivity {
    GeckoView view_gecko = null;
    static GeckoRuntime runtime = null;
    GeckoSession session = null;
    private static WebExtension.Port mPort;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view_gecko = findViewById(R.id.view_gecko);

        GeckoRuntimeSettings.Builder builder = new GeckoRuntimeSettings.Builder()
                .allowInsecureConnections(ALLOW_ALL)
                .javaScriptEnabled(true)
                .doubleTapZoomingEnabled(false)
                .inputAutoZoomEnabled(false)
                .forceUserScalableEnabled(false)
                .aboutConfigEnabled(true)
                .webManifest(true)
                .consoleOutput(true);
        session = new GeckoSession();
        runtime = GeckoRuntime.create(this,builder.build());
        installExtension();
//        session.getSettings().setAllowJavascript(true);
//        session.getSettings().setDisplayMode(GeckoSessionSettings.DISPLAY_MODE_FULLSCREEN);
//        session.getSettings().setUserAgentMode(GeckoSessionSettings.USER_AGENT_MODE_MOBILE);
        session.open(runtime);
        view_gecko.setSession(session);
//        session.loadUri("https://www.whatismybrowser.com/");
//        session.loadUri("http://html5test.com/");
//        session.loadUri("https://liulanmi.com/labs/core.html");
        session.getSettings().setAllowJavascript(true);
        session.getPanZoomController().setIsLongpressEnabled(false);
//        session.loadUri("about:buildconfig");
        session.loadUri("http://47.102.202.2:8080/app/js1.html");
        findViewById(R.id.btn_run_js).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("method", "runJs('ok')");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mPort.postMessage(jsonObject);
            }
        });
    }

    void installExtension() {
        runtime.getWebExtensionController()
                .ensureBuiltIn("resource://android/assets/messaging/", "messaging@example.com")
                .accept(
                        extension -> {
                            Log.i("MessageDelegate", "Extension installed: " + extension);
                            runOnUiThread(() -> {
                                extension.setMessageDelegate(mMessagingDelegate, "browser");
                            });
                        },
                        e -> Log.e("MessageDelegate", "Error registering WebExtension", e)
                );
    }


    private final WebExtension.MessageDelegate mMessagingDelegate = new WebExtension.MessageDelegate() {
        @Nullable
        @Override
        public void onConnect(@NonNull WebExtension.Port port) {
            Log.e("MessageDelegate", "onConnect");
            mPort = port;
            mPort.setDelegate(mPortDelegate);
        }
    };


    private final WebExtension.PortDelegate mPortDelegate = new WebExtension.PortDelegate() {
        @Override
        public void onPortMessage(final @NonNull Object message,
                                  final @NonNull WebExtension.Port port) {
            Log.e("MessageDelegate", "from extension: "
                    + message);
            try {
                Toast.makeText(MainActivity.this, "收到js调用: " + message.toString(), Toast.LENGTH_LONG).show();
                JSONObject jsonObject = (JSONObject) message;
                String method = jsonObject.getString("method");
                if (method.contains("hasCamera")) {
                    JSONObject request = new JSONObject();
                    request.put("method", "androidView('hasCamera','0')");
                    mPort.postMessage(request);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnect(final @NonNull WebExtension.Port port) {
            Log.e("MessageDelegate:", "onDisconnect");
            if (port == mPort) {
                mPort = null;
            }
        }
    };

    public void evaluateJavascript(String javascriptString) {
        try {
            long id = System.currentTimeMillis();
            Log.e("evalJavascript:id:", id + "");
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("action", "evalJavascript");
            jsonObject.put("data", javascriptString);
            jsonObject.put("id", id);
            Log.e("evalJavascript:", jsonObject.toString());
            mPort.postMessage(jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}