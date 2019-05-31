import axios from 'axios';
import restUrl from 'RestUrl';
import {Encrypt,Sign} from "Utils/encrypt";



axios.defaults.baseURL = restUrl.BASE_HOST;
axios.defaults.headers['Content-Type'] = 'application/json;charset=UTF-8';

// 添加请求拦截器
axios.interceptors.request.use(config => {
    // 在发送请求之前做些什么

    // 数据加密
    /*if (config.data) {
        try {
            config.data =Encrypt(JSON.stringify(config.data)).replace(/\+/g, '%')
        } catch (e) {
            console.error('Encryption failed')
        }
    }*/
    // 签名串
    let timestamp = new Date().getTime();
    //设置token

    const token = sessionStorage.getItem('token');
    if(token){
        config.headers['X-Auth-Token'] = `${token}`;
    }
  /*  if(token){
        let signstr = Sign(token, timestamp, config.data);
        console.log('token', token);
        console.log('signstr', signstr);
        config.headers = {
            timestamp,
            signstr
        };
    }*/

    return config;
}, error => {
    // 对请求错误做些什么
    return Promise.reject(error);
});

// 添加响应拦截器
axios.interceptors.response.use(response => {
    // 对响应数据做点什么
    if (response.status === 401) {
        window.location.hash = '/login';
    } else if (response.data && response.data.auth === false) {
        window.location.hash = '/login';
    }
    return response;
}, error => {
    // 对响应错误做点什么
    return Promise.reject(error);
});

export default axios;