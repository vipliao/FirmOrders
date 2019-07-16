import  CryptoJS from 'crypto-js'
import util from './util'
import JSEncrypt from './jsencrypt';
import Jsrsasign from 'jsrsasign';


const serverPublicKey='MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCCfoK7cvveWXg8TIqhc5oQEiqwqeeUwxAmRC5q' +
    'aWqhZNnCYZGK4sOhNmR1LHRsELsPUKPBfAnujqmR7N53Cik3895PDPqVSpz/esxlYUZxq4BSxC881fAZepYG7R0A' +
    'P01GjhhHejiS0R62jO6UxWlUqIhb6tG39JdGbxVSF3cRJQIDAQAB';

const clientPrivateKey='MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAKXoWzm22aIJ4aVL7' +
    'wXRXYruv+zABSJ7tytR5Dijdjex1I6iGyLRKdbpoiriBxpNQiOvxm/sBqkY8z5wr1pzTlmAoqXtpeHsnqIdOz' +
    'vDYuj2zpkVbR7AjB5IOGNFRccEPXhbvhGrYK4izorGQnrNaxUAF/pev4/8nyD1TTIGRnytAgMBAAECgYBjYIME' +
    'VxqVjQW0VX+fJIG7rrcXwQ1SUnitYigqJP+y7Lo9laxbdp5xNREGTs2iW5S6e5eJgv+aWm+ZfzYKH/dVflotAP' +
    'gDSLu8w8PpbImsIb/CveAivU0nHQO/9IxMhuodRhWJ8GtCFEj2hcHvr2fqXLJM6UOaUo/eLA5OIh0UoQJBANLv' +
    'Pqwubn6ovpeZjj82SFAV9jumMien2niZp86NuZoNEf7FNaWK57GRV+HNs2EnjwC0Ckf3/6CHNcVMObv4XRsCQQDJ' +
    'Wm5UlJYeMsmzbschB8p4NkkHWu1y3DOtHeQnmda4I/S8HM4AlYmCpOUqugaXrA+yg1V8JKok1KqTM9+3HJHXAkEAya' +
    'y9O1rZgnhMYh4LBx0bXpbg6n8ojQnLtGsVKkQPJN9CSVi8xh5dEEVqxRYrX8S20XFSMyVGX3FnU9BhkvsP6QJAeoU' +
    'COYkoRzvUK9CublTVSIyPp2AWuBtxfld/6vDbDLUkSAqm9wgG4m3/3B3RwRy7eH4d2CpfNYYKPQQFjuBdhQJBANJ' +
    'sxFSuihxX1q+WfPtHikh5/xVNphgGy1OqgAkBw5OuhHImj8kOOJZgfEZvB8GKwwqun3ZCO9IRDI7Ok8ddzBw='

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
    let encrypted
    try{
        let rsaEncrypt = new JSEncrypt();
        rsaEncrypt.setPublicKey(serverPublicKey);
        rsaEncrypted = rsaEncrypt.encryptLong(
            JSON.stringify({
                metadataKey: metadataKey,
                metadataIv: metadataIv,
                metadata: aesEncrypted.toString()
            }));

        //加签
        //获取16位随机数
        let rStr = util.randomString(16);
        let signature = new Jsrsasign.KJUR.crypto.Signature({alg: "SHA1withRSA"});
        // 传入key实例, 初始化signature实例
        signature.init('-----BEGIN PRIVATE KEY-----\n' + clientPrivateKey + '-----END PRIVATE KEY-----');
        // 传入待签明文
        signature.updateString(rStr);
        // 签名, 得到16进制字符结果
        let sign = Jsrsasign.hextob64(signature.sign());
        let rStrStartPart = rStr.substr(0,8);
        let rStrEndPart = rStr.substr(8,8);
        let signStartPart = sign.toString().substr(0,9);
        let signEndPart = sign.toString().substr(9,sign.toString().length);
        let signEndPartLengthStr = signEndPart.length.toString();
        let interStr = util.intersectString(util.randomString(16,true),signEndPartLengthStr);
        encrypted = signStartPart+rStrStartPart+rsaEncrypted.toString()+signEndPart+rStrEndPart+"/"+interStr

    }catch (e) {
        console.error('RSA Encryption failed')
    }
    return encrypted;
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
        console.error('RSA Decryption failed',e)
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
        console.error('AES Decryption failed',e)
    }
    return JSON.parse(decryptedStr);
}

export {
    Encrypt,
    Decrypt,
};

