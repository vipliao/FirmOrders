import  CryptoJS from 'crypto-js'

/*
const key = CryptoJS.enc.Hex.parse("1234567890ABCDEF");
const iv = CryptoJS.enc.Hex.parse("0123456789ABCDEF");

/!**
 * 加密（需要先加载lib/aes/aes.min.js文件）
 *!/
const Encrypt = (word) => {
    let srcs = CryptoJS.enc.Hex.parse(word);
    let encrypted = CryptoJS.AES.encrypt(srcs, key, {
        iv:iv,
        mode: CryptoJS.mode.CBC,
        padding: CryptoJS.pad.Pkcs7
    });
    return encrypted.ciphertext.toString();
}
/!**
 * 解密
 *!/
const Decrypt = (word) => {
    let decrypt = CryptoJS.AES.decrypt(word, key, {
        iv:iv,
        mode: CryptoJS.mode.CBC,
        padding: CryptoJS.pad.Pkcs7
    });
    return CryptoJS.enc.Hex.stringify(decrypt).toString();
}

// BASE64
const Base64 = {
    en: (data) => CryptoJS.enc.Base64.stringify(CryptoJS.enc.Utf8.parse(data)),
    de: (data) => CryptoJS.enc.Base64.parse(data).toString(CryptoJS.enc.Utf8)
};
// SHA256
const Sha256 = (data) => {
    return CryptoJS.SHA256(data).toString();
};
// MD5
const Md5 = (data) => {
    return CryptoJS.MD5(data).toString();
};

/!**
 * 签名
 * @param token 身份令牌
 * @param timestamp 签名时间戳
 * @param data 签名数据
 *!/
const Sign = (token, timestamp, data) => {
    // 签名格式： timestamp + token + data(字典升序)
    let ret = [];
    for (let it in data) {
        let val = data[it];
        if (typeof val === 'object' && //
            (!(val instanceof Array) || (val.length > 0 && (typeof val[0] === 'object')))) {
            val = JSON.stringify(val);
        }
        ret.push(it + val);
    }
    // 字典升序
    ret.sort();
    let signsrc = timestamp + token + ret.join('');
    return Md5(signsrc);
};

export {
    Encrypt,
    Decrypt,
    Md5,
    Sha256,
    Base64,
    Sign
};
*/

const key = CryptoJS.enc.Utf8.parse("A-16-Byte-keyVal");
const iv = CryptoJS.enc.Utf8.parse("A-16-Byte-String");

//aes加密
const Encrypt =(context) =>{
    if(!context || typeof(context) == 'undefined'){
        return;
    }
    let encrypted;
    if(typeof(context) == 'object'){
        context = JSON.stringify(context);
    }
    let word = CryptoJS.enc.Utf8.parse(context);
    try {
        encrypted = CryptoJS.AES.encrypt(word, key, {
            iv: iv,
            mode: CryptoJS.mode.CBC,
            padding: CryptoJS.pad.Pkcs7
        });
    } catch (e) {
        console.error('Encryption failed')
    }

    return encrypted.toString();
}
// aes解密
const Decrypt = (context) =>{
    if(!context || typeof(context) == 'undefined'){
        return;
    }
    let decryptedStr;
    try{
        let decrypt = CryptoJS.AES.decrypt(context, key, {
            iv: iv,
            mode: CryptoJS.mode.CBC,
            padding: CryptoJS.pad.Pkcs7
        });
        decryptedStr = decrypt.toString(CryptoJS.enc.Utf8);
    }catch (e) {
        console.error('Decryption failed')
    }
    return JSON.parse(decryptedStr);
}

export {
    Encrypt,
    Decrypt,
};

