import axios from 'axios';
import restUrl from 'RestUrl';
import {Encrypt,Decrypt} from "./encrypt";


axios.defaults.baseURL = restUrl.BASE_HOST;
axios.defaults.headers['Content-Type'] = 'application/json;charset=UTF-8';

// 添加请求拦截器
axios.interceptors.request.use(config => {
    // 在发送请求之前做些什么
    //设置token
    const token = sessionStorage.getItem('token');
	if (token) {
        config.headers['X-Auth-Token'] = `${token}`;
    }
    // 数据加密
    /*switch (config.method.toUpperCase()) {
        case 'GET':
            if (token) {
                let data=new Object();
                if(Object.prototype.toString.call(config.params) == '[object String]'){
                    data = JSON.parse( config.params);
                }else if(Object.prototype.toString.call(config.params) == '[object Object]'){
                    data =  config.params;
                }
                data['X-Auth-Token'] = `${token}`;
                config.params = data;
            }
            config.params = config.params?{p:Encrypt(config.params)}:null;
            break;
        case 'POST':
            if (token) {
                let data = new Object();
                if(Object.prototype.toString.call(config.data) == '[object String]'){
                    data = JSON.parse(config.data);
                }else if(Object.prototype.toString.call(config.data) == '[object Object]'){
                    data = config.data;
                }
                data['X-Auth-Token'] = `${token}`;
                config.data = data;
            }
            config.data =config.data?Encrypt(config.data):null;
            break;
        default:
            break;
    }*/

    return config;
}, error => {
    // 对请求错误做些什么
    return Promise.reject(error);
});

// 添加响应拦截器
axios.interceptors.response.use(response => {
    //解密
   /* if(response.data ){
        response.data = Decrypt(response.data);
    }*/
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