var CODE_SUCCESS = 200;//成功
var CODE_FAIL = -200;//失败
var CODE_DOING = 201;//任务进行中
var CODE_WEBSOCKET_OPEN = 300;//websocket连接成功
var CODE_PACK_SUCCESS = 400;//打包成功

var CODE_GENERATE_PLUGIN_SUCCESS = 500;//生成插件成功


var FILE_TYPE_APK = "apk";//文件目录
var FILE_TYPE_DCODEAPK = "dcodeapk";
var FILE_TYPE_PLUGIN = "plugin";

var FILE_TYPE_KEYSTORE = "keystore";

function uploadFile(url, params, onUploading, onSuccess, onError) {
    $.ajax({
        url: url,
        type: 'POST',
        Accept: 'text/html;charset=UTF-8',
        async: true,
        contentType: "multipart/form-data",
        data: params,
        processData: false,
        contentType: false,
        xhr: function () {
            myXhr = $.ajaxSettings.xhr();
            if (myXhr.upload) {
                myXhr.upload.addEventListener('progress', function (e) {
                    var loaded = e.loaded;//已经上传大小情况
                    var tot = e.total;//附件总大小
                    var per = Math.floor(100 * loaded / tot);  //已经上传的百分比
                    onUploading(per);
                }, false);
            }
            return myXhr;
        },
        success: function (data) {

            onSuccess(data);

        },
        error: function (data) {
            onError(data);

        }
    });
}

function getUUID() {
    var s = [];
    var hexDigits = "0123456789abcdef";
    for (var i = 0; i < 36; i++) {
        s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1);
    }
    s[14] = "4"; // bits 12-15 of the time_hi_and_version field to 0010
    s[19] = hexDigits.substr((s[19] & 0x3) | 0x8, 1); // bits 6-7 of the clock_seq_hi_and_reserved to 01
    s[8] = s[13] = s[18] = s[23] = "-";

    var uuid = s.join("");
    return uuid;
}