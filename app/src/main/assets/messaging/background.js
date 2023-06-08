'use strict';
const port = browser.runtime.connectNative("browser");

//发送js请求到界面
async function sendMessageToTab(message) {
 try {
   let tabs = await browser.tabs.query({})
   return await browser.tabs.sendMessage(
     tabs[tabs.length - 1].id,
     message
   )
 } catch (e) {
   return e.toString();
 }
}

//监听app请求
port.onMessage.addListener(request => {
   sendMessageToTab(request).then((resp) => {
//结果回调
//       port.postMessage(resp);
     }).catch((e) => {
       post.postMessage(e)
     });
})

//接收 content.js 调用
browser.runtime.onMessage.addListener((data, sender) => {
   port.postMessage(data);
   return Promise.resolve('done');
})

