<center>表单</center>

一、描述
React在组件之间传值，比较重要的两个属性：props和state；该form位于props中，本文是关于form的介绍。
二、Simple introduction
1、createForm：<font color=red>创建表单域</font>-支持1列，2列，3列，4列布局，有label；meta&nbsp;&nbsp;&nbsp;&nbsp;模板json数据
2、getAllFormValue()：获取表单所有数据
3、setAllFormValue()：设置表单全部数据
4、getFormItemsValue()：获取表单中某个字段的值
5、setFormItemsValue()：设置表单中某个字段的值
6、setFormItemsDisabled()：设置表单编辑性
7、getFormItemsDisabled()：获取表单某个或某些字段的编辑性
8、setFormItemsRequired(): 设置表单某些字段的必输性
9、getFormItemsRequired()：获取表单某些字段的必输性
10、setFormItemsVerify(): 设置表单某些字段的校验规则
11、getFormItemsVerify(): 获取表单某些字段的校验规则
12、EmptyAllFormValue()：<font color=red>清空表单所有数据</font>
13、isCheckNow()：表单检验(必填,正则校验)
14、openArea(): 展开表单某个区域
15、closeArea(): 收起表单某个区域
16、setFormStatus(): <font color=red>设置表单status</font>
17、getFormStatus(): 获取表单status
18、cancel(): 表单取消方法
19、checkRequired()：检验是否必填
20、getFormItemsVisible()：获取表单某个或某些字段的编辑性
21、setFormItemsVisible(): 设置表单编辑性
22、resetItemWidth(): 设置表单列数
23、setFormItemAutoFocus(): 自动获取焦点API
24、updateForm(): 更新表单
25、getAllFormValueSimple()：获取表单所有简化版数据
26、setItemsVisible()：设置表单 显示 隐藏
27、setAllFormValueRS()：设置表单全部数据(兼容多语控件,默认把值设置到登录语言上)
28、setFormItemsValueRS()：设置表单单个值(兼容多语控件,默认设置到登录语言上,待验证)
29、getResidtxtLang()：根据多语文本 attrcode 获取 当前登录语言的 attrcode
30、setFormPopConfirmSwitchTips()：设置表单 popconfirmswitch 组件 提示语
31、setFormItemFocus()：设置表单某项获取焦点
32、focusFormItem()：聚焦到表单某一项
33、focusFormNextItem()：聚焦到表单 下一项  formId分为两个主要是为了分组
34、updateDataByRefresh()
35、getCacheDataById()：获取表单缓存数据
36、getRequiredItems()：获取必输项字段名
37、updateValuesByRefresh()
三、function detail introduction
引入
```
import PubSub from 'pubsub-js';
import toast from '../../api/toast';
import clearOneTypeToast from '../../api/clearOneTypeToast';
import { localeLang } from '../../public/currentLocale';
import { setFormDisableAttrCode } from '../WithKeyboardChange/createKeyMap';
import {
	focusToNext,
	focusNextItem
} from './enterkey';
```
1、createForm()
2、getAllFormValue()：获取表单所有数据
```
/**
 * @description: 获取表单所有数据
 * @param {String/Array}   
 * @return: Objeact
 */
export function getAllFormValue(moduleIds) {
	if (typeof moduleIds === 'string') {
		if (this.myForm[moduleIds] && this.myForm[moduleIds].state && this.myForm[moduleIds].state.form) {
			//let formData = JSON.parse(JSON.stringify(this.myForm[moduleIds].state.form));
			//formData = setReferValue.call(this, moduleIds, formData);
			let formData = setReferValue.call(this, moduleIds, this.myForm[moduleIds].state.form);
			let data = {
				values: formData
			};
			if (this.myForm[moduleIds]) {
				if (this.myForm[moduleIds].state.status == 'add') {
					data.status = '2';
				} else if (this.myForm[moduleIds].state.status == 'edit') {
					data.status = '1';
				}
				return {
					areaType: 'form',
					rows: [data]
				};
			}
		} else if (this.formSetFormValues[moduleIds] && Object.keys(this.formSetFormValues[moduleIds]).length) {
			//let formData = JSON.parse(JSON.stringify(this.formSetFormValues[moduleIds]));
			//formData = setReferValue.call(this, moduleIds, formData);
			let formData = setReferValue.call(this, moduleIds, this.formSetFormValues[moduleIds]);
			let data = {
				values: formData
			};
			if (this.myForm[moduleIds]) {
				if (this.myForm[moduleIds].state.status == 'add') {
					data.status = '2';
				} else if (this.myForm[moduleIds].state.status == 'edit') {
					data.status = '1';
				}
				return {
					areaType: 'form',
					rows: [data]
				};
			} else {
				return {
					areaType: 'form',
					rows: [data]
				};
			}
		} else if (this.state.meta && this.state.meta[moduleIds]) {
			let data = {
				values: {}
			};
			// 返回值为 rows[0].values.value
			let itemsArr = this.state.meta[moduleIds].items;
			itemsArr.map(item => {
				let initialvalue = {};
				if (item.itemtype === 'checkbox_switch' || item.itemtype === 'switch') {
					initialvalue = { display: '否', value: false };
				} else if (item.itemtype === 'switch_browse') {
					initialvalue = { value: false };
				} else {
					initialvalue = {};
				}
				data.values[item.attrcode] = {
					display: null,
					value: null,
					...initialvalue
				};
			});
			return {
				areaType: 'form',
				rows: [data]
			};
		}
	} else if (moduleIds instanceof Array) {
		let allFormData = {};
		for (let i = 0; i < moduleIds.length; i++) {
			let id = moduleIds[i];
			if (this.myForm[id] && this.myForm[id].state && this.myForm[id].state.form) {
				//let formData = JSON.parse(JSON.stringify(this.myForm[id].state.form));
				//formData = setReferValue.call(this, id, formData);
				let formData = setReferValue.call(this, id, this.myForm[id].state.form);
				allFormData[id] = {
					areaType: 'form',
					rows: [
						{
							status:
								this.myForm[id] &&
								(this.myForm[id].state.status == 'add'
									? '2'
									: this.myForm[id].state.status == 'edit' ? '1' : null),
							values: formData
						}
					]
				};
			} else if (this.formSetFormValues[id] && Object.keys(this.formSetFormValues[id]).length) {
				//let formData = JSON.parse(JSON.stringify(this.formSetFormValues[id]));
				//formData = setReferValue.call(this, id, formData);
				let formData = setReferValue.call(this, id, this.formSetFormValues[id]);
				allFormData[id] = {
					areaType: 'form',
					rows: [
						{
							status:
								this.myForm[id] &&
								(this.myForm[id].state.status == 'add'
									? '2'
									: this.myForm[id].state.status == 'edit' ? '1' : null),
							values: formData
						}
					]
				};
			} else if (this.state.meta && this.state.meta[id]) {
				let data = {
					values: {}
				};
				// 返回值为 rows[0].values.value
				let itemsArr = this.state.meta[id].items;
				itemsArr.map(item => {
					let initialvalue = {};
					if (item.itemtype === 'checkbox_switch' || item.itemtype === 'switch') {
						initialvalue = { display: '否', value: false };
					} else if (item.itemtype === 'switch_browse') {
						initialvalue = { value: false };
					} else {
						initialvalue = {};
					}
					data.values[item.attrcode] = {
						display: null,
						value: null,
						...initialvalue
					};
				});
				allFormData[id] = {
					areaType: 'form',
					rows: [data]
				};
			}
		}
		return allFormData;
	}
}

/**
 * @description: 设置参照值
 * @param {String, Object}
 * @return:
 */
function setReferValue(moduleId, formData) {
	let newResData = {};  //新加--提高效率
	for (let pop in formData) {
		let result = null;
		if (formData[pop]) {
			if (Object.prototype.toString.call(formData[pop].value).slice(8, -1) === 'String') {
				formData[pop].value = formData[pop].value.trim();
			}
			result = {
				display: formData[pop].display,
				value: formData[pop].value,
			};
			if (formData[pop].hasOwnProperty('scale')) {
				result.scale = formData[pop].scale;
			}
		}
		//formData[pop] = result;
		newResData[pop] = result;//新加--提高效率
	}
	return newResData;
}
```
3、setAllFormValue()：设置表单全部数据
```
/**
 * @description: 设置表单全部数据
 * @param {Object, Boolean, Boolean, Array, Function}
 * @return:
 */
export function setAllFormValue(formDataObj, copyFlag = true, emptyOldVal = false, setOldValueExceptKey, callback) {
	//set数据
	for (let moduleId in formDataObj) {
		if (!this.myForm[moduleId] && this.formSetFormValues[moduleId]) {
			Object.keys(this.formSetFormValues[moduleId]).length ? Object.assign(this.formSetFormValues[moduleId], formDataObj[moduleId].rows[0].values) : this.formSetFormValues[moduleId] = formDataObj[moduleId].rows[0].values;
			return;
		}
		let newData = formDataObj[moduleId].rows[0].values;
		// console.log(newData,this.myForm[moduleId].state.verify)
		if (this.myForm[moduleId] && this.myForm[moduleId].state && this.myForm[moduleId].state.form) {
			for (let pop in newData) {
				if (newData[pop] instanceof Array) {
					this.myForm[moduleId].state.form[pop] = newData[pop];
					this.formOldValues[moduleId][pop] = newData[pop];
					if (copyFlag) { //取消时,是否取消到当前值---默认取消到当前值
						this.myForm[moduleId].state.formBack[pop] = newData[pop];
					}
				} else {
					this.myForm[moduleId].state.form[pop] = { ...newData[pop] };
					if (setOldValueExceptKey && setOldValueExceptKey == pop) {
						continue;
					} else {
						this.formOldValues[moduleId][pop] = { ...newData[pop] };
					}
					if (copyFlag) {
						this.myForm[moduleId].state.formBack[pop] = { ...newData[pop] };
					}
				}
				// verify 置为true
				if (this.myForm[moduleId].state.verify[pop]) {
					this.myForm[moduleId].state.verify[pop].verify = true;
				}
			}
		}
		//重新set值时是否清空旧值---默认不清空旧值
		if (emptyOldVal && this.formOldValues[moduleId]) {
			this.formOldValues[moduleId] = {};
		}

		//开关关闭时才setState
		if (!this.isUpdatePage && this.myForm[moduleId]) {
			//更新
			this.myForm[moduleId].setState({
				form: this.myForm[moduleId].state.form,
				formBack: this.myForm[moduleId].state.formBack,
				verify: this.myForm[moduleId].state.verify
			}, () => {

				//最后一次执行setState时 才回调-----待改
				if (callback && typeof callback === 'function') {
					callback();
				}

			});
		}


	}
}
```
4、getFormItemsValue()：获取表单中某个字段的值
```
/**
 * @description: 获取表单中某个字段的值
 * @param {String, Object}
 * @return:
 */
export function getFormItemsValue(moduleId, data) {
	let itemsArr = this.state.meta[moduleId] ? this.state.meta[moduleId].items : [];
	if (this.myForm[moduleId] && this.myForm[moduleId].state && this.myForm[moduleId].state.form) {
		if (typeof data === 'string') {
			let result = null;
			let item = itemsArr.filter(item => item.itemtype === 'residtxt' && item.attrcode === data)[0];
			if (item) {
				result = {};
				item.languageMeta.map(i => item.attrcode + i.index).forEach(i => {
					return result[i] = this.myForm[moduleId].state.form[i];
				});
				result[data] = this.myForm[moduleId].state.form[data];
				return result;
			}
			// itemsArr.map(item => {
			//     if(item.itemtype === 'residtxt' && item.attrcode === data){
			//         result = {};
			//         item.languageMeta.map(i => item.attrcode + i.index).forEach(i => {
			//             return result[i] = this.myForm[moduleId].state.form[i]
			//         })
			//         return result
			//     }
			// })
			//修改参照值获取
			if (this.myForm[moduleId].state.form[data]) {
				result = {
					display: this.myForm[moduleId].state.form[data].display,
					value: this.myForm[moduleId].state.form[data].value,
				};
				if (this.myForm[moduleId].state.form[data].hasOwnProperty('scale')) {
					result.scale = this.myForm[moduleId].state.form[data].scale;
				}
			}
			return result;

		} else if (data instanceof Array) {
			let newData = [];
			const _this = this;
			newData = data.map((item) => {
				//修改参照值获取
				//return getItemValue.call(this, moduleId, item);
				//return  _this.state.form[moduleId][item];
				let result = null;
				if (_this.myForm[moduleId].state.form[item]) {
					result = {
						display: _this.myForm[moduleId].state.form[item].display,
						value: _this.myForm[moduleId].state.form[item].value,
					};
					if (_this.myForm[moduleId].state.form[item].hasOwnProperty('scale')) {
						result.scale = _this.myForm[moduleId].state.form[item].scale;
					}
				}
				return result;
			});
			return newData;
		}
		//页面未初始化form
	} else if (this.formSetFormValues[moduleId] && Object.keys(this.formSetFormValues[moduleId]).length) {
		if (typeof data === 'string') {
			let result = null;
			if (this.formSetFormValues[moduleId][data]) {
				result = {
					display: this.formSetFormValues[moduleId][data].display,
					value: this.formSetFormValues[moduleId][data].value,
				};
				if (this.formSetFormValues[moduleId][data].hasOwnProperty('scale')) {
					result.scale = this.formSetFormValues[moduleId][data].scale;
				}
			} else {
				return {
					display: null,
					value: null
				};
			}
			return result;
		} else if (data instanceof Array) {
			let newData = [];
			const _this = this;
			newData = data.map((item) => {
				let result = null;
				if (_this.formSetFormValues[moduleId][item]) {
					result = {
						display: _this.formSetFormValues[moduleId][item].display,
						value: _this.formSetFormValues[moduleId][item].value,
					};
					if (_this.formSetFormValues[moduleId][item].hasOwnProperty('scale')) {
						result.scale = _this.formSetFormValues[moduleId][item].scale;
					}
				}
				return result;
			});
			return newData;
		}
	} else {
		if (typeof data === 'string') {
			return {
				display: null,
				value: null
			};
		} else if (data instanceof Array) {
			let newData = [];
			newData.data.map((item) => {
				return {
					display: null,
					value: null
				};
			});
			return newData;
		}
	}
}

/**
 * @description: 获取表单单个字段值
 * @param {String, Object}
 * @return:
 */
function getItemValue(moduleId, data) {
	if (this.state.meta[moduleId] && this.state.meta[moduleId].items && this.myForm[moduleId].state.form[data]) {
		let metaItem = this.state.meta[moduleId].items.find((item) => {
			return item.attrcode == data;
		});
		if (metaItem && metaItem.itemtype == 'refer') {
			return {
				display: this.myForm[moduleId].state.form[data].display,
				value: this.myForm[moduleId].state.form[data].value
			};
		} else {
			return this.myForm[moduleId].state.form[data];
		}
	} else {
		return this.myForm[moduleId].state.form[data];
	}
}
```
5、setFormItemsValue()：设置表单中某个字段的值
```
/**
 * @description: 设置表单中某个字段的值
 * @param {String, Object, Boolean}
 * @return:
 */
export function setFormItemsValue(moduleId, values, cancel = true) {
	if (values && Object.prototype.toString.call(values) === '[object Object]') {
		if (!this.myForm[moduleId]) {
			this.formSetFormValues[moduleId] = this.formSetFormValues[moduleId] ? Object.assign(this.formSetFormValues[moduleId], values, { cancel }) : Object.assign({}, values, { cancel });
			return;
		}
		for (let key of Object.keys(values)) {
			if (key) {
				this.myForm[moduleId].state.form[key] = this.myForm[moduleId].state.form[key] || {};
				this.myForm[moduleId].state.form[key] = { ...values[key] };
				//若cancel为false则取消时,该项不会清空
				if (!cancel) {
					this.myForm[moduleId].state.formBack[key] = { ...values[key] };
				}
				//把设置的值存入旧值中
				this.formOldValues[moduleId][key] = { ...values[key] };
				// setFormAttribute.call(this, moduleId, { [key]: true }, 'verify')
				// 设置单个值  verify 置为true    
				if (this.myForm[moduleId].state.verify[key]) {
					this.myForm[moduleId].state.verify[key].verify = true;
				}

			}
		}

		//开关打开时直接退出
		if (this.isUpdatePage) return;

		this.myForm[moduleId].setState({
			form: this.myForm[moduleId].state.form,
			formBack: this.myForm[moduleId].state.formBack,
			verify: this.myForm[moduleId].state.verify
		});
		// this.setState({
		//     meta: this.state.meta
		// })
	}

}
```
6、setFormItemsDisabled()：设置表单编辑性
```
/**
 * @description: 设置表单编辑性
 * @param {String, Object}
 * @return:
 */
export function setFormItemsDisabled(moduleId, values) {
	//setFormAttribute.call(this, moduleId, values, 'disabled');
	if ((!this.myForm[moduleId]) || (!this.myForm[moduleId].state)) {
		if (this.formItemAttrFlag && this.formItemAttrFlag[moduleId]) {
			if (!this.formItemAttrFlag[moduleId]['disabled']) {
				this.formItemAttrFlag[moduleId]['disabled'] = {};
			}
			Object.assign(this.formItemAttrFlag[moduleId]['disabled'], values);
		}
		return;
	}

	Object.assign(this.myForm[moduleId].state.disabled, values);

	setFormDisableAttrCode(moduleId, this.myForm[moduleId].state.disabled);

	//开关打开时直接退出
	if (this.isUpdatePage) return;

	this.myForm[moduleId].setState({
		disabled: this.myForm[moduleId].state.disabled
	});

}
```
7、getFormItemsDisabled()：获取表单某个或某些字段的编辑性
```
/**
 * @description: 获取表单某个或某些字段的编辑性
 * @param {String, String}
 * @return:
 */
export function getFormItemsDisabled(moduleId, id) {
	//return getFormAttribute.call(this, moduleId, id, 'disabled');
	if ((!this.myForm[moduleId]) || (!this.myForm[moduleId].state)) {
		if (this.formItemAttrFlag &&
			this.formItemAttrFlag[moduleId] &&
			this.formItemAttrFlag[moduleId]['disabled']
		) {
			if (typeof id === 'string') {
				return !!this.formItemAttrFlag[moduleId]['disabled'][id];
			}
			if (id instanceof Array) {
				let res = [];
				res = id.map((item, i) => {
					return !!this.formItemAttrFlag[moduleId]['disabled'][item];
				});
				return res;
			}
		}
	} else {
		if (this.myForm[moduleId].state.disabled) {
			if (typeof id === 'string') {
				return !!this.myForm[moduleId].state.disabled[id];
			}
			if (id instanceof Array) {
				let res = [];
				res = id.map((item, i) => {
					return !!this.myForm[moduleId].state.disabled[item];
				});
				return res;
			}
		}

	}

}
```
8、setFormItemsRequired(): 设置表单某些字段的必输性
```
/**
 * @description: 设置表单某些字段的必输性
 * @param {String,Obejct}
 * @return:
 */
export function setFormItemsRequired(moduleId, values) {
	setFormAttribute.call(this, moduleId, values, 'required');
}
```
9、getFormItemsRequired()：获取表单某些字段的必输性
```
/**
 * @description: 获取表单某些字段的必输性
 * @param {String,String}
 * @return:
 */
export function getFormItemsRequired(moduleId, id) {
	return getFormAttribute.call(this, moduleId, id, 'required');
}
```
10、setFormItemsVerify()
```
/**
 * @description: 设置表单某些字段的校验规则
 * @param {type}
 * @return:
 */
export function setFormItemsVerify(moduleId, values) {
	//setFormAttribute.call(this, moduleId, values, 'verify');
}
```
11、getFormItemsVerify(): 获取表单某些字段的校验规则
```
/**
 * @description: 获取表单某些字段的校验规则
 * @param {type}
 * @return:
 */
export function getFormItemsVerify(moduleId, id) {
	//return getFormAttribute.call(this, moduleId, id, 'verify');
}
```
12、EmptyAllFormValue()：清空表单所有数据
```
/**
 * @description: 清空表单所有数据
 * @param {String, Array, Boolean}
 * @return:
 */
export function EmptyAllFormValue(moduleId, exceptArr, cancel = true) {
	let switchfalse = ['switch', 'switch_browse', 'checkbox_switch'];
	if (this.myForm[moduleId] && this.myForm[moduleId].state && this.myForm[moduleId].state.form) {
		for (let pop in this.myForm[moduleId].state.form) {
			//不清空
			let flag = true;
			if (Array.isArray(exceptArr) && exceptArr.length > 0 && exceptArr.includes(pop)) {
				continue;
				//清空
			} else {
				if (this.state.meta.formrelation && this.state.meta.formrelation[moduleId]) {
					let mIds = [moduleId];
					mIds = mIds.concat(this.state.meta.formrelation[moduleId]);
					mIds.forEach((modid) => {
						let item = this.state.meta[modid].items.find(function (elem) {
							// delete elem.verify;   // 删除字段校验信息
							return elem.attrcode == pop;
						});
						if (item && item.hasOwnProperty('itemtype')) {
							flag = false;
							this.myForm[moduleId].state.form[pop].value = item.initialvalue ? item.initialvalue.value : (switchfalse.includes(item.itemtype) ? false : null);
							this.myForm[moduleId].state.form[pop].display = item.initialvalue ? item.initialvalue.display : (switchfalse.includes(item.itemtype) ? '否' : null);
							if (this.formOldValues[moduleId]) {
								this.formOldValues[moduleId][pop] = {
									value: item.initialvalue ? item.initialvalue.value : null
								};
							}
							//取消时,是否取消到当前值---默认取消到当前值
							if (cancel) {
								this.myForm[moduleId].state.formBack[pop] ? (this.myForm[moduleId].state.formBack[pop].value = item.initialvalue ? item.initialvalue.value : (switchfalse.includes(item.itemtype) ? false : null)) : null;
								this.myForm[moduleId].state.formBack[pop] ? (this.myForm[moduleId].state.formBack[pop].display = item.initialvalue ? item.initialvalue.display : (switchfalse.includes(item.itemtype) ? '否' : null)) : null;
							}
						} else if (flag && !item) {
							this.myForm[moduleId].state.form[pop].value = null;
							this.myForm[moduleId].state.form[pop].display = null;
							if (this.formOldValues[moduleId]) {
								this.formOldValues[moduleId][pop] = {
									value: null
								};
							}
							//取消时,是否取消到当前值---默认取消到当前值
							if (cancel) {
								this.myForm[moduleId].state.formBack[pop] ? this.myForm[moduleId].state.formBack[pop].value = null : null;
								this.myForm[moduleId].state.formBack[pop] ? this.myForm[moduleId].state.formBack[pop].display = null : null;
							}
						}
					});
				} else {
					if (this.state.meta[moduleId] && this.state.meta[moduleId].items) {
						let item = this.state.meta[moduleId].items.find(function (elem) {
							// delete elem.verify; // 删除字段校验信息
							return elem.attrcode == pop;
						});
						if (item && item.hasOwnProperty('itemtype')) {
							this.myForm[moduleId].state.form[pop].value = item.initialvalue ? item.initialvalue.value : (switchfalse.includes(item.itemtype) ? false : null);
							this.myForm[moduleId].state.form[pop].display = item.initialvalue ? item.initialvalue.display : (switchfalse.includes(item.itemtype) ? '否' : null);
							if (this.formOldValues[moduleId]) {
								this.formOldValues[moduleId][pop] = {
									value: item.initialvalue ? item.initialvalue.value : null
								};
							}
							//取消时,是否取消到当前值---默认取消到当前值
							if (cancel) {
								this.myForm[moduleId].state.formBack[pop] ? (this.myForm[moduleId].state.formBack[pop].value = item.initialvalue ? item.initialvalue.value : (switchfalse.includes(item.itemtype) ? false : null)) : null;
								this.myForm[moduleId].state.formBack[pop] ? (this.myForm[moduleId].state.formBack[pop].display = item.initialvalue ? item.initialvalue.display : (switchfalse.includes(item.itemtype) ? '否' : null)) : null;
							}
						} else if (!item) {
							this.myForm[moduleId].state.form[pop].value = null;
							this.myForm[moduleId].state.form[pop].display = null;
							if (this.formOldValues[moduleId]) {
								this.formOldValues[moduleId][pop] = {
									value: null
								};
							}
							//取消时,是否取消到当前值---默认取消到当前值
							if (cancel) {
								this.myForm[moduleId].state.formBack[pop] ? this.myForm[moduleId].state.formBack[pop].value = null : null;
								this.myForm[moduleId].state.formBack[pop] ? this.myForm[moduleId].state.formBack[pop].display = null : null;
							}
						}
					}

				}
				// verify 置为true
				if (this.myForm[moduleId].state.verify &&
					this.myForm[moduleId].state.verify[pop] &&
					this.myForm[moduleId].state.verify[pop].verify
				) {
					this.myForm[moduleId].state.verify[pop].verify = true;
				}

			}
		}

		//开关打开时直接退出
		if (this.isUpdatePage) return;

		this.myForm[moduleId].setState({
			form: this.myForm[moduleId].state.form,
			verify: this.myForm[moduleId].state.verify,
			formBack: this.myForm[moduleId].state.formBack
		});
	}
}
```
13、isCheckNow()：表单检验(必填,正则校验)
```
/**
 * @description: 单个表单校验
 * @param {String}  
 * @return: Boolean
 */
function checkFormById(moduleId) {
	let flag = true;
	//必输项校验和正则匹配
	let requiredItems = getRequiredItems.call(this, moduleId)[0];
	let regItems = getRequiredItems.call(this, moduleId)[1];
	let maxItem = getRequiredItems.call(this, moduleId)[2];
	let requiredlabel = [];
	let reglabel = [];
	let maxlabel = [];
	let switchfalse = ['switch', 'switch_browse', 'radio', 'checkbox_switch'];
	let itemsArr = [];
	if (this.state.meta.formrelation && this.state.meta.formrelation[moduleId]) { //分组表单
		if (this.state.meta[moduleId] && this.state.meta[moduleId].items) {
			itemsArr = this.state.meta[moduleId].items;
			this.state.meta.formrelation[moduleId].forEach((elem) => {
				if (this.state.meta[elem] && this.state.meta[elem].items) {
					itemsArr = itemsArr.concat(this.state.meta[elem].items);
				}
			});
		}
	} else { //单个表单
		if (this.state.meta[moduleId] && this.state.meta[moduleId].items) {
			itemsArr = this.state.meta[moduleId].items;
		}
	}
	for (let key in this.myForm[moduleId].state.verify) {
		let metaItem = itemsArr.find((item) => {
			return item.attrcode == key;
		});
		if (requiredItems.includes(key) && metaItem && !switchfalse.includes(metaItem.itemtype)) {
			flag = false;
			this.myForm[moduleId].state.verify[key].verify = false;
			requiredlabel.push(this.myForm[moduleId].state.verify[key].label);
		} else {
			this.myForm[moduleId].state.verify[key].verify = true;
		}
		// 正则
		if (regItems.includes(key)) {
			flag = false;
			this.myForm[moduleId].state.verify[key].verify = false;
			reglabel.push(this.myForm[moduleId].state.verify[key].label);
		}
		// maxlength
		if (maxItem.includes(key)) {
			flag = false;
			this.myForm[moduleId].state.verify[key].verify = false;
			maxlabel.push(this.myForm[moduleId].state.verify[key].label);
		}
	}
	this.myForm[moduleId].setState({
		verify: this.myForm[moduleId].state.verify
	});
	//该表单的所有校验都通过
	return {
		flag,
		requiredItems: requiredlabel,
		regItems: reglabel,
		itemsAttrcode: requiredItems,
		maxItem: maxlabel
	};
}


/**
 * @description: 表单检验(必填,正则校验)
 * @param {String, String}
 * @return: Boolean
 */
export function isCheckNow(moduleId, type = 'danger') {
	let flag = true;
	let requiredItems = [];
	let regItems = [];
	let maxItem = [];
	let attrcode = '';
	if (typeof moduleId === 'string') {
		if (this.myForm[moduleId]) {
			let verifyObj = checkFormById.call(this, moduleId);
			if (verifyObj) {
				flag = verifyObj.flag;
				requiredItems = verifyObj.requiredItems;
				regItems = verifyObj.regItems;
				maxItem = verifyObj.maxItem;
				attrcode = verifyObj.itemsAttrcode[0];
			}
		}
	} else if (moduleId instanceof Array) {
		for (let i = 0; i < moduleId.length; i++) {
			if (this.myForm[moduleId[i]]) {
				let res = checkFormById.call(this, moduleId[i]);
				if (res) {
					let newFlag = res.flag;
					requiredItems = requiredItems.concat(res.requiredItems);
					regItems = regItems.concat(res.regItems);
					maxItem = maxItem.concat(res.maxItem);
					flag = flag && newFlag;
				}
			}
		}
	}
	if (!flag) {
		moduleId = Object.prototype.toString.call(moduleId).slice(8, -1) == 'Array' ? moduleId[0] : moduleId;
		let requiredItemsMsgTitle = this.myForm[moduleId].state.json['page-form-0001'];
		let regItemsMsgTitle = this.myForm[moduleId].state.json['page-form-0002'];
		let maxItemMsgTitle = this.myForm[moduleId].state.json['page-form-0010'];
		let title = this.myForm[moduleId].state.json['page-form-0009'];
		let requiredItemsMsg = requiredItems.join(', ').length === 0 ? '' : `${requiredItemsMsgTitle}：${requiredItems.join(', ')}`;
		let regItemsMsg = regItems.join(', ').length === 0 ? '' : `${regItemsMsgTitle}：${regItems.join(', ')}`;
		let maxItemMsg = maxItem.join(', ').length === 0 ? '' : `${maxItemMsgTitle}：${maxItem.join(', ')}`;
		if (attrcode) {
			PubSub.publish('autoFocus', { data: attrcode });
		}
		// let toastContent = requiredItemsMsg;
		// if (toastContent && regItemsMsg){
		// 	toastContent = toastContent + '\\n' + regItemsMsg;
		// }
		// if(toastContent && maxItemMsg){
		// 	toastContent = toastContent + '\\n' + maxItemMsg;
		// }
		let toastContent = [requiredItemsMsg, regItemsMsg, maxItemMsg].filter(e => !!e == true).join('\\n');
		// [requiredItemsMsg,regItemsMsg,maxItemMsg].filter(e => !!e == true).join('\\n');
		toast({
			mark: 'form_isCheckNow',
			color: type,
			title,
			//content: `${requiredItemsMsg} \\n ${regItemsMsg} \\n ${maxItemMsg}`
			content: toastContent
		});
	}
	return flag;
}
```
14、openArea(): 展开表单某个区域
```
/**
 * @description: 展开表单某个区域
 * @param {String}
 * @return:
 */
export function openArea(moduleId) {
	if (!moduleId) return;
	let meta = this.meta.getMeta();
	if (meta && meta.formrelation && Object.keys(meta.formrelation).length) {
		let relation = meta.formrelation;
		let maincode = '';
		for (let pop in relation) {
			if (Array.isArray(relation[pop]) && relation[pop].includes(moduleId)) {
				maincode = pop;
				break;
			}
		}
		if (maincode && this.myForm[maincode].state.showFormIcon) {
			this.myForm[maincode].state.showFormIcon[moduleId] = true;
			this.myForm[maincode].setState({
				showFormIcon: this.myForm[maincode].state.showFormIcon
			});
		}
	}
}
```
15、closeArea(): 收起表单某个区域
```
/**
 * @description: 收起表单某个区域
 * @param {String}  
 * @return:
 */
export function closeArea(moduleId) {
	if (!moduleId) return;
	let meta = this.meta.getMeta();
	if (meta && meta.formrelation && Object.keys(meta.formrelation).length) {
		let relation = meta.formrelation;
		let maincode = '';
		for (let pop in relation) {
			if (Array.isArray(relation[pop]) && relation[pop].includes(moduleId)) {
				maincode = pop;
				break;
			}
		}
		if (maincode && this.myForm[maincode].state.showFormIcon && this.myForm[maincode].state.showFormIcon) {
			this.myForm[maincode].state.showFormIcon[moduleId] = false;
			this.myForm[maincode].setState({
				showFormIcon: this.myForm[maincode].state.showFormIcon
			});
		}
	}
}
```
16、setFormStatus(): 设置表单status
```
/**
 * @description: 设置表单status
 * @param {String, String}
 * @return:
 */
export function setFormStatus(moduleId, status) {
	if (!this.myForm[moduleId]) {
		if (this.formStatusFlag) {
			this.formStatusFlag[moduleId] = status;
		}
		return;
	}
	if (this.myForm[moduleId] && this.myForm[moduleId].state) {
		let flag = this.myForm[moduleId].state.status;
		this.myForm[moduleId].state.status = status;


		//页面从编辑态变为浏览态的时候，清除所在页面danger类型的toast提示 -liuxis
		if (flag !== status && status === 'browse') clearOneTypeToast('danger');

		//开关打开时直接退出
		if (this.isUpdatePage) return;

		this.myForm[moduleId].setState({
			status: this.myForm[moduleId].state.status
		});
	}
}
```
17、getFormStatus(): 获取表单status
```
/**
 * @description: 获取表单status
 * @param {String/Array}
 * @return:
 */
export function getFormStatus(moduleId) {
	if (this.myForm[moduleId]) {
		if (this.myForm[moduleId].state.status) {
			return this.myForm[moduleId].state.status;
		} else if (this.state.meta && this.state.meta[moduleId] && this.state.meta[moduleId].status) {
			return this.state.meta[moduleId].status;
		}
	} else if (this.formStatusFlag[moduleId]) {
		return this.formStatusFlag[moduleId];
	} else if (this.state.meta && this.state.meta[moduleId] && this.state.meta[moduleId].status) {
		return this.state.meta[moduleId].status;
	} else {
		return 'browse';
	}
}
```
18、cancel(): 表单取消方法
```
/**
 * @description: 表单取消方法
 * @param {String/Array}
 * @return:
 */
export function cancel(ids) {
	if (typeof ids === 'string' && this.myForm[ids]) {
		if (Object.prototype.toString.call(this.myForm[ids].state.formBack) == '[object Object]') {
			this.myForm[ids].state.form = JSON.parse(JSON.stringify(this.myForm[ids].state.formBack));
		}
		for (let key in this.myForm[ids].state.verify) {
			this.myForm[ids].state.verify[key].verify = true;
		}
		// this.state.meta[ids].status = 'browse';
		this.myForm[ids].state.status = 'browse';

		// 清除danger toast
		clearOneTypeToast('danger');
		//适配一主多子
		// if (this.state.meta.formrelation && this.state.meta.formrelation[ids]) {
		// 	this.state.meta.formrelation[ids].forEach((elem, index) => {
		// 		this.state.meta[elem].status = 'browse';
		// 	});
		// }

		this.myForm[ids].setState({
			form: this.myForm[ids].state.form,
			status: this.myForm[ids].state.status,
			verify: this.myForm[ids].state.verify
		});


	} else if (ids instanceof Array) {
		for (let i = 0; i < ids.length; i++) {
			let id = ids[i];
			if (this.myForm[id] && this.myForm[id].state && this.myForm[id].state.form) {
				if (Object.prototype.toString.call(this.myForm[id].state.formBack) == '[object Object]') {
					this.myForm[id].state.form = JSON.parse(JSON.stringify(this.myForm[id].state.formBack));
				}
				for (let key in this.myForm[id].state.verify) {
					this.myForm[id].state.verify[key].verify = true;
				}
				// this.state.meta[id].status = 'browse';
				this.myForm[id].state.status = 'browse';

				// 清除danger toast
				clearOneTypeToast('danger');
				//适配一主多子
				// if (this.state.meta.formrelation && this.state.meta.formrelation[ids]) {
				// 	this.state.meta.formrelation[ids].forEach((elem, index) => {
				// 		this.state.meta[elem].status = 'browse';
				// 	});
				// }
				this.myForm[id].setState({
					form: this.myForm[id].state.form,
					status: this.myForm[id].state.status,
					verify: this.myForm[id].state.verify
				});

			}
		}
	}
}
```
19、checkRequired()：检验是否必填
```
/**
 * @description: 检验是否必填
 * @param {String}
 * @return: Boolean
 */
export function checkRequired(moduleId) {
	let requiredItems = getRequiredItems.call(this, moduleId)[0];
	if (requiredItems && requiredItems.length > 0) {
		return false;
	}
	return true;
}
```
20、getFormItemsVisible()：获取表单某个或某些字段的编辑性
```
//需要修改，以适配一主多子
/**
 * @description: 获取表单某字段属性
 * @param {String, String, String}
 * @return: {}
 */
function getFormAttribute(moduleId, id, attribute) {
	if (typeof id === 'string') {
		let arr = this.state.meta[moduleId].items.find(function (elem) {
			return elem.attrcode == id;
		});
		return arr[attribute];
	} else if (id instanceof Array) {
		let newData = [];
		const _this = this;
		newData = id.map((item, i) => {
			let arr = this.state.meta[moduleId].items.find(function (elem) {
				return elem.attrcode == item;
			});
			return arr[attribute];
		});
		return newData;
	}
}

/**
 * @description: 获取表单某个或某些字段的编辑性
 * @param {String, String}
 * @return:
 */
export function getFormItemsVisible(moduleId, id) {
	return getFormAttribute.call(this, moduleId, id, 'visible');
}
```
21、setFormItemsVisible(): 设置表单编辑性
```
/**
 * @description: 设置表单编辑性
 * @param {String, Object}
 * @return:
 */
export function setFormItemsVisible(moduleId, values) {
	//setItemsVisible.call(this, moduleId, values);
	setFormAttribute.call(this, moduleId, values, 'visible');
}
```
22、resetItemWidth(): 设置表单列数
```
/**
 * @description: 设置表单列数
 * @param {String}
 * @return:
 */
export function resetItemWidth(moduleId) {
	let ele = document.getElementById(moduleId);
	let width;
	if (ele) {
		width = ele.offsetWidth;
	}
	let formItemWidth = 50;
	let column = 2;
	if (width < 600) {
		formItemWidth = 100;
		column = 1;
	}
	if (width >= 600 && width < 900) {
		formItemWidth = 50;
		column = 2;
	}
	if (width >= 900 && width < 1230) {
		formItemWidth = 33.333333;
		column = 3;
	}
	if (width >= 1230 && width < 1820) {
		formItemWidth = 25;
		column = 4;
	}
	if (width >= 1820) {
		formItemWidth = 20;
		column = 5;
	}
	if (this.myForm[moduleId] && this.myForm[moduleId].state) {
		this.myForm[moduleId].state.formItemWidth.width = formItemWidth;
		this.myForm[moduleId].state.formItemWidth.column = column;


		this.myForm[moduleId].setState(
			{
				formItemWidth: this.myForm[moduleId].state.formItemWidth
			}
		);
	}

}
```
23、setFormItemAutoFocus(): 自动获取焦点API
```
/**
 * @description: 自动获取焦点API
 * @param {String,String}
 * @return:
 */
export function setFormItemAutoFocus(moduleId, attrcode) {
	if (moduleId && attrcode) {
		this.myForm[moduleId].state.autoFocusFormItem = attrcode;
		this.myForm[moduleId].setState({
			autoFocusFormItem: this.myForm[moduleId].state.autoFocusFormItem
		});
	}
}
```
24、updateForm(): 更新表单
```
/**
 * @description: 更新表单
 * @param {String/Array}
 * @return:
 */
export function updateForm(moduleId) {
	if (typeof moduleId === 'string') {
		if (this.myForm[moduleId]) {
			this.myForm[moduleId].setState(
				this.myForm[moduleId].state
			);
		}
	}
	if (moduleId instanceof Array && moduleId.length) {
		for (let i = 0; i < moduleId.length; i++) {
			if (this.myForm[moduleId[i]]) {
				this.myForm[moduleId[i]].setState(
					this.myForm[moduleId].state
				);
			}
		}
	}
}
```
25、getAllFormValueSimple()：获取表单所有简化版数据
```
/**
 * @description: 获取表单所有简化版数据
 * @param {String, Array}
 * @return: Array
 */
export function getAllFormValueSimple(moduleIds) {
	if (typeof moduleIds === 'string') {
		if (this.myForm[moduleIds] && this.myForm[moduleIds].state && this.myForm[moduleIds].state.form) {
			//let formData = JSON.parse(JSON.stringify(this.myForm[moduleIds].state.form));
			//formData = setReferValueSimple.call(this, moduleIds, formData);
			let formData = setReferValueSimple.call(this, moduleIds, this.myForm[moduleIds].state.form);
			let data = {
				values: formData
			};
			if (this.myForm[moduleIds]) {
				if (this.myForm[moduleIds].state.status == 'add') {
					data.status = '2';
				} else if (this.myForm[moduleIds].state.status == 'edit') {
					data.status = '1';
				}
				return {
					areaType: 'form',
					rows: [data]
				};
			}
		} else if (this.formSetFormValues[moduleIds] && Object.keys(this.formSetFormValues[moduleIds]).length) {
			//let formData = JSON.parse(JSON.stringify(this.formSetFormValues[moduleIds]));
			let formData = setReferValueSimple.call(this, moduleIds, this.formSetFormValues[moduleIds]);
			let data = {
				values: formData
			};
			if (this.myForm[moduleIds]) {
				if (this.myForm[moduleIds].state.status == 'add') {
					data.status = '2';
				} else if (this.myForm[moduleIds].state.status == 'edit') {
					data.status = '1';
				}
				return {
					areaType: 'form',
					rows: [data]
				};
			} else {
				return {
					areaType: 'form',
					rows: [data]
				};
			}
		} else {
			let data = {
				values: {}
			};
			// 返回值为 rows[0].values.value
			let itemsArr = this.state.meta[moduleIds].items;
			itemsArr.map(item => {
				let initialvalue = {};
				if (item.itemtype === 'checkbox_switch' || item.itemtype === 'switch') {
					initialvalue = { value: false };
				} else if (item.itemtype === 'switch_browse') {
					initialvalue = { value: false };
				} else {
					initialvalue = {};
				}
				data.values[item.attrcode] = {
					value: null,
					...initialvalue
				};
			});
			return {
				areaType: 'form',
				rows: [data]
			};
		}
	} else if (moduleIds instanceof Array) {
		let allFormData = {};
		for (let i = 0; i < moduleIds.length; i++) {
			let id = moduleIds[i];
			if (this.myForm[id] && this.myForm[id].state && this.myForm[id].state.form) {
				//let formData = JSON.parse(JSON.stringify(this.myForm[id].state.form));
				let formData = setReferValueSimple.call(this, id, this.myForm[id].state.form);
				allFormData[id] = {
					areaType: 'form',
					rows: [
						{
							status:
								this.myForm[id] &&
								(this.myForm[id].state.status == 'add'
									? '2'
									: this.myForm[id].state.status == 'edit' ? '1' : null),
							values: formData
						}
					]
				};
			} else if (this.formSetFormValues[id] && Object.keys(this.formSetFormValues[id]).length) {
				//let formData = JSON.parse(JSON.stringify(this.formSetFormValues[id]));
				let formData = setReferValueSimple.call(this, id, this.formSetFormValues[id]);
				allFormData[id] = {
					areaType: 'form',
					rows: [
						{
							status:
								this.myForm[id] &&
								(this.myForm[id].state.status == 'add'
									? '2'
									: this.myForm[id].state.status == 'edit' ? '1' : null),
							values: formData
						}
					]
				};
			} else {
				let data = {
					values: {}
				};
				// 返回值为 rows[0].values.value
				let itemsArr = this.state.meta[id].items;
				itemsArr.map(item => {
					let initialvalue = {};
					if (item.itemtype === 'checkbox_switch' || item.itemtype === 'switch') {
						initialvalue = { value: false };
					} else if (item.itemtype === 'switch_browse') {
						initialvalue = { value: false };
					} else {
						initialvalue = {};
					}
					data.values[item.attrcode] = {
						value: null,
						...initialvalue
					};
				});
				allFormData[id] = {
					areaType: 'form',
					rows: [data]
				};
			}
		}
		return allFormData;
	}
}

/**
 * @description: 简化表单每一项数据
 * @param {String, Object}
 * @return: Object
 */
function setReferValueSimple(moduleId, formData) {
	let newData = {};
	for (let pop in formData) {
		if (!isEmpty(formData[pop].value)) {
			newData[pop] = { value: formData[pop].value };
		} else {
			newData[pop] = {};
		}
	}
	return newData;
}

/**
 * @description: 判断第一个参数是否为空，后面可以传其他【认为是空值】的参数
 * @param {}
 * @return: Boolean
 */
function isEmpty(val, ...rest) {
	if (val === null || val === undefined || rest.find((e) => e == val)) {
		return true;
	}
	return false;
}
```
26、setItemsVisible()：设置表单 显示 隐藏
```
/**
 * @description: 设置表单 显示 隐藏
 * @param {String, Object}
 * @return:
 */
export function setItemsVisible(moduleId, values) {
	// setFormAttribute.call(this,moduleId,values,'visible') 兼容 凭证维护 节点
	if ((!this.myForm[moduleId]) || (!this.myForm[moduleId].state) || (!this.myForm[moduleId].state.visible[moduleId])) {
		if (this.formsetFormVisible && this.formsetFormVisible[moduleId]) {
			if (!this.formsetFormVisible[moduleId]['visible']) {
				this.formsetFormVisible[moduleId]['visible'] = {};
			}
			Object.assign(this.formsetFormVisible[moduleId]['visible'], values);
		}
		return;
	}
	Object.assign(this.myForm[moduleId].state.visible[moduleId], values);


	// 开关打开时直接退出
	if (this.isUpdatePage) return;

	this.myForm[moduleId].setState({
		visible: this.myForm[moduleId].state.visible
	});
}
```
27、setAllFormValueRS()：设置表单全部数据(兼容多语控件,默认把值设置到登录语言上)
```
/**
 * @description: 设置表单全部数据(兼容多语控件,默认把值设置到登录语言上)
 * @param {Object, Boolean, Boolean, Array, Function}
 * @return:
 */
export function setAllFormValueRS(formDataObj, copyFlag = true, emptyOldVal = false, setOldValueExceptKey, callback) {
	//set数据
	for (let moduleId in formDataObj) {
		if (!this.myForm[moduleId] && this.formSetFormValues[moduleId]) {
			Object.keys(this.formSetFormValues[moduleId]).length ? Object.assign(this.formSetFormValues[moduleId], formDataObj[moduleId].rows[0].values) : this.formSetFormValues[moduleId] = formDataObj[moduleId].rows[0].values;
			return;
		}
		let newData = formDataObj[moduleId].rows[0].values;
		// console.log(newData,this.myForm[moduleId].state.verify)
		if (this.myForm[moduleId] && this.myForm[moduleId].state && this.myForm[moduleId].state.form) {
			for (let pop in newData) {
				if (newData[pop] instanceof Array) {
					// 多语 赋值到登录语言 和 主语言上
					if (this.myForm[moduleId].state.residtxt[pop]) {
						for (let key in this.myForm[moduleId].state.residtxt[pop]) {
							if (this.myForm[moduleId].state.residtxt[pop][key] == localeLang && !this.myForm[moduleId].state.form[key]) {
								this.myForm[moduleId].state.form[key] = newData[pop];
							}
						}
					}
					this.myForm[moduleId].state.form[pop] = newData[pop];
					this.formOldValues[moduleId][pop] = newData[pop];
					if (copyFlag) { //取消时,是否取消到当前值---默认取消到当前值
						this.myForm[moduleId].state.formBack[pop] = newData[pop];
					}
				} else {
					// 多语 赋值到登录语言上
					if (this.myForm[moduleId].state.residtxt[pop]) {
						for (let key in this.myForm[moduleId].state.residtxt[pop]) {
							if (this.myForm[moduleId].state.residtxt[pop][key] == localeLang && !this.myForm[moduleId].state.form[key]) {
								this.myForm[moduleId].state.form[key] = newData[pop];
							}
						}
					}
					this.myForm[moduleId].state.form[pop] = { ...newData[pop] };
					if (setOldValueExceptKey && setOldValueExceptKey == pop) {
						continue;
					} else {
						this.formOldValues[moduleId][pop] = { ...newData[pop] };
					}
					if (copyFlag) {
						this.myForm[moduleId].state.formBack[pop] = { ...newData[pop] };
					}
				}
				// verify 置为true
				if (this.myForm[moduleId].state.verify[pop]) {
					this.myForm[moduleId].state.verify[pop].verify = true;
				}
			}
		}
		//重新set值时是否清空旧值---默认不清空旧值
		if (emptyOldVal && this.formOldValues[moduleId]) {
			this.formOldValues[moduleId] = {};
		}

		//开关关闭时才setState
		if (!this.isUpdatePage && this.myForm[moduleId]) {
			//更新
			this.myForm[moduleId].setState({
				form: this.myForm[moduleId].state.form,
				formBack: this.myForm[moduleId].state.formBack,
				verify: this.myForm[moduleId].state.verify
			}, () => {

				//最后一次执行setState时 才回调-----待改
				if (callback && typeof callback === 'function') {
					callback();
				}

			});
		}


	}
}
```
28、setFormItemsValueRS()：设置表单单个值(兼容多语控件,默认设置到登录语言上,待验证)
```
/**
 * @description: 设置表单单个值(兼容多语控件,默认设置到登录语言上,待验证)
 * @param {String, Object, Boolean}
 * @return:
 */
export function setFormItemsValueRS(moduleId, values, cancel = true) {
	if (values && Object.prototype.toString.call(values) === '[object Object]') {
		if (!this.myForm[moduleId]) {
			(this.formSetFormValues[moduleId] && Object.keys(this.formSetFormValues[moduleId]).length) ? Object.assign(this.formSetFormValues[moduleId], values, { cancel }) : this.formSetFormValues[moduleId] = Object.assign({}, values, { cancel });
			return;
		}
		for (let key of Object.keys(values)) {
			if (key) {
				this.myForm[moduleId].state.form[key] = this.myForm[moduleId].state.form[key] || {};
				// 多语 赋值到登录语言上
				if (this.myForm[moduleId].state.residtxt[key]) {
					for (let i in this.myForm[moduleId].state.residtxt[key]) {
						if (this.myForm[moduleId].state.residtxt[key][i] == localeLang && !this.myForm[moduleId].state.form[i]) {
							this.myForm[moduleId].state.form[i] = values[key];
						}
					}
				}
				this.myForm[moduleId].state.form[key] = { ...values[key] };
				//若cancel为false则取消时,该项不会清空
				if (!cancel) {
					this.myForm[moduleId].state.formBack[key] = { ...values[key] };
				}
				//把设置的值存入旧值中
				this.formOldValues[moduleId][key] = { ...values[key] };
				// setFormAttribute.call(this, moduleId, { [key]: true }, 'verify')
				// 设置单个值  verify 置为true    
				if (this.myForm[moduleId].state.verify[key]) {
					this.myForm[moduleId].state.verify[key].verify = true;
				}

			}
		}

		//开关打开时直接退出
		if (this.isUpdatePage) return;

		this.myForm[moduleId].setState({
			form: this.myForm[moduleId].state.form,
			formBack: this.myForm[moduleId].state.formBack,
			verify: this.myForm[moduleId].state.verify
		});
		// this.setState({
		//     meta: this.state.meta
		// })
	}

}

//需要修改，以适配一主多子
/**
 * @description: 设置表单字段某个属性值
 * @param {String, String, String}
 * @return:
 */
function setFormAttribute(moduleId, values, attribute) {
	if (!this.state.meta[moduleId]) {
		if (this.formItemAttrFlag && this.formItemAttrFlag[moduleId]) {
			this.formItemAttrFlag[moduleId][attribute] = {};
			Object.assign(this.formItemAttrFlag[moduleId][attribute], values);
		}
		return;
	}
	if (this.state.meta.formrelation && this.state.meta.formrelation[moduleId]) { //分组表单
		let modid = [moduleId];
		modid = modid.concat(this.state.meta.formrelation[moduleId]);
		for (let key of Object.keys(values)) {
			if (key) {
				for (let i = 0; i < modid.length; i++) {
					let elem = modid[i];
					if (this.state.meta[elem] && this.state.meta[elem].items) {
						let items = this.state.meta[elem].items;
						let item = items.find(function (elem) {
							return elem.attrcode == key;
						});
						if (item) {
							let index = items.indexOf(item);
							this.state.meta[elem].items[index][attribute] = values[key];
						}
					}

				}
			}
		}
	} else { //单个表单
		if (this.state.meta[moduleId] && this.state.meta[moduleId].items) {
			let items = this.state.meta[moduleId].items;
			for (let key of Object.keys(values)) {
				if (key) {
					let item = items.find(function (elem) {
						return elem.attrcode == key;
					});
					if (item) {
						let index = items.indexOf(item);
						this.state.meta[moduleId].items[index][attribute] = values[key];
					}
				}
			}
		}
	}
	this.setState({ meta: this.state.meta });
}
```
29、getResidtxtLang()：根据多语文本 attrcode 获取 当前登录语言的 attrcode
```
/**
 * add by wanglongx @2019/02/16
 * 根据多语文本 attrcode 获取 当前登录语言的 attrcode
 * @param  moduleId String  form的id号
 * @param  attrcode String 字段
 */
export function getResidtxtLang(moduleId, attrcode) {
	let residtxtlang = this.myForm[moduleId].state.residtxt[attrcode];
	for (let k in residtxtlang) {
		if (residtxtlang[k] === localeLang) {
			return k;
		}
	}
	return attrcode;
}
```
30、setFormPopConfirmSwitchTips():设置表单 popconfirmswitch 组件 提示语
```
/** add by wanglongx
 * @description: 设置表单 popconfirmswitch 组件 提示语
 * @param {string} moduleId
 * @param {string} attrcode
 * @param {Array}  content [确定提示, 取消提示]
 * @return:
 */
export function setFormPopConfirmSwitchTips(moduleId, attrcode, content) {
	if (this.myForm[moduleId] && this.myForm[moduleId].state.verify[attrcode]) {
		this.myForm[moduleId].state.verify[attrcode].popConfirmSwitch = [...content];
		this.myForm[moduleId].setState({
			verify: this.myForm[moduleId].state.verify
		});
	}
}
```
31、setFormItemFocus()：设置表单某项获取焦点
```
/**
 * @description: 设置表单某项获取焦点
 * @param {String,String}
 * @return:
 */
export function setFormItemFocus(moduleId, attrcode) {
	if (moduleId && attrcode) {
		PubSub.publish('autoFocus', { data: attrcode });
	}
}
```
32、focusFormItem()：聚焦到表单某一项
```
/**
 * 聚焦到表单某一项
 * @param {*} param0
 * @param {*} formId
 */
export function focusFormItem({ itemtype, attrcode }, formId) {
	focusNextItem({ itemtype, attrcode }, formId);
}
```
33、focusFormNextItem()：聚焦到表单 下一项  formId分为两个主要是为了分组
```
/**
 * 聚焦到表单 下一项  formId分为两个主要是为了分组
 * @param {*} attrcode
 * @param {*} currenFormId
 * @param {*} mainFormId
 */
export function focusFormNextItem(attrcode, currenFormId, mainFormId) {
	console.log(attrcode, currenFormId, this);
	// focusToNext();
	let formComponent = this.myForm[mainFormId || currenFormId] || {};
	let meta = this.state.meta;
	let params = {
		states: formComponent.state,
		meta,
		moduleId: currenFormId,
		attrcode,
		head: mainFormId || currenFormId,
		formrelation: meta && meta.formrelation ? meta.formrelation[mainFormId || currenFormId] : [mainFormId || currenFormId],
		orderOfHotKey: this.orderOfHotKey,
		onLastFormEnter: formComponent.props && formComponent.props.events.onLastFormEnter
	};
	focusToNext(params);
}
```
34、updateDataByRefresh()
35、getCacheDataById()：获取表单缓存数据
```
/**
 * @description: 获取表单缓存数据
 * @param {String}
 * @return: Object
 */
export function getCacheDataById(moduleId) {
	if (this.myForm[moduleId].state.formBack) {
		return this.myForm[moduleId].state.formBack;
	}
}
```
36、getRequiredItems()：获取必输项字段名
```
/**
 * @description: 获取必输项字段名
 * @param {String}
 * @return: Array
 */
function getRequiredItems(moduleId) {
	if (this.state.meta[moduleId]) {
		let itemsArr = [];
		if (this.state.meta.formrelation && this.state.meta.formrelation[moduleId]) { //分组表单
			if (this.state.meta[moduleId] && this.state.meta[moduleId].items) {
				itemsArr = this.state.meta[moduleId].items;
				this.state.meta.formrelation[moduleId].forEach((elem) => {
					if (this.state.meta[elem] && this.state.meta[elem].items) {
						itemsArr = itemsArr.concat(this.state.meta[elem].items);
					}
				});
			}

		} else { //单个表单
			if (this.state.meta[moduleId] && this.state.meta[moduleId].items) {
				itemsArr = this.state.meta[moduleId].items;
			}
		}
		let finalValue = '';
		let regItem = [];
		let maxItem = [];
		let lengthTypes = ['input', 'number', 'textarea'];
		// let residtxtType = ['residtxt']
		itemsArr = itemsArr.filter((item) => item.visible == true);
		let requiredItems = itemsArr
			.filter((ele) => {
				finalValue = this.myForm[moduleId].state.form[ele.attrcode] &&
					this.myForm[moduleId].state.form[ele.attrcode].value;
				let flag = true;
				if (finalValue && ele.reg) {
					flag = ele.reg.test(finalValue);
					ele.reg.lastIndex = 0;
					if (!flag) {
						flag = true;
						regItem.push(ele.attrcode);
					}
				}
				// if (lengthTypes.includes(ele.itemtype) && finalValue && ele.maxlength && finalValue.length > ele.maxlength) {
				// 	flag = false;
				// 	//  maxItem.push(ele.attrcode)
				// }
				// UE 不需要提示给用户
				if (lengthTypes.includes(ele.itemtype) && finalValue && ele.maxlength) {
					// 字符串长度校验  改为不合法提示
					let temValue = String(finalValue);
					// if (ele.itemtype === 'number' && ele.scale != undefined) { temValue = temValue.replace('/[^\x00-\xff]/g', '11'); };
					if (temValue.length > ele.maxlength) {
						maxItem.push(ele.attrcode);
					}
				}
				// space
				if (ele.required && Object.prototype.toString.call(finalValue).slice(8, -1) === 'String') {
					if (/^\s+$/.test(finalValue)) {
						flag = false;
					}
				}
				// restixet
				if (ele.required && ele.itemtype === 'residtxt') {
					if (Object.prototype.toString.call(finalValue).slice(8, -1) === 'String') {
						if (/^\s+$/.test(finalValue)) {
							flag = false;
						}
					}
				}
				// if(residtxtType.includes(ele.itemtype)){
				//     let value = this.myForm[moduleId].state.residtxt[ele.attrcode] && Object.keys(this.myForm[moduleId].state.residtxt[ele.attrcode])[0]
				//     finalValue = this.myForm[moduleId].state.form[value]
				// }
				return (ele.required && !finalValue && finalValue !== 0) || (!flag);
			})
			.map(function (ele) {
				return ele.attrcode;
			});
		return [requiredItems, regItem, maxItem];

	}
}
```
37、updateValuesByRefresh()
```
export function updateValuesByRefresh(moduleId, refreshData, pkname) {
	if (typeof moduleId == 'string' && this.myForm[moduleId] && Array.isArray(refreshData)) {
		let myForm = this.myForm[moduleId];
		let FormData = myForm.state.form;
		let newData = refreshData[0];
		let pknameValue = FormData[pkname] ? (FormData[pkname].value || FormData[pkname].display || FormData[pkname]) : '';
		if (pkname && pknameValue == newData[pkname]) {  // 根据pkname进行更新
			for (let pop in newData) {
				FormData[pop] = {
					...FormData[pop],
					value: newData[pop]
				};
			}
		}
		console.log('update form data by pkname:', pkname);
		myForm.setState({
			form: FormData
		});
	}
}
```
