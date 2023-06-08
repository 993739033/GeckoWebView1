'use strict';
const port = browser.runtime.connectNative("browser");

let webview = {
    //关键对应@JavascriptInterface 注解方法
    raiseEvent: function(method,value) {
         browser.runtime.sendMessage({
                   method: method,
                   value: value
               });
    }
}

//关键 对应webview 注入对象
//  webView.addJavascriptInterface(new AndroidInterface(), "webview");
window.wrappedJSObject.webview = cloneInto(
    webview,
    window,
    { cloneFunctions: true });

//接收background.js 发送的消息
browser.runtime.onMessage.addListener((data, sender) => {
        let evalCallBack = {
            method: data.method
        }
        try {
            let result = window.eval(data.method);
            if (result) {
                evalCallBack.result = result;
            } else {
                evalCallBack.result = "";
            }
        } catch (e) {
            evalCallBack.result = e.toString();
            return Promise.resolve(evalCallBack);
        }
        return Promise.resolve(evalCallBack);
});



