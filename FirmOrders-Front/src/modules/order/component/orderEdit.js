import React from 'react';
import PropTypes from 'prop-types';
import {
    Row,
    Col,
    Form,
    Input,
    InputNumber,
    Select,
    Breadcrumb,
    Button,
    Modal,
    Divider,
    Spin,
    Icon,
    DatePicker,
    Notification,
    Popconfirm,
    Message,
    Alert,
    Radio
} from 'antd';
import moment from 'moment';
import 'moment/locale/zh-cn';

moment.locale('zh-cn');
import '../index.less';
import {ZZTable} from 'Comps/zz-antD';
import {formItemLayout, itemGrid} from 'Utils/formItemGrid';
import {message} from "antd/lib/index";
import assign from "lodash/assign";
import uniqBy from "lodash/uniqBy";
import indexOf from "lodash/indexOf";
import includes from "lodash/includes";
import axios from "Utils/axios";

const FormItem = Form.Item;
const Search = Input.Search;
const Option = Select.Option;
const RadioGroup = Radio.Group;
const TextArea = Input.TextArea;

class Index extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            data: {},
            allProduct: [],
            orderProduct: [],
            selectedProduct: [],
            selectedRowKeys: [],
            tempSelectedRowKeys: [],
            tempSelectedRow: [],
            selectLoading: false,
            pagination: {},
            params: {
                pageNumber: 1,
                pageSize: 10,
            },
            showTips: false,
            showModal: false,
            canEdit: true,
            loading: false,
            region: sessionStorage.region,
            isOperator: false,
            isInEdit: true,
            submitLoading: false,
            warehouseLoading:false,
            warehouseList:[],
            optionValues:[
                (<Option key='2' value={2}>圆通</Option>),
                (<Option key='0' value={0}>顺丰</Option>),
                (<Option key='1' value={1}>邮政</Option>),
                (<Option key='3' value={3}>中通</Option>),
                (<Option key='4' value={4}>德邦</Option>),
                (<Option key='5' value={5}>联邦</Option>)
            ],
        };

        this.productColumns = [
            {
                title: '产品名称',
                dataIndex: 'name',
                width: 250,
                align: 'center',
                key: 'name'
            }, {
                title: '所属仓库',
                dataIndex: 'wareHouse',
                width: 250,
                align: 'center',
                key: 'wareHouse',
                render: (text, record, index) => {
                    let house;
                    house = record.wareHouseName?record.wareHouseName:'';
                  /*  if (text === 0) house = '广西';
                    else if (text === 1) house = '北京';
                    else if (text === 2) house = '武汉2';*/
                    return (<div>{house}</div>)
                }
            }, {
                title: '单位',
                dataIndex: 'unit',
                key: 'unit',
                align: 'center',
                width: 100
            }, {
                title: '产品条码',
                dataIndex: 'barCode',
                align: 'center',
                width: 250,
                key: 'barCode'
            }]

        this.orderColumns = [
            {
                title: '产品名称',
                dataIndex: 'name',
                align: 'center',
                width: '25%',
                key: 'name'
            }, {
                title: '所属仓库',
                dataIndex: 'wareHouse',
                width: '15%',
                align: 'center',
                key: 'wareHouse',
                render: (text, record, index) => {
                    let house;
                    house = record && record.wareHouseName?record.wareHouseName:'';
                    /*if (text === 0) house = '广西';
                    else if (text === 1) house = '北京';
                    else if (text === 2) house = '武汉2';*/
                    return (<div>{house}</div>)
                }
            }, {
                title: '产品条码',
                dataIndex: 'barCode',
                align: 'center',
                width: '25%',
                key: 'barCode'
            }, {
                title: '数量',
                dataIndex: 'pnumber',
                align: 'center',
                width: '15%',
                key: 'pnumber',
                render: (text, record, index) => (
                    <InputNumber
                        style={{width: '50%'}}
                        defaultValue={text || 1}
                        min={1}
                        step={1}
                        onChange={value => this.setEachProNumber(value, record, index)}
                    />
                )
            }, {
                title: '单位',
                dataIndex: 'unit',
                key: 'unit',
                width: '10%',
                align: 'center',
            }, {
                title: <a><Icon type="setting" style={{fontSize: 18}}/></a>,
                key: 'operation',
                width: '10%',
                align: 'center',
                render: (text, record, index) => (
                    <div>
                        <Popconfirm title="是否删除?" onConfirm={() => this.onDelete(record)}>
                            <a href="javascript:;">删除</a>
                        </Popconfirm>
                    </div>
                )
            }]
    }

    componentWillMount = () => {
        const type = sessionStorage.type;
        //业务员
        if (type === "3") {
            this.setState({isOperator: true});
        }
        this.queryWarehouseList();
    }

    componentDidMount = () => {

       this.queryDetail()
    }
    queryWarehouseList = callback => {
        this.setState({warehouseLoading: true});
        axios.get('warehouse/queryList').then(res => res.data).then(data => {
            if (data.success) {
                let content = data.backData.content;
                let warehouseList = [];
                content.map(item => {
                    warehouseList.push({
                        id: item.id,
                        code:item.code,
                        name: item.name
                    });
                });

                this.setState({
                    warehouseList,
                    warehouseLoading: false
                }, () => {
                    if (typeof callback === 'function') callback();
                });
            } else {
                Message.error(data.backMsg);
            }
        });
    }

    queryDetail = () => {
        const id = this.props.params.id;
        const param = {};
        param.id = id;
        this.setState({
            loading: true
        });
        axios.get('order/findbyid', {
            params: param
        }).then(res => res.data).then(data => {
            if (data.success) {
                let backData = data.backData;
                backData.childrenDetail.map(item => {
                    item.key = item.id;
                });

                this.setState({
                    data: backData,
                    orderProduct: backData.childrenDetail,
                    isInEdit: backData.orderState === 0
                }, () => {
                    this.showTips();
                    this.canEdit();
                    this.setSelectData();
                });
            } else {
                Message.error('订单信息查询失败');
            }
            this.setState({
                loading: false
            });
        });
    }

    showTips = () => {
        const data = this.state.data;
        const deliverDate = data.deliverDate + ' 16:00:00';

        if (new Date(deliverDate).getTime() < new Date().getTime()) {
            this.setState({
                showTips: true
            });
        }
    }

    canEdit = () => {
        const data = this.state.data;
        const deliverDate = data.deliverDate + ' 16:00:00';
        let canEdit = data.orderState === 0 && (new Date(deliverDate).getTime() >= new Date().getTime());
        if (!this.state.isOperator) canEdit = true;
        this.setState({canEdit});
    }

    setSelectData = () => {
        let {data ,orderProduct}= this.state;
        let rowKeys = orderProduct.map(item => item.productId);
        let rows = orderProduct.map(item => {
            return {
                _id: item.id,
                id: item.productId,
                name: item.productName,
                pnumber: item.pnumber,
                unit: item.productUnit,
                barCode: item.productBarCode,
                orderId: item.orderId,
                costPrice: item.productCostPrice,
                wareHouse: item.productWarehouse,
                wareHouseName: item.productWarehouseName,
                voState: item.voState
            }
        });
        let currentWareHouse = data.warehouse;
        let optionValues=[];
        if(currentWareHouse=="004"){
            optionValues=[
                (<Option key='2' value={2}>圆通</Option>),
                (<Option key='0' value={0}>顺丰</Option>),
                (<Option key='1' value={1}>邮政</Option>),
                (<Option key='4' value={4}>德邦</Option>),
                (<Option key='5' value={5}>联邦</Option>)
            ]

        }else  if(currentWareHouse=="005"){
            optionValues=[
                (<Option key='0' value={0}>顺丰</Option>),
                (<Option key='1' value={1}>邮政</Option>)
            ]
        }else{
            optionValues=[
                (<Option key='2' value={2}>圆通</Option>),
                (<Option key='0' value={0}>顺丰</Option>),
                (<Option key='1' value={1}>邮政</Option>),
                (<Option key='3' value={3}>中通</Option>),
                (<Option key='4' value={4}>德邦</Option>),
                (<Option key='5' value={5}>联邦</Option>)
            ]
        }
        this.setState({
            optionValues:optionValues,
            selectedRowKeys: rowKeys,
            selectedProduct: rows
        });
    }



    onDelete = record => {
        let {selectedProduct, selectedRowKeys} = this.state;
        if (record.orderId) {
            selectedProduct.map(item => {
                if (record.id === item.id) {
                    item.voState = 3;
                }
            });
        } else {
            selectedProduct = selectedProduct.filter(item => item.id !== record.id);
        }
        selectedRowKeys = selectedRowKeys.filter(item => item !== record.id);
        this.setState({
            selectedProduct: selectedProduct,
            selectedRowKeys: selectedRowKeys
        });
    }

    showModal = () => {
        const {selectedRowKeys, selectedProduct} = this.state;
        this.setState({
            showModal: true,
            params: {
                pageNumber: 1,
                pageSize: 10,
            },
            tempSelectedRowKeys: selectedRowKeys,
            tempSelectedRow: selectedProduct
        }, () => {
            this.getList();
        })
    }

    handleCancel = () => {
        const {selectedRowKeys, selectedProduct} = this.state
        this.setState({
            tempSelectedRowKeys: selectedRowKeys,
            tempSelectedRow: selectedProduct,
            showModal: false
        });
    }

    handleOk = () => {
        let {tempSelectedRowKeys, tempSelectedRow, selectedProduct} = this.state;
        /* 判断临时选中的行是否有已经删除的接口返回的产品数据，有则把voState重置为2，更新态 */
        selectedProduct.map(item => {
            if(item.voState !=3){
                if (item.orderId) {
                    if (indexOf(tempSelectedRowKeys, item.id) > -1) {
                        item.voState = 2;
                    }
                } else {
                    item.voState = 1;
                }
            }

        });
        tempSelectedRow = uniqBy(tempSelectedRow.concat(selectedProduct), 'id');
        tempSelectedRow = tempSelectedRow.filter(item => includes(tempSelectedRowKeys.concat(selectedProduct.map(item => item.id)), item.id));
        this.setState({
            selectedRowKeys: tempSelectedRowKeys,
            selectedProduct: tempSelectedRow,
            showModal: false
        });
    }

    onSelectChange = (selectedRowKeys, selectedRows) => {
        const selectedNum = selectedRowKeys.length;
        let wareHouse = this.props.form.getFieldValue('warehouse');
        let currentWareHouse;
        if(wareHouse){
            currentWareHouse = wareHouse;
        }else{
            currentWareHouse = this.state.tempSelectedRow
            && this.state.tempSelectedRow.length>0 ?this.state.tempSelectedRow[0].wareHouse:null;
        }

        let warehouseList=this.state.warehouseList?this.state.warehouseList:[];
        let houseName='';
        if(warehouseList){
            for(var i=0;i<warehouseList.length;i++){
                if(warehouseList[i].code == currentWareHouse){
                    houseName = warehouseList[i].name;
                }
            }
        }
        let res = selectedRows.find(item => currentWareHouse  && item.wareHouse != currentWareHouse);
        if(res){
            if(!wareHouse){
                Message.warning(`所选产品${res.name}的仓库,与已选中的其他产品仓库不匹配！`);
                return;
            }
            Message.warning(`当前订单仓库为${houseName},选中产品仓库不匹配！`);
            return;
        }
        //if (selectedNum <= 6) {
            this.setState({
                tempSelectedRowKeys: selectedRowKeys,
                tempSelectedRow: this.state.tempSelectedRow.concat(selectedRows)
            });
        /*} else {
            Message.warning('产品种类最多为六种');
        }*/
        if(!currentWareHouse){
            currentWareHouse = selectedRows[0].wareHouse;
        }
        this.props.form.setFieldsValue({'warehouse':currentWareHouse});

    }

    setEachProNumber = (val, record, index) => {
        let data = this.state.selectedProduct;
        record.pnumber = val ? val : 1;
        if(data && data.length>0){
            data.forEach(function (item) {
                if(item.id && item.id==record.id){
                    item = record;
                }
            })
        }
        this.setState({
            selectedProduct: data
        })
    }

    isSameHouse = (val) => {
        let selectedProduct = this.state.selectedProduct;
        let warehouseList=this.state.warehouseList?this.state.warehouseList:[];
        let houseName='';
        if(warehouseList){
            for(var i=0;i<warehouseList.length;i++){
                if(warehouseList[i].code == wareHouse){
                    houseName = warehouseList[i].name;
                }
            }
        }
        let res = selectedProduct.find(item => item.productWarehouse !== val);
        if (res) {
            Message.warning(`当前订单仓库为${houseName},与选中产品仓库不匹配！`);
            return;
        }
    }

    // 处理分页变化
    handlePageChange = param => {
        const params = assign({}, this.state.params, param);
        this.setState({params}, () => {
            this.getList();
        });
    }

    getList = () => {
        const {params} = this.state;
        this.setState({selectLoading: true});
        axios.get('product/queryList', {
            params: params
        }).then(res => res.data).then(data => {
            if (data.success) {
                const backData = data.backData
                const total = backData ? backData.totalElements : 0;
                data = backData ? backData.content : [];
                data.map(item => {
                    item.key = item.id;
                });

                this.setState({
                    allProduct: data,
                    loading: false,
                    pagination: {total}
                });
            } else {
                message.error('查询产品列表失败');
            }
        });
    }

    /* 处理定金 */
    handleDepositAmoutChange = val => {
        const values = this.props.form.getFieldsValue();
        const {collectionAmout} = values;
        if (!isNaN(collectionAmout) && !isNaN(val)) {
            values.totalAmount = val + collectionAmout;
            this.props.form.setFieldsValue(values);
        }
    }

    /* 处理代收金额 */
    handleCollectionAmoutChange = val => {
        const values = this.props.form.getFieldsValue();
        const {depositAmout} = values;
        if (!isNaN(depositAmout) && !isNaN(val)) {
            values.totalAmount = depositAmout + val;
            this.props.form.setFieldsValue(values);
        }
    }

    validatePhone = (rule, value, callback) => {
        const reg = /^[1][3,4,5,6,7,8,9][0-9]{9}$/;
        if (value && value !== '' && !reg.test(value)) {
            callback(new Error('手机号格式不正确'));
        } else {
            callback();
        }
    }

    validateBlankSpace = (rule, value, callback)=>{
        const reg = /^[^\s]*$/;
        if (value && value !== '' && !reg.test(value)) {
            callback(new Error('输入的数据存在空格'));
        } else {
            callback();
        }
    }

    handleSubmit = (e) => {
        e.preventDefault();
        this.props.form.validateFields((err, values) => {
            if (!err) {
                let {data, selectedProduct} = this.state;
                let validSelectedProduct =[];
                if (selectedProduct.length === 0) {
                    Message.warning('请添加相关产品！');
                    return;
                }
                selectedProduct.forEach(function (item) {
                    if(item.voState != 3){
                        validSelectedProduct.push(item);
                    }
                })
                if (validSelectedProduct.length === 0) {
                    Message.warning('请添加相关产品！');
                    return;
                }

                values.id = this.props.params.id;
                values.orderDate = values.orderDate.format("YYYY-MM-DD");
                values.deliverDate = values.deliverDate.format("YYYY-MM-DD");
                values.incomlineTime = values.incomlineTime.format("YYYY-MM-DD");
                values.costRatio = this.state.data.costRatio;
                values.childrenDetail = selectedProduct.map(item => {
                    return {
                        id: item._id,
                        productId: item.id,
                        productName: item.name,
                        pnumber: item.pnumber || 1,
                        productUnit: item.unit,
                        productBarCode: item.barCode,
                        orderId: item.orderId || data.id,
                        productCostPrice: item.costPrice,
                        productWarehouse: item.wareHouse,
                        productWarehouseName: item.wareHouseName,
                        voState: item.voState || 1
                    }
                });
                let comBin = [];
                if(data.childrenDetail && data.childrenDetail.length>0){
                    data.childrenDetail.forEach(function (item) {
                        let has = false;
                        values.childrenDetail.forEach(function (p) {
                            if(item.id && item.id == p.id ){
                                has = true;
                                return;
                            }
                        })
                        if(!has){
                            item.voState =3;
                            comBin.push(item);
                        }
                    })
                }
                if(comBin.length>0){
                    values.childrenDetail = values.childrenDetail.concat(comBin);
                }
                console.log('handleSubmit  param === ', values);
                this.setState({
                    submitLoading: true
                });
                axios.post('order/save', values).then(res => res.data).then(data => {
                    if (data.success) {
                        Notification.success({
                            message: '提示',
                            description: '更新订单信息成功！'
                        });

                        return this.context.router.push('/frame/order/list');
                    } else {
                        Message.error(data.backMsg);
                    }
                    this.setState({
                        submitLoading: false
                    });
                });
            }
        });
    }

    formWareHouseChange = value =>{
        console.log('formWareHouseChange---'+value+'--');
        let currentWarehouse = this.props.form.getFieldValue("warehouse");
        if(value && value != currentWarehouse){
            let {selectedProduct,selectedRowKeys} = this.state;
            let isSame=true;
            let partSelectProduct=[];
            if(selectedProduct && selectedProduct instanceof  Array && selectedProduct.length>0){
                selectedProduct.forEach(function (item) {
                    if(!item.voState || item.voState !=3){
                        partSelectProduct.push(item);
                    }
                });

            }
            if(partSelectProduct && partSelectProduct.length>0){
                let wareHouse = partSelectProduct[0].wareHouse;
                let wareHouseName = partSelectProduct[0].wareHouseName;
                if(value != wareHouse){
                    Message.warning(`已添加的产品仓库为${wareHouseName},订单所选仓库不匹配,请重新选择产品！`);
                    this.props.form.setFieldsValue({
                        'warehouse':wareHouse,
                        'expressCompany':''
                    });
                    isSame =false
                }
            }
            let optionValues=[];
            if(value=="004"){
                optionValues=[
                    (<Option key='2' value={2}>圆通</Option>),
                    (<Option key='0' value={0}>顺丰</Option>),
                    (<Option key='1' value={1}>邮政</Option>),
                    (<Option key='4' value={4}>德邦</Option>),
                    (<Option key='5' value={5}>联邦</Option>)
                ]

            }else if(value=="005"){
                optionValues=[
                    (<Option key='0' value={0}>顺丰</Option>),
                    (<Option key='1' value={1}>邮政</Option>),
                ]
            }else{
                optionValues=[
                    (<Option key='2' value={2}>圆通</Option>),
                    (<Option key='0' value={0}>顺丰</Option>),
                    (<Option key='1' value={1}>邮政</Option>),
                    (<Option key='3' value={3}>中通</Option>),
                    (<Option key='4' value={4}>德邦</Option>),
                    (<Option key='5' value={5}>联邦</Option>)
                ]
            }
            this.setState({
                optionValues :optionValues,
                selectedProduct:isSame?selectedProduct:[],
                selectedRowKeys:isSame?selectedRowKeys:[]
            });
        }
    }

    /**
     * 日期比较
     * @param date1
     * @param date2
     * @returns {boolean}
     */
    compareDate = (date1,date2) =>{
        if(!date1 || !date2){
            return true;
        }
        let result = false;
        if(date1.getFullYear()>date2.getFullYear()){
            result = true;
        }else if(date1.getFullYear() ==  date2.getFullYear()){
            if(date1.getMonth() > date2.getMonth()){
                result = true;
            }else if(date1.getMonth() ==  date2.getMonth()){
                if(date1.getDate() > date2.getDate()){
                    result = true;
                }
            }
        }
        return result;
    }

    render() {
        const {getFieldDecorator} = this.props.form;
        const {data, selectedProduct, canEdit, isInEdit, showTips, warehouseLoading,warehouseList,tempSelectedRowKeys, isOperator, pagination, allProduct, loading, submitLoading, showModal} = this.state;
        const rowSelection = {
            selectedRowKeys: tempSelectedRowKeys,
            onChange: this.onSelectChange,
        };
        return (
            <div className="zui-content">
                <div className='pageHeader'>
                    <div className="breadcrumb-block">
                        <Breadcrumb>
                            <Breadcrumb.Item>订单管理</Breadcrumb.Item>
                            <Breadcrumb.Item>订单列表</Breadcrumb.Item>
                            <Breadcrumb.Item>更新订单信息</Breadcrumb.Item>
                        </Breadcrumb>
                    </div>
                    <h1 className='title'>更新订单信息</h1>
                </div>
                <div className='pageContent'>
                    <div className='ibox-content'>
                        {
                            showTips ? <Alert message="当前日期已过订单发货日期下午16点，业务员不允许修改" type="warning" showIcon/> : null
                        }
                        <Spin spinning={loading}>
                            <Divider>产品信息</Divider>

                            <div style={{
                                paddingBottom: 30,
                                textAlign: 'center'
                            }}>
                                <div style={{marginBottom: 15}}>
                                    <Button
                                        type='dashed'
                                        icon='plus'
                                        onClick={this.showModal}
                                        style={{
                                            padding: '0 150px',
                                            borderColor: '#b6b6b6'
                                        }}
                                    >选择产品</Button>
                                </div>
                                <ZZTable
                                    size='small'
                                    dataSource={selectedProduct.filter(item => item.voState !== 3)}
                                    columns={this.orderColumns}
                                />
                            </div>
                            <Modal
                                title="添加产品"
                                visible={showModal}
                                destroyOnClose='true'
                                width={800}
                                onCancel={this.handleCancel}
                                footer={[
                                    <Button key="back" onClick={this.handleCancel}>返回</Button>,
                                    <Button key="submit" type="primary" onClick={this.handleOk}>确定</Button>
                                ]}
                            >
                                <Row type='flex' justify="space-around" align="middle">
                                    <Col span={8}>
                                        <Search
                                            placeholder="搜索产品名称关键字"
                                            enterButton
                                            size="default"
                                            onSearch={searchText => this.setState({searchText})}
                                        />
                                    </Col>
                                </Row>
                                <h3 style={{marginBottom: 8}}>
                                    {tempSelectedRowKeys.length ? `已选择 ${tempSelectedRowKeys.length} 个产品` : '未选择产品'}
                                </h3>
                                <ZZTable
                                    columns={this.productColumns}
                                    rowSelection={rowSelection}
                                    dataSource={allProduct}
                                    pagination={pagination}
                                    loading={loading}
                                    handlePageChange={this.handlePageChange.bind(this)}
                                />
                            </Modal>
                            <Divider>订单信息</Divider>
                            <Form onSubmit={this.handleSubmit}>
                                <Row>
                                    <Col {...itemGrid} style={{display: isOperator ? 'none' : 'block'}}>
                                        <FormItem
                                            {...formItemLayout}
                                            label="所属区域"
                                        >
                                            {getFieldDecorator('region', {
                                                rules: [{required: true, message: '请输入所属区域'}],
                                                initialValue: data.region
                                            })(
                                                <Input disabled={isOperator}/>
                                            )}
                                        </FormItem>
                                    </Col>
                                    <Col {...itemGrid} style={{display: isOperator ? 'none' : 'block'}}>
                                        <FormItem
                                            label="所属仓库"
                                            {...formItemLayout}
                                        >
                                            <Spin spinning={warehouseLoading} indicator={<Icon type="loading"/>}>
                                                {getFieldDecorator('warehouse', {
                                                    rules: [{required: true, message: '所属仓库不能为空!',}],
                                                    initialValue: data.warehouse
                                                })(
                                                    <Select onChange={this.formWareHouseChange}>
                                                        {
                                                            warehouseList.map(item => {
                                                                return (<Option key={item.code}
                                                                                value={item.code}>{item.name}</Option>)
                                                            })
                                                        }
                                                    </Select>
                                                )}
                                            </Spin>
                                        </FormItem>
                                    </Col>
                                    <Col {...itemGrid}>
                                        <FormItem
                                            {...formItemLayout}
                                            label="进线时间"
                                        >
                                            {getFieldDecorator('incomlineTime', {
                                                rules: [{required: true, message: '请输入进线时间'}],
                                                initialValue: moment(data.incomlineTime)
                                            })(
                                                <DatePicker style={{width: '100%'}}/>
                                            )}
                                        </FormItem>
                                    </Col>
                                    <Col {...itemGrid}>
                                        <FormItem
                                            {...formItemLayout}
                                            label="成单日期"
                                        >
                                            {getFieldDecorator('orderDate', {
                                                rules: [{required: true, message: '请输入成单日期'}],
                                                initialValue: moment(data.orderDate)

                                            })(
                                                <DatePicker style={{width: '100%'}} onChange={this.getDate}/>
                                            )}
                                        </FormItem>
                                    </Col>
                                    <Col {...itemGrid}>
                                        <FormItem
                                            {...formItemLayout}
                                            label="发货日期"
                                        >
                                            {getFieldDecorator('deliverDate', {
                                                rules: [{required: true, message: '请输入发货日期'}],
                                                initialValue: moment(data.deliverDate)

                                            })(
                                                <DatePicker style={{width: '100%'}}/>
                                            )}
                                        </FormItem>
                                    </Col>
                                    <Col {...itemGrid}>
                                        <FormItem
                                            {...formItemLayout}
                                            label="广告渠道"
                                        >
                                            {getFieldDecorator('advertChannel', {
                                                rules: [
                                                    {required: true, message: '请输入广告渠道'},
                                                    {validator:this.validateBlankSpace}
                                                    ],
                                                initialValue: data.advertChannel
                                            })(
                                                <Input/>
                                            )}
                                        </FormItem>
                                    </Col>
                                    <Col {...itemGrid}>
                                        <FormItem
                                            {...formItemLayout}
                                            label="订单性质"
                                        >
                                            {getFieldDecorator('orderNature', {
                                                rules: [
                                                    {required: true, message: '请输入订单性质'},
                                                    {validator:this.validateBlankSpace}
                                                    ],
                                                initialValue: data.orderNature
                                            })(
                                                <Input/>
                                            )}
                                        </FormItem>
                                    </Col>
                                    <Col {...itemGrid}>
                                        <FormItem
                                            {...formItemLayout}
                                            label="寄件电话"
                                        >
                                            {getFieldDecorator('serderPhone', {
                                                rules: [{required: true, message: '请输入寄件电话'}, {
                                                    validator: this.validatePhone,
                                                }],
                                                initialValue: data.serderPhone
                                            })(
                                                <Input disabled/>
                                            )}
                                        </FormItem>
                                    </Col>
                                    <Col {...itemGrid}>
                                        <FormItem
                                            {...formItemLayout}
                                            label="寄件详细地址"
                                        >
                                            {getFieldDecorator('senderAddr', {
                                                rules: [{required: true, message: '请输入寄件详细地址'}],
                                                initialValue: data.senderAddr
                                            })(
                                                <Input title={data.senderAddr} disabled/>
                                            )}
                                        </FormItem>
                                    </Col>
                                    <Col {...itemGrid}>
                                        <FormItem
                                            {...formItemLayout}
                                            label="成单微信号"
                                        >
                                            {getFieldDecorator('orderWechatCode', {
                                                rules: [
                                                    {required: true, message: '请输入成单微信号'},
                                                    {validator:this.validateBlankSpace}
                                                ],
                                                initialValue: data.orderWechatCode
                                            })(
                                                <Input/>
                                            )}
                                        </FormItem>
                                    </Col>
                                    <Col {...itemGrid} style={{display: 'none'}}>
                                        <FormItem
                                            {...formItemLayout}
                                            label="业务员id"
                                        >
                                            {getFieldDecorator('userId', {
                                                rules: [{required: true, message: '请输入业务员id'}],
                                                initialValue: data.userId
                                            })(
                                                <Input disabled/>
                                            )}
                                        </FormItem>
                                    </Col>
                                    <Col {...itemGrid} style={{display: isOperator ? 'none' : 'block'}}>
                                        <FormItem
                                            {...formItemLayout}
                                            label="业务员姓名"
                                        >
                                            {getFieldDecorator('userName', {
                                                rules: [{required: true, message: '请输入业务员姓名'}],
                                                initialValue: data.userName
                                            })(
                                                <Input disabled/>
                                            )}
                                        </FormItem>
                                    </Col>
                                    <Col {...itemGrid} style={{display: isOperator ? 'none' : 'block'}}>
                                        <FormItem
                                            {...formItemLayout}
                                            label="订单编号"
                                        >
                                            {getFieldDecorator('orderCode', {
                                                rules: [{required: false, message: '请输入订单编号'}],
                                                initialValue: data.orderCode
                                            })(
                                                <Input disabled/>
                                            )}
                                        </FormItem>
                                    </Col>
                                    <Col {...itemGrid} style={{display: isOperator ? 'none' : 'block'}}>
                                        <FormItem
                                            {...formItemLayout}
                                            label="订单状态"
                                        >
                                            {getFieldDecorator('orderState', {
                                                rules: [{required: !isInEdit, message: '请选择'}],
                                                initialValue: data.orderState
                                            })(
                                                <Select disabled={isOperator}>
                                                    <Option key='0' value={0}>编辑中</Option>
                                                    <Option key='1' value={1}>已锁定</Option>
                                                    <Option key='2' value={2}>已发快递</Option>
                                                    <Option key='3' value={3}>成单</Option>
                                                </Select>
                                            )}
                                        </FormItem>
                                    </Col>
                                    <Col {...itemGrid} style={{display: isOperator ? 'none' : 'block'}}>
                                        <FormItem
                                            {...formItemLayout}
                                            label="是否超过成本"
                                        >
                                            {getFieldDecorator('isOverCost', {
                                                rules: [{required: true, message: '请选择'}],
                                                initialValue: data.isOverCost
                                            })(
                                                <RadioGroup>
                                                    <Radio value={0}>否</Radio>
                                                    <Radio value={1}>是</Radio>
                                                </RadioGroup>
                                            )}
                                        </FormItem>
                                    </Col>
                                    <Col {...itemGrid} style={{display: isOperator ? 'none' : 'block'}}>
                                        <FormItem
                                            {...formItemLayout}
                                            label="成本数据"
                                        >
                                            {getFieldDecorator('costAmount', {
                                                rules: [{required: true, message: '请输入成本数据'}],
                                                initialValue: data.costAmount
                                            })(
                                                <InputNumber
                                                    min={0}
                                                    step={0.1}
                                                    precision={2}
                                                    style={{width: '100%'}}
                                                />
                                            )}
                                        </FormItem>
                                    </Col>
                                    <Col {...itemGrid}>
                                        {
                                            isOperator ?
                                                (<FormItem
                                                    {...formItemLayout}
                                                    label="成本比例"
                                                >
                                                    {getFieldDecorator('costRatio', {
                                                        rules: [{required: true, message: '请输入成本比例'}],
                                                        initialValue: data.costRatio > 1 ? '超过' : '不超过'
                                                    })(
                                                        <Input disabled={isOperator}/>
                                                    )}
                                                </FormItem>) :
                                                (<FormItem
                                                    {...formItemLayout}
                                                    label="成本比例"
                                                >
                                                    {getFieldDecorator('costRatio', {
                                                        rules: [{required: true, message: '请输入成本比例'}],
                                                        initialValue: data.costRatio
                                                    })(
                                                        <Input disabled={isOperator}/>
                                                    )}
                                                </FormItem>)
                                        }
                                    </Col>
                                    <Col {...itemGrid}>
                                        <FormItem
                                            {...formItemLayout}
                                            label="快递类别"
                                        >
                                            {getFieldDecorator('expressCompany', {
                                                rules: [{required: true, message: '请输入快递类别'}],
                                                initialValue: data.expressCompany
                                            })(
                                                <Select>
                                                    {this.state.optionValues}
                                                </Select>
                                            )}
                                        </FormItem>
                                    </Col>
                                    <Col {...itemGrid}>
                                        <FormItem
                                            {...formItemLayout}
                                            label="是否国际件"
                                        >
                                            {getFieldDecorator('isForeignExpress', {
                                                rules: [{required: true, message: '请选择'}],
                                                initialValue: data.isForeignExpress
                                            })(
                                                <RadioGroup>
                                                    <Radio value={0}>否</Radio>
                                                    <Radio value={1}>是</Radio>
                                                </RadioGroup>
                                            )}
                                        </FormItem>
                                    </Col>
                                    <Col {...itemGrid} style={{display: isOperator ? 'none' : 'block'}}>
                                        <FormItem
                                            {...formItemLayout}
                                            label="快递单号"
                                        >
                                            {getFieldDecorator('expressCode', {
                                                rules: [{required: !isInEdit, message: '请输入快递单号'}],
                                                initialValue: data.expressCode
                                            })(
                                                <Input disabled={isOperator}/>
                                            )}
                                        </FormItem>
                                    </Col>
                                    <Col {...itemGrid} style={{display: isOperator ? 'none' : 'block'}}>
                                        <FormItem
                                            {...formItemLayout}
                                            label="快递状态"
                                        >
                                            {getFieldDecorator('expressState', {
                                                rules: [{required: !isInEdit, message: '请选择快递状态'}],
                                                initialValue: data.expressState
                                            })(
                                                <Select disabled={isOperator}>
                                                    <Option key='0' value={0}>未发货</Option>
                                                    <Option key='1' value={1}>已发货</Option>
                                                    <Option key='2' value={2}>取消发货</Option>
                                                    <Option key='3' value={3}>未妥投</Option>
                                                    <Option key='4' value={4}>退回</Option>
                                                    <Option key='5' value={5}>签收</Option>
                                                </Select>
                                            )}
                                        </FormItem>
                                    </Col>
                                    <Col {...itemGrid}>
                                        <FormItem
                                            {...formItemLayout}
                                            label="备注"
                                        >
                                            {getFieldDecorator('memo', {
                                                rules: [{required: false, message: '请输入备注'}],
                                                initialValue: data.memo
                                            })(
                                                <TextArea title={data.memo}/>
                                            )}
                                        </FormItem>
                                    </Col>
                                </Row>
                                <Row>
                                    <Divider>收件人信息</Divider>
                                    <Col {...itemGrid}>
                                        <FormItem
                                            {...formItemLayout}
                                            label="收件人"
                                        >
                                            {getFieldDecorator('receiverName', {
                                                rules: [
                                                    {required: true, message: '请输入收件人'},
                                                    {validator:this.validateBlankSpace}
                                                    ],
                                                initialValue: data.receiverName
                                            })(
                                                <Input/>
                                            )}
                                        </FormItem>
                                    </Col>
                                    <Col {...itemGrid}>
                                        <FormItem
                                            {...formItemLayout}
                                            label="收件人手机号"
                                        >
                                            {getFieldDecorator('receiverPhone', {
                                                rules: [{required: true, message: '请输入收件人手机号'}, {
                                                    validator: this.validatePhone,
                                                }],
                                                initialValue: data.receiverPhone
                                            })(
                                                <Input/>
                                            )}
                                        </FormItem>
                                    </Col>

                                    <Col {...itemGrid}>
                                        <FormItem
                                            {...formItemLayout}
                                            label="收件人地址省"
                                        >
                                            {getFieldDecorator('receiverProvince', {
                                                rules: [
                                                    {
                                                        required: this.compareDate(new Date(data.deliverDate),new Date("2019-04-21")),
                                                        message: '请输入收件人地址省'
                                                    }
                                                    ],
                                                initialValue: data.receiverProvince
                                            })(
                                                <Input/>
                                            )}
                                        </FormItem>
                                    </Col>
                                    <Col {...itemGrid}>
                                        <FormItem
                                            {...formItemLayout}
                                            label="收件人地址市"
                                        >
                                            {getFieldDecorator('receiverCity', {
                                                rules: [
                                                    {
                                                        required: this.compareDate(new Date(data.deliverDate),new Date("2019-04-21")),
                                                        message: '请输入收件人收件人地址市'
                                                    }
                                                    ],
                                                initialValue: data.receiverCity
                                            })(
                                                <Input/>
                                            )}
                                        </FormItem>
                                    </Col>
                                    <Col {...itemGrid}>
                                        <FormItem
                                            {...formItemLayout}
                                            label="收件人地址区"
                                        >
                                            {getFieldDecorator('receiverArea', {
                                                rules: [
                                                    {
                                                        required: this.compareDate(new Date(data.deliverDate),new Date("2019-04-21")),
                                                        message: '请输入收件人收件人地址区'
                                                    }
                                                    ],
                                                initialValue: data.receiverArea
                                            })(
                                                <Input/>
                                            )}
                                        </FormItem>
                                    </Col>

                                    <Col {...itemGrid}>
                                        <FormItem
                                            {...formItemLayout}
                                            label="收件人详细地址"
                                        >
                                            {getFieldDecorator('receiverAddr', {
                                                rules: [{required: true, message: '请输入收件人详细地址'}],
                                                initialValue: data.receiverAddr
                                            })(
                                                <Input title={data.receiverAddr}/>
                                            )}
                                        </FormItem>
                                    </Col>
                                    <Col {...itemGrid}>
                                        <FormItem
                                            {...formItemLayout}
                                            label="定金"
                                        >
                                            {getFieldDecorator('depositAmout', {
                                                rules: [{required: true, message: '请输入定金'}],
                                                initialValue: data.depositAmout
                                            })(
                                                <InputNumber
                                                    step={0.01}
                                                    precision={2}
                                                    min={0}
                                                    style={{width: '100%'}}
                                                    onChange={this.handleDepositAmoutChange}
                                                />
                                            )}
                                        </FormItem>
                                    </Col>
                                    <Col {...itemGrid}>
                                        <FormItem
                                            {...formItemLayout}
                                            label="代收金额"
                                        >
                                            {getFieldDecorator('collectionAmout', {
                                                rules: [{required: true, message: '请输入代收金额'}],
                                                initialValue: data.collectionAmout
                                            })(
                                                <InputNumber
                                                    min={0}
                                                    step={0.01}
                                                    precision={2}
                                                    style={{width: '100%'}}
                                                    onChange={this.handleCollectionAmoutChange}
                                                />
                                            )}
                                        </FormItem>
                                    </Col>
                                    <Col {...itemGrid}>
                                        <FormItem
                                            {...formItemLayout}
                                            label="总金额"
                                        >
                                            {getFieldDecorator('totalAmount', {
                                                rules: [{required: true, message: '请输入总金额'}],
                                                initialValue: data.totalAmount
                                            })(
                                                <InputNumber
                                                    min={0}
                                                    step={0.01}
                                                    precision={2}
                                                    style={{width: '100%'}}
                                                    disabled
                                                />
                                            )}
                                        </FormItem>
                                    </Col>
                                </Row>
                                <Row type="flex" justify="center" style={{marginTop: 40}}>
                                    <Button type="primary" size='large' style={{width: 120}} htmlType="submit"
                                            loading={submitLoading} disabled={!canEdit}>提交</Button>
                                </Row>
                            </Form>
                        </Spin>
                    </div>
                </div>
            </div>
        );
    }
}

const orderEdit = Form.create()(Index);

Index.contextTypes = {
    router: PropTypes.object
}

export default orderEdit;