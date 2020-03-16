<center>高级查询区</center>

一、描述
二、Simple introduction
1、NCCreateSearch
2、setSearchValue：设置查询区数据
3、setSearchValByField：设置查询区具体某个字段数据
4、getAllSearchData：获取查询区所有数据
5、getSearchValByField：获取表格某个字段的数据
6、setDisabledByField：设置某个查询条件是否可用
7、closeSearchPlanArea：关闭查询方案区域 ，内部使用
8、openAdvSearch：打开/关闭 高级查询面板
9、setDisabled：设置查询区不可编辑
10、clearSearchArea：清空查询区方法
11、getQueryInfo
12、getOprtypeByField：获取查询条件的操作符
13、changeItemVisibleByField：修改条件可见性
14、setRequiredByField：修改查询模板必输性
15、setTemlateByField：修改查询模板属性
三、function detail introduction
引入
```
import moment from 'moment';
import PubSub from 'pubsub-js';
import { NCMessage } from '../../base';
import { isFunction, isArray, isEmpty } from '../../public';
import { timeFunctionTranslater, DongbaToLocalTime, LocalToDongbaTime } from '../../public/timer';
import {
	getTimezoneOffset,
	_clone as deepClone,
	getFunctionValueFromViewModal,
	snapshotChangeByTemplate,
	CompareDate,
	isDateFunction,
	naturalTimeHandler
} from './_methods';
import { _addData } from '../CreateAdvSearch/methods';
import toast from '../../api/toast';

let multiLang = {};
```
0、复用function
```
/* 检测组件id是否存在 */
function checkModuleId(id) {
	if (!this.state.meta.hasOwnProperty(id)) {
		toast({ color: 'danger', content: multiLang['page-search-0043'] });
		return false;
	}
	return true;
}
function checkSearchAreaRenderComplete(id) {
	if (!this.state.meta[id].isCompleted) {
		toast({ color: 'danger', content: '请先初始化查询区,再调用查询区方法' });
		return false;
	}
	return true;
}
/* 获取具体某个field的值 */
function getValueByField(searchId, field, status) {
	let searchInfo = this.state.searchInfo;
	let id = this.props.id;
	let snapshot = searchInfo.snapshotMap[status || searchInfo.status];
	let data = getAllSearchDataEve.call(this, id, snapshot, false);
	if (!data) {
		return { value: { firstvalue: '', secondvalue: '' }, display: '', oprtype: '' };
	}
	// 下面这段不走吧。。
	let len = data.conditions.length;
	if (len <= 0) {
		// {
		// 	toast({ content: `当前查询区没有查询条件`, color: 'warning' });
		// }
		return { value: { firstvalue: '', secondvalue: '' }, display: '', oprtype: '' };
	}
	// ——————
	let res = { value: { firstvalue: '', secondvalue: '' }, display: '', oprtype: '' };

	function getvalue(list, field) {
		list.forEach((item) => {
			if (item.conditions) {
				getvalue(item.conditions, field);
			} else {
				if (item.field === field) {
					res.value = item.value;
					res.display = item.display;
					res.oprtype = item.oprtype;
				}
			}
		});
	}
	getvalue(data.conditions, field);
	return res;
}
// loop set value
function setValue(list, field, data) {
	list.forEach((val) => {
		if (val.children && Array.isArray(val.children)) {
			setValue(val.children);
		} else if (val.attrcode === field) {
			val.initialvalue = data;
		}
	});
}
/* 设值查询区某个字段的值 */
function setSearchValByFieldEve(searchId, field, data, status) {
	// // 新查询用方案区分 status 其实没有用了 simple narmal super 都处理了
	// let arr =['simple', 'normal', 'super'];
	// if (this.state.searchInfo) {
	// 	let searchInfo = this.state.searchInfo;
	// 	// let snapshot = searchInfo.snapshotMap[status || searchInfo.status];
	// 	let searchPlanList = searchInfo.searchPlanCache;
	// 	let currentPk = searchInfo.currentPlan ? searchInfo.currentPlan.pk_ncc_queryscheme : searchInfo.defaultPlan.pk_ncc_queryscheme;
	// 	let currentPlan = searchPlanList.find(item => item.pk_ncc_queryscheme == currentPk)['renderobj4web']
	// 	if (!data) {
	// 		data = {
	// 			value: '',
	// 			display: ''
	// 		};
	// 	}
	// 	arr.map(item => {
	// 		let current = searchInfo.snapshotMap[item]
	// 		if (isEmpty(current)) {
	// 			searchInfo.snapshotCache[item || searchInfo.status][field] = data;
	// 		} else {
	// 			setValue(current, field, data);
	// 		}
	// 	})
	// 	// if (isEmpty(snapshot)) {
	// 	// 	searchInfo.snapshotCache[status || searchInfo.status][field] = data;
	// 	// } else {
	// 	// 	setValue(snapshot, field, data);
	// 	// }
	// 	// 放到当前方案的 cach 当中
	// 	if (!isEmpty(currentPlan)) {
	// 		setValue(currentPlan, field, data);
	// 	}
	// 	this.setState({
	// 		searchInfo: searchInfo
	// 	});
	// } else {
	// 	console.error('setSearchValByField方法没有找到searchId');
	// 	return false;
	// }
	if (this.state.searchInfo) {
		let searchInfo = this.state.searchInfo;
		let snapshot = searchInfo.snapshotMap[status || searchInfo.status];
		if (!data) {
			data = {
				value: '',
				display: ''
			};
		}
		if (isEmpty(snapshot)) {
			searchInfo.snapshotCache[status || searchInfo.status][field] = data;
		} else {
			function setValue(list) {
				list.forEach((val) => {
					if (val.children && Array.isArray(val.children)) {
						setValue(val.children);
					} else if (val.attrcode === field) {
						val.initialvalue = data;
					}
				});
			}
			setValue(snapshot);
			this.setState({
				searchInfo
			});
		}

	} else {
		console.error('setSearchValByField方法没有找到searchId');
		return false;
	}
}
function ArrayHas2Value(value) {
	let result = true;
	if (!Array.isArray(value) || value.length != 2 || isEmpty(value[0]) || isEmpty(value[1])) result = false;
	return result;
}
export function setMultiLang(_multiLang) {
	multiLang = _multiLang;
}

/* 获取查询区 所有数据 */
export function getAllSearchDataEve(id, snapshot, flag) {
	let callBackMeta = [];
	let searchInfo = this.state.searchInfo;

	// 必填校验时 把所有必填校验未通过都标红 	
	let requiredArr = [];



	// 这个for 循环的逻辑太复杂了 没办法 必须把 校验逻辑抽出来
	for (let i = 0; i < snapshot.length; i++) {
		if (snapshot[i].children && Array.isArray(snapshot[i].children)) {
			callBackMeta.push(getAllSearchDataEve.call(this, id, snapshot[i].children, flag));
			continue;
		}
		let attrcode = snapshot[i].attrcode;
		let val = searchInfo.itemsMap[attrcode];
		let VALUE = snapshot[i].initialvalue;
		let displayVal = VALUE.display || '';
		let optSign = snapshot[i].operationSign;
		let isIncludeSub = snapshot[i].runWithChildren || false;
		let refurl = snapshot[i].refurl || '';
		let timeFunction = '';

		if (displayVal && snapshot[i].runWithChildren) {
			displayVal = displayVal + multiLang['page-search-0044'];
		}

		let data = {
			firstvalue: '',
			secondvalue: ''
		};

		if (val.required && flag && snapshot[i].visible) {
			let isKong = false;
			if (isEmpty(VALUE.value) || (Array.isArray(VALUE.value) && !ArrayHas2Value(VALUE.value))) {
				if (optSign != 'is null' && optSign != 'is not null') { isKong = true; }

			}
			if (isKong) {
				callBackMeta = [];
				requiredArr.push(val.attrcode);
			}
		}
		if ((val.itemtype === 'rangepicker' || ((val.itemtype === 'datepicker' || val.itemtype === 'NCTZDatePickerEnd' || val.itemtype === 'NCTZDatePickerStart' || val.itemtype === 'datetimepicker') && optSign === 'between')) && VALUE) {
			let vals = VALUE.value;

			if (vals && !isArray(vals)) {
				vals = vals.split(',');
			}
			//给报表平台加的逻辑，如果是函数，则将函数直接传给后台
			if (isDateFunction(vals[0])) {
				timeFunction = vals.join(',');
			}
			if (vals && isArray(vals)) {
				if (vals.length > 0 && vals[0] && vals[0].format) {
					// 多时区转化
					let start = vals[0].format('YYYY-MM-DD');
					let end = vals[1].format('YYYY-MM-DD');
					// 1 开始时间
					let currentZoneStartTime = moment(start).startOf('day').toDate();
					let currentZoneStartHours = currentZoneStartTime.getHours();
					let offsetZone = currentZoneStartTime.getTimezoneOffset() / 60;
					// 2 结束时间
					let currentZoneEndTime = moment(end).endOf('day').toDate();
					let currentZoneEndHours = currentZoneEndTime.getHours();
					// 当地时间转换为东八区时间
					offsetZone = offsetZone + 8;
					currentZoneStartTime.setHours(currentZoneStartHours + offsetZone);
					currentZoneEndTime.setHours(currentZoneEndHours + offsetZone);
					//   newData.firstvalue = moment(currentZoneStartTime).format('YYYY-MM-DD HH:mm:ss');
					//   newData.secondvalue = moment(currentZoneEndTime).format('YYYY-MM-DD HH:mm:ss');

					data.firstvalue = moment(currentZoneStartTime).format('YYYY-MM-DD HH:mm:ss');
					data.secondvalue = moment(currentZoneEndTime).format('YYYY-MM-DD HH:mm:ss');

					// newVal.push(value.format('YYYY-MM-DD'));
				} else if (vals.length > 0 && vals[0] && !vals[0].format) {
					data.firstvalue = vals[0];
					data.secondvalue = vals[1];
				} else if (vals.length === 0) {
					data.firstvalue = '';
					data.secondvalue = '';
				}
			}
		} else if (val.itemtype === 'refer') {
			VALUE = getFunctionValueFromViewModal(VALUE, this.props.context);
			//参照函数翻译完成之后 有可能 出现翻译不出来的情况 ，翻译不出来 再次做一次必填校验吧
			if (val.required && flag && snapshot[i].visible) {
				if (!VALUE.value && VALUE.value !== 0) {
					callBackMeta = [];
					if (!requiredArr.includes(val.attrcode)) requiredArr.push(val.attrcode);
				}
			}
			if (optSign === 'between') {
				data.firstvalue = (VALUE.value || [])[0] || '';
				data.secondvalue = (VALUE.value || [])[1] || '';
				displayVal = (VALUE.display || []).join(',');
			} else {
				displayVal = VALUE.display;
				data.firstvalue = VALUE.value;
			}
		} else if (
			(val.itemtype === 'select' ||
				val.itemtype === 'number' ||
				val.itemtype === 'datetimepicker' ||
				val.itemtype === 'NCTZDatePickerEnd' ||
				val.itemtype === 'NCTZDatePickerStart') &&
			optSign === 'between'
		) {
			data.firstvalue = (VALUE.value || [])[0];
			data.secondvalue = (VALUE.value || [])[1];
			displayVal = (VALUE.display || [])[0] + '~' + (VALUE.display || [])[1];
		} else {
			data.firstvalue = VALUE.value;
		}
		let timeItemList = [
			'datepicker',
			'datetimepicker',
			'rangepicker',
			'NCTZDatePickerStart',
			'NCTZDatePickerEnd',
			'NCTZDatePickClientTime',
			'NCTZDatePickClientHourTime',
			'NCTZDatePickerRangeDay',
			'datePickerNoTimeZone'
		];
		// 需要从东八区时间处理当地时间，然后计算0点和24点，按照当地时间得0点和24点分别转为东八区时间
		const localToEast = ['datepicker', 'datetimepicker', 'NCTZDatePickClientTime', 'NCTZDatePickClientHourTime'];
		// 开始 和结束时间是时间戳概念  操作符为 = 时不需要进行扩充为日期范围
		const timeStampType = ['NCTZDatePickerStart', 'NCTZDatePickerEnd'];
		if (timeItemList.some((item) => item === val.itemtype) && !isEmpty(VALUE.value)) {
			//xuyangt 需要优化此处逻辑 很混乱
			let firstvalue = '',
				secondvalue = '';
			if (isDateFunction(data.firstvalue) && !data.secondvalue) {
				timeFunction = data.firstvalue;
			}
			if (isDateFunction(data.firstvalue)) {
				if (optSign == '=' || optSign == '<>') {
					// start 取开始时间戳 end时 取结束时间戳
					if (timeStampType.includes(val.itemtype)) {
						if (val.itemtype === 'NCTZDatePickerEnd') {
							firstvalue = timeFunctionTranslater(data.firstvalue, { flag: false });
						} else {
							firstvalue = timeFunctionTranslater(data.firstvalue);
						}
					} else {
						firstvalue = timeFunctionTranslater(data.firstvalue);
						secondvalue = timeFunctionTranslater(data.firstvalue, { flag: false });
					}
				} else if (optSign == '<' || optSign == '>=') {
					firstvalue = timeFunctionTranslater(data.firstvalue);
				} else if (optSign == '>' || optSign == '<=') {
					firstvalue = timeFunctionTranslater(data.firstvalue, { flag: false });
				} else if (optSign == 'between') {
					if (!isEmpty(data.firstvalue)) {
						firstvalue = timeFunctionTranslater(data.firstvalue);
					}
					if (!isEmpty(data.secondvalue)) {
						secondvalue = timeFunctionTranslater(data.secondvalue, { flag: false });
					}
				}


			} else {
				if (optSign == '=' || optSign == '<>') {
					if (!timeStampType.includes(val.itemtype)) {
						// datepicker datepicker 需要处理时区从东八区时间转换为当地时间 计算当天时间[00:00:00, 23:59:59]在转换为东八区时间
						if (localToEast.includes(val.itemtype) && !isDateFunction(data.firstvalue)) {
							firstvalue = DongbaToLocalTime(moment(data.firstvalue)).format('YYYY-MM-DD 00:00:00');
							secondvalue = moment(firstvalue).format('YYYY-MM-DD 23:59:59');

							// 转为东八区时间
							firstvalue = LocalToDongbaTime(moment(firstvalue)).format('YYYY-MM-DD HH:mm:ss');
							secondvalue = LocalToDongbaTime(moment(secondvalue)).format('YYYY-MM-DD HH:mm:ss');
						} else {
							// start end 类型暂未处理
							firstvalue = moment(data.firstvalue).format('YYYY-MM-DD 00:00:00');
							secondvalue = timeFunctionTranslater(moment(data.firstvalue).format('YYYY-MM-DD 23:59:59'));
						}
					} else {
						firstvalue = data.firstvalue;
					}

				} else if (optSign == '<' || optSign == '>=') {
					firstvalue = naturalTimeHandler(data.firstvalue, 'start');
				} else if (optSign == '>' || optSign == '<=') {
					firstvalue = naturalTimeHandler(data.firstvalue, 'end');
				} else if (optSign == 'between') {
					if (!isEmpty(data.firstvalue)) {
						firstvalue = naturalTimeHandler(data.firstvalue, 'start');
					}
					if (!isEmpty(data.secondvalue)) {
						secondvalue = naturalTimeHandler(data.secondvalue, 'end');
					}
				}
				//如果存在两个值，做一下端点值大小的判断和调整
				if (firstvalue && secondvalue && CompareDate(firstvalue, secondvalue)) {
					firstvalue = timeFunctionTranslater(secondvalue);
					secondvalue = timeFunctionTranslater(firstvalue, { flag: false });
				}
			}
			if (optSign == '=') {
				optSign = 'between';
			}
			data.firstvalue = firstvalue;
			data.secondvalue = secondvalue;

		}
		if (!val.hasOwnProperty('queryOperateType')) {
			val.queryOperateType = '';
			console.error(`查询模板中必须要有queryOperateType字段，请检查模板中${val.attrcode}字段`);
		}
		//增加去除前后空格的逻辑 目前 ‘ ’作为例外情况处理
		if (data.firstvalue && typeof data.firstvalue === 'string' && data.firstvalue !== ' ') {
			data.firstvalue = data.firstvalue.trim();
		}
		if (data.secondvalue && typeof data.firstvalue === 'string' && data.secondvalue !== ' ') {
			data.secondvalue = data.secondvalue.trim();
		}
		let Obj = {
			field: attrcode,
			value: data,
			oprtype: optSign,
			display: String(displayVal || '').replace(/~/, ','),
			isIncludeSub,
			refurl,
			datatype: val.datatype || '',
			timeFunction
		};
		if (val.itemtype === 'residtxt') {
			Obj.langSeq = VALUE.index;
		}
		if (!isEmpty(Obj.value.firstvalue) || !isEmpty(Obj.value.secondvalue) || optSign == 'is null' || optSign == 'is not null') {
			snapshot[i].visible && callBackMeta.push(Obj);
		}
	}

	if (requiredArr.length !== 0) {
		{
			toast({ content: multiLang['page-search-0045'], color: 'warning' });
		}
		PubSub.publish('searchRequired', requiredArr);
		return false;
	}


	return {
		logic: 'and',
		conditions: callBackMeta
	};
}

/*设置查询区数据*/
function setSearchValueEve(id, data, cb) {
	let searchInfo = this.state.searchInfo;
	let snapshot = searchInfo.snapshotMap[searchInfo.status];
	let items = snapshot;
	let datas = data;
	if (datas.conditions) {
		datas = datas.conditions;
	}
	if (!isArray(datas)) {
		console.error('setSearchValue方法第二个参数不正确');
		return false;
	}
	const findItem = (item) => {
		let len = items.length;
		while (len--) {
			if (items[len].attrcode === item.field) {
				items[len].initialvalue = items[len].initialvalue || {};
				if (item.oprtype === 'between') {
					items[len].initialvalue.value = [item.value.firstvalue, item.value.secondvalue];
					//items[len].initialvalue.display = [item.value.firstvalue, item.value.secondvalue];
				} else {
					items[len].initialvalue.value = item.value.firstvalue;
					items[len].initialvalue.display = item.display;
				}
				//下面的逻辑是为了处理 反写日期函数时转为具体时间的bug
				if (item.timeFunction) {
					if (item.timeFunction.includes(',')) {
						items[len].initialvalue.value = item.timeFunction.split(',');
					} else {
						items[len].initialvalue.value = item.timeFunction;
						items[len].initialvalue.display = '';
					}
				}
				items[len].operationSign = item.oprtype;
				items[len].runWithChildren = item.isIncludeSub || false;
				items[len].refurl = item.refurl || '';
				break;
			}
		}
	};
	datas.map((item) => {
		findItem(item);
	});
	this.setState(
		{
			searchInfo
		},
		() => {
			typeof cb === 'function' && cb();
		}
	);
}

/* 清空查询区 */
function clearSearchAreaEve(moduleId) {
	let searchInfo = this.state.searchInfo;
	let data = searchInfo.snapshotMap[searchInfo.status];
	let len = data.length;
	while (len--) {
		data[len].initialvalue = {
			value: '',
			display: ''
		};
	}
	this.setState({
		searchInfo
	});
}

/* 设置某个字段是否可用 */
function setDisabledByFieldEve(moduleId, field, status) {
	let meta = this._this.meta.getMeta() || {};

	let data = meta[moduleId].items;
	let len = data.length;
	while (len--) {
		let item = data[len];
		if (item.attrcode === field) {
			item.disabled = status;
			break;
		}
	}
	this._this.meta.setMeta(meta);
}
/* 设置某个字段是否必填*/
function setRequiredByFieldEve(moduleId, field, status) {
	let meta = this._this.meta.getMeta() || {};

	let data = meta[moduleId].items;
	let len = data.length;
	while (len--) {
		let item = data[len];
		if (item.attrcode === field) {
			item.required = !!status;
			break;
		}
	}
	this._this.meta.setMeta(meta);
}
function _setTemlateByField(moduleId, field, property, value) {
	let meta = this._this.meta.getMeta() || {};

	let data = meta[moduleId].items;
	let len = data.length;
	while (len--) {
		let item = data[len];
		if (item.attrcode === field) {
			item[property] = value;
			break;
		}
	}
	this._this.meta.setMeta(meta);

}
function getOprtypeByFieldEve(moduleId, field) {
	let searchInfo = this.state.searchInfo;
	let data = searchInfo.snapshotMap[searchInfo.status];
	function getValue(list, result = []) {
		list.forEach((item) => {
			if (item.children) {
				getValue(item.children, result);
			} else {
				if (item.attrcode === field) {
					result.push(item.operationSign);
				}
			}
		});
		return result;
	}

	return getValue(data);
}
```
0-0、code description
```
/*=======================================以下为输出方法=============================*/
/*
*	因为查询区有3种展示状态，所以需要根据当前状态判断业务组操作的是在哪种状态下进行的。
*
*	@ 规则：
*	1、当高级面板展开 或者 展示查询方案时,取高级面板中的 普通或高级中的数据
*	2、当高级面板收起 并且 不展示查询方案时，取外面简单查询区的值
*
*	@ 状态：
*	高级面板显影性：  			searchInfo.showAdvModal   ( true/false )
*	高级面板当前是普通或高级：  searchInfo.advSearchStatus	（normal/super）
*
* */
```
1、NCCreateSearch
2、setSearchValue：设置查询区数据
```
/* 设置查询区数据 */
export function setSearchValue(moduleId, data, cb) {
	let f = checkModuleId.call(this, moduleId);
	if (!f) return;
	let _this = this.myState.search[`searchArea_${moduleId}`];
	setSearchValueEve.call(_this, moduleId, data, cb);
}
```
3、setSearchValByField：设置查询区具体某个字段数据
```
/* 设置查询区具体某个字段数据 */
export function setSearchValByField(moduleId, field, data, status) {
	let f = checkModuleId.call(this, moduleId);
	if (!f) {
		return;
	}
	let statusList = ['simple', 'plan', 'normal', 'super'];

	if (status && !statusList.some(item => item === status)) {
		status = '';
	}
	let _this = this.myState.search[`searchArea_${moduleId}`];
	setSearchValByFieldEve.call(_this, moduleId, field, data, status);
}
```
4、getAllSearchData：获取查询区所有数据
```
/* 获取查询区所有数据 */
export function getAllSearchData(moduleId, flag = true) {
	let f = checkModuleId.call(this, moduleId);
	if (!f) return;
	let _this = this.myState.search[`searchArea_${moduleId}`];
	let searchInfo = _this.state.searchInfo;
	let snapshot = searchInfo.snapshotMap[searchInfo.status];
	return getAllSearchDataEve.call(_this, moduleId, snapshot, flag);
}
```
5、getSearchValByField：获取表格某个字段的数据
```
/*
*	获取表格某个字段的数据
* */
export function getSearchValByField(moduleId, field, status) {
	let f = checkModuleId.call(this, moduleId);
	if (!f) return;
	let statusList = ['simple', 'plan', 'normal', 'super'];
	if (status && !statusList.some(item => item === status)) {
		status = '';
	}
	let _this = this.myState.search[`searchArea_${moduleId}`];
	return getValueByField.call(_this, moduleId, field, status);
}
```
6、setDisabledByField：设置某个查询条件是否可用
```
/*
* 	设置某个查询条件是否可用
* 	@ field：控件的attrcode
*  	@ status: true可用；false不可用
* */
export function setDisabledByField(moduleId, field, status) {
	let f = checkModuleId.call(this, moduleId);
	if (!f) return;
	let _this = this.myState.search[`searchArea_${moduleId}`];
	setDisabledByFieldEve.call(_this, moduleId, field, status);
}
```
7、closeSearchPlanArea：关闭查询方案区域 ，内部使用
```
/* 关闭查询方案区域 ，内部使用*/
export function closeSearchPlanArea(moduleId) {
	let f = checkModuleId.call(this, moduleId);
	if (!f) return;
	let _this = this.myState.search[`searchArea_${moduleId}`];
	_this.state.searchInfo.status = 'simple';
	_this.setState({
		searchInfo: _this.state.searchInfo
	});
}
```
8、openAdvSearch：打开/关闭 高级查询面板
```
/* 打开/关闭 高级查询面板 */
export function openAdvSearch(moduleId, status, callback) {
	let f = checkModuleId.call(this, moduleId);
	if (!f) return;
	//this.state.meta[moduleId].showAdvModal = status;
	let _this = this.myState.search[`searchArea_${moduleId}`];
	if (status) {
		_this.state.searchAdvBtnClick(_this.state.searchInfo);
	}
	_this.setState({
		searchInfo: _this.state.searchInfo
	}, callback);
}
```
9、setDisabled：设置查询区不可编辑
```
/* 设置查询区不可编辑 */
export function setDisabled(moduleId, status) {
	let f = checkModuleId.call(this, moduleId);
	if (!f) return;
	let data = this.state.meta[moduleId].items;
	let len = data.length;
	while (len--) {
		let item = data[len];
		item.disabled = status;
	}
	this.setState({
		meta: this.state.meta
	});
}
```
10、clearSearchArea：清空查询区方法
```
/* 清空查询区方法 */
export function clearSearchArea(moduleId) {
	let f = checkModuleId.call(this, moduleId);
	if (!f) return;
	let _this = this.myState.search[`searchArea_${moduleId}`];
	clearSearchAreaEve.call(_this, moduleId);
}
```
11、getQueryInfo
```
export function getQueryInfo(moduleId, flag = true) {
	// let _this = this.myState.search[`searchArea_${moduleId}`];
	if (isEmpty(this.state.meta[moduleId])) {
		toast({ content: multiLang['page-search-0046'], color: 'warning' });
		return {};
	} else {
		let data = getAllSearchData.call(this, moduleId, flag);
		if (data) {
			return {
				queryAreaCode: moduleId,
				oid: this.state.meta[moduleId].oid,
				querytype: 'tree',
				querycondition: data,
				nctz: getTimezoneOffset()
			};
		} else {
			return {};
		}

	}
}
```
12、getOprtypeByField：获取查询条件的操作符
```
/* 获取查询条件的操作符 */
export function getOprtypeByField(moduleId, field) {
	let f = checkModuleId.call(this, moduleId);
	if (!f) return;
	let _this = this.myState.search[`searchArea_${moduleId}`];
	return getOprtypeByFieldEve.call(_this, moduleId, field);
}
```
13、changeItemVisibleByField：修改条件可见性
```
/* 修改条件可见性 */
export function changeItemVisibleByField(moduleId, field, status = false) {
	let f = checkModuleId.call(this, moduleId);
	if (!f) return;
	let _this = this.myState.search[`searchArea_${moduleId}`];
	let searchInfo = _this.state.searchInfo;
	let snapshotMap = searchInfo.snapshotMap;
	let context = _this.props.context;
	let items = this.state.meta[moduleId].items;
	items.find((item) => item.attrcode === field).visible = status;
	Object.keys(snapshotMap).forEach((key) => {
		let snapshot = snapshotMap[key];
		if (key == 'super') {
			snapshotMap[key] = _addData.call(_this, items, 'super');
		} else {
			snapshotMap[key] = snapshotChangeByTemplate(snapshot, items, context);
		}

	});
	_this.setState({ searchInfo });
}
```
14、setRequiredByField：修改查询模板必输性
```
/*修改查询模板必输性 */
export function setRequiredByField(moduleId, field, status) {
	let f = checkModuleId.call(this, moduleId);
	if (!f) return;
	let _this = this.myState.search[`searchArea_${moduleId}`];
	setRequiredByFieldEve.call(_this, moduleId, field, status);
}
```
15、setTemlateByField：修改查询模板属性
```
/*修改查询模板属性 */
export function setTemlateByField(moduleId, field, property, value) {
	let f = checkModuleId.call(this, moduleId);
	if (!f) return;
	let _this = this.myState.search[`searchArea_${moduleId}`];
	_setTemlateByField.call(_this, moduleId, field, property, value);
}
```
