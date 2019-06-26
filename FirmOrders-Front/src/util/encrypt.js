import  CryptoJS from 'crypto-js'
import util from './util'
import JSEncrypt from './jsencrypt';

const serverPublicKey='';

const clientPrivateKey='';

/**
 * AES+RSA加密
 * @param context
 * @returns {string}
 * @constructor
 */
const Encrypt =(context) =>{
    if(!context || typeof(context) == 'undefined'){
        return;
    }

    if(typeof(context) == 'object'){
        context = JSON.stringify(context);
    }
    let word = CryptoJS.enc.Utf8.parse(context);

    const metadataKey = util.randomString(16);
    const metadataIv= util.randomString(16);

    //AES加密
    let aesEncrypted;
    const key = CryptoJS.enc.Utf8.parse(metadataKey);
    const iv = CryptoJS.enc.Utf8.parse(metadataIv);
    try {
        aesEncrypted = CryptoJS.AES.encrypt(word, key, {
            iv: iv,
            mode: CryptoJS.mode.CBC,
            padding: CryptoJS.pad.Pkcs7
        });
    } catch (e) {
        console.error('AES Encryption failed')
    }

    //RSA加密
    let rsaEncrypted;
    try{
        let rsaEncrypt = new JSEncrypt();
        rsaEncrypt.setPublicKey(serverPublicKey);
        rsaEncrypted = rsaEncrypt.encryptLong(
            JSON.stringify({
                metadataKey: metadataKey,
                metadataIv: metadataIv,
                metadata: aesEncrypted.toString()
            }));
    }catch (e) {
        console.error('RSA Encryption failed')
    }
    return rsaEncrypted.toString();
}

/**
 * AES+RSA解密
 * @param context
 * @returns {any}
 * @constructor
 */
const Decrypt = (context) =>{
    if(!context || typeof(context) == 'undefined'){
        return;
    }

    //RSA解密
    let rsaDecryptedJson;
    try{
        let rsaDecrypt = new JSEncrypt();
        rsaDecrypt.setPrivateKey(clientPrivateKey);
        let rsaDecrypted = rsaDecrypt.decryptLong(context);
        rsaDecryptedJson = JSON.parse(rsaDecrypted);
    }catch (e) {
        console.error('RSA Decryption failed')
    }

    //AES解密
    const key = CryptoJS.enc.Utf8.parse(rsaDecryptedJson.metadataKey);
    const iv = CryptoJS.enc.Utf8.parse(rsaDecryptedJson.metadataIv);
    let decryptedStr;
    try{
        let decrypt = CryptoJS.AES.decrypt(rsaDecryptedJson.metadata, key, {
            iv: iv,
            mode: CryptoJS.mode.CBC,
            padding: CryptoJS.pad.Pkcs7
        });
        decryptedStr = decrypt.toString(CryptoJS.enc.Utf8);
    }catch (e) {
        console.error('AES Decryption failed')
    }
    return JSON.parse(decryptedStr);
}

export {
    Encrypt,
    Decrypt,
};

