import assign from 'lodash/assign';
import {Encrypt,Decrypt} from "./encrypt";

export default {

    //日期格式
    FormatDate: function (date, type) {
        var seperator1 = "-";
        var seperator2 = ":";
        if (date == null) {
            date = new Date();
        } else if (typeof(date) === "number") {
            date = new Date(date);
        } else {
            date = new Date(date);
        }
        var curyear = date.getFullYear();
        var curmonth = date.getMonth() + 1;
        var curday = date.getDate();
        if (curmonth >= 1 && curmonth <= 9) {
            curmonth = "0" + curmonth;
        }
        if (curday >= 0 && curday <= 9) {
            curday = "0" + curday;
        }

        if ('date' === type) {
            var curDate = curyear + seperator1 + curmonth + seperator1 + curday; //可以获取当前日期
            return curDate;
        }
        else if ('dateHM' === type) {
            var curhour = date.getHours();
            if (curhour >= 0 && curhour <= 9) {
                curhour = "0" + curhour;
            }
            var curmin = date.getMinutes();
            if (curmin >= 0 && curmin <= 9) {
                curmin = "0" + curmin;
            }
            var curDate = curyear + seperator1 + curmonth + seperator1 + curday + " " +
                curhour + seperator2 + curmin; //可以获取当前时间
            return curDate;
        } else {
            var curhour = date.getHours();
            if (curhour >= 0 && curhour <= 9) {
                curhour = "0" + curhour;
            }
            var curmin = date.getMinutes();
            if (curmin >= 0 && curmin <= 9) {
                curmin = "0" + curmin;
            }
            var cursec = date.getSeconds();
            if (cursec >= 0 && cursec <= 9) {
                cursec = "0" + cursec;
            }
            var curDate = curyear + seperator1 + curmonth + seperator1 + curday + " " +
                curhour + seperator2 + curmin + seperator2 + cursec; //可以获取当前时间
            return curDate;
        }
    },

    //金钱格式
    fmoney: function (s, n) {
        var plus_minus = '';
        n = n > 0 && n <= 20 ? n : 2;
        if (parseFloat(s) < 0)
            plus_minus = '-';
        s = parseFloat((Math.abs(s) + "").replace(/[^\d\.-]/g, "")).toFixed(n) + "";
        var l = s.split(".")[0].split("").reverse(),
            r = s.split(".")[1];
        var t = "";
        for (var i = 0; i < l.length; i++) {
            t += l[i] + ((i + 1) % 3 == 0 && (i + 1) != l.length ? "," : "");
        }
        return plus_minus + t.split("").reverse().join("") + "." + r;
    },

    //list 转 tree
    listToTree: (list) => {
        if (list.length === 0) return;
        const _list = [];
        list.map(item => _list.push(assign({}, item)));
        let arr = [];
        //首先状态顶层节点
        _list.map(item => {
            if (!item.pId) {
                arr.push(item);
            }
        });
        let toDo = [];
        arr.map(item => {
            toDo.push(item);
        });
        while (toDo.length) {
            let node = toDo.shift();
            for (let i = 0; i < _list.length; i++) {
                let row = _list[i];
                if (node.id === row.pId) {
                    if (node.children) {
                        node.children.push(row);
                    } else {
                        node.children = [row];
                    }
                    toDo.push(row);
                }
            }
        }

        function sortNumber(a, b) {
            return new Date(a.create_time).getTime() - new Date(b.create_time).getTime()
        }

        arr.sort(sortNumber);

        return arr;
    },
    /**
     * @param 使用js让数字的千分位用,分隔
     */
    shiftThousands: val => {
        if (typeof val !== "number") {
            return null;
        }
        return val.toFixed(2).replace(/(\d)(?=(\d{3})+\.)/g, '$1,');//使用正则替换，每隔三个数加一个','
    },

    //导出excel文件
    exportExcel: options => {
        let {url, params,method, body, success, error} = options;
        const token = sessionStorage.token;
        function obtainURLParam(url) {
            url = url == null ? window.location.href : url;
            let search = url.substring(url.lastIndexOf("?") + 1);
            let obj = {};
            let reg = /([^?&=]+)=([^?&=]*)/g;
            search.replace(reg, function (rs, $1, $2) {
                let name = decodeURIComponent($1);
                let val = decodeURIComponent($2);
                val = String(val);
                obj[name] = val;
                return rs;
            });
            return obj;
        }

        function linkURL(obj) {
            const params = []
            Object.keys(obj).forEach((key) => {
                let value = obj[key]
                // 如果值为undefined我们将其置空
                if (typeof value === 'undefined') {
                    value = ''
                }
                // 对于需要编码的文本（比如说中文）我们要进行编码
                params.push([key, encodeURIComponent(value)].join('='))
            })
            return params.join('&');
        }
        let data = new Object();
        switch (method.toUpperCase()) {
            case 'GET':
                if (token) {
                    if(params){
                        if(Object.prototype.toString.call(params) == '[object String]'){
                            data = JSON.parse(params);
                        }else if(Object.prototype.toString.call(params) == '[object Object]'){
                            data = params;
                        }
                    }else{
                        data =obtainURLParam(url);
                    }
                    data['X-Auth-Token'] = `${token}`;
                }
                let urlPrefix = url.split('?')[0];
                url = data?`${urlPrefix}?${linkURL({p:Encrypt(data)})}`:url;
                break;
            case 'POST':
                if (token) {

                    if(Object.prototype.toString.call(body) == '[object String]'){
                        data = JSON.parse(body);
                    }else if(Object.prototype.toString.call(body) == '[object Object]'){
                        data = body;
                    }
                    data['X-Auth-Token'] = `${token}`;
                }
                body =data?Encrypt(data):null;
                break;
            default:
                break;
        }
        fetch(url, {
            method: method,
            mode: 'cors',
            headers: {
                'Content-Type': 'application/json',
                //'X-Auth-Token': sessionStorage.token
            },
            body:body
        }).then((response) => {
            const disposition = response.headers.get('Content-Disposition');
            let filename;
            if (disposition && disposition.match(/attachment/)) {
                filename = disposition.replace(/attachment;filename=/, '').replace(/"/g, '');
                filename = decodeURIComponent(filename);
                filename = filename || 'file.xls';
                response.blob().then(blob => {
                    const fileUrl = URL.createObjectURL(blob);
                    const saveLink = document.createElement('a');
                    saveLink.href = fileUrl;
                    saveLink.download = filename;
                    let e = new MouseEvent('click');
                    saveLink.dispatchEvent(e);
                    // 使用完ObjectURL后需要及时释放, 否则会浪费浏览器存储区资源.
                    URL.revokeObjectURL(fileUrl);
                });
                if (typeof success === 'function' && success !== undefined) success();
            } else {
                if (typeof error === 'function' && error !== undefined){
                    response.json().then(res => error(Decrypt(res)));
                }
            }
        });
    },

    /**
     * 获取url中参数，且以对象返回
     * @param url
     * @returns {*}
     */
    obtainURLParam: url => {
        url = url == null ? window.location.href : url;
        let search = url.substring(url.lastIndexOf("?") + 1);
        let obj = {};
        let reg = /([^?&=]+)=([^?&=]*)/g;
        search.replace(reg, function (rs, $1, $2) {
            let name = decodeURIComponent($1);
            let val = decodeURIComponent($2);
            val = String(val);
            obj[name] = val;
            return rs;
        });
        return obj;
    },

    /**
     * 拼接url
     * @param param
     * @param key
     * @returns {string}
     */
    linkURL: (urlPrefix,obj) => {
        const params = []
        Object.keys(obj).forEach((key) => {
            let value = obj[key]
            // 如果值为undefined我们将其置空
            if (typeof value === 'undefined') {
                value = ''
            }
            // 对于需要编码的文本（比如说中文）我们要进行编码
            params.push([key, encodeURIComponent(value)].join('='))
        })
        return `${urlPrefix}?${params.join('&')})}`;
    },

    /**
     * 获取随机字符串
     */
    randomString:(len) =>{
    len = len || 32;
    /****默认去掉了容易混淆的字符oOLl,9gq,Vv,Uu,I1****/
    let $chars = 'ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz2345678';
    let maxPos = $chars.length;
    let pwd = '';
    for (let i = 0; i < len; i++) {
        pwd += $chars.charAt(Math.floor(Math.random() * maxPos));
    }
    return pwd;
}


};