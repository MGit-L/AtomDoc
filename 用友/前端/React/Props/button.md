<center>按钮</center>

一、描述
二、Simple introduction
1、createButton：
2、setDisabled：
3、getDisabled：
4、setButtonDisabled：设置按钮可用性
5、getButtonDisabled：获取按钮可用性
6、setButtonVisible：设置按钮的显隐性
7、setButtonsVisible：由于之前有个方法名称是setButtonsVisible, 现已把该方法的功能合并到了上一个方法setButtonVisible中, 但为了兼容业务组以前的代码, 这里保留该方法名
8、getButtonVisible：获取按钮可见性
9、hideButtonsByAreas：隐藏所传区域的所有按钮
10、setButtonTitle：设置按钮的中文名
11、getButtons：获取按钮的数据
12、setButtons：设置按钮的数据
13、updateButtons：更新某些区域的按钮
14、setMainButton：设置主要按钮和次要按钮 用来改变按钮的颜色
15、setPopContent：设置操作行确认提示内容框，只对表格操作列中的按钮有效， 当content为空字符串的时候 不提示
16、setUploadDocAmount：设置上传按钮附件数量
17、setUploadConfig：设置按钮为上传按钮
18、createButtonApp：
19、createOprationButton：
20、setOprationBtnsRenderStatus：设置操作行区域是否刷新
21、createErrorButton：
22、createErrorFlag：
23、toggleErrorStatus：
三、function detail introduction
引入
```
import { isObj } from '../../public';
```
0、复用function
```
// 根据 key(id) 从树形嵌套结构中获取按钮
const findItemsByKey = (key, data) => {
    let items = [];
    let process = (tree) => {
        return tree.forEach((item) => {
            if (item.key == key) {
                items.push(item);
            } else if (item.children) {
                process(item.children);
            }
        });
    };
    process(data);
    return items;
};

// 将按钮的自定义属性(visible，disabled，popContent，主要次要按钮)存储在this.myState.btnInfo中，作用是可以在按钮数据请求回来前就可以设置按钮自定义属性
function addCustomAttribute(key, attr, value) {
    let { btnInfo } = this.myState;
    btnInfo[key] = btnInfo[key] || {};
    btnInfo[key][attr] = value;
}

// 将编码为buttonID的按钮的attr属性的值设为value
function setButtonAttribute(buttonId, attr, value) {
    if (typeof buttonId == 'string') {
        // 把按钮的自定义信息存到btnInfo里
        addCustomAttribute.call(this, buttonId, attr, value);
        // 已经有按钮数据，将自定义数据合并到原有的按钮数据中
        let items = findItemsByKey(buttonId, this.myState.buttons);
        items.forEach((item) => {
            item[attr] = value;

            if (attr === 'disabled') {
                if (item.type === 'divider') {
                    item['disabled'] = value;
                } else {
                    item['isenable'] = !value;
                }
            }
            // 把子按钮的visible设为true时,要把子按钮所在的父级同时设为true
            if (attr === 'visible' && item.parentCode) {
                if (value) {
                    setButtonAttribute.call(this, item.parentCode, 'visible', true);
                }
            }
        });
    } else if (buttonId instanceof Array) {
        buttonId.forEach((item) => {
            addCustomAttribute.call(this, item, attr, value);
        });
        for (let i = 0; i < buttonId.length; i++) {
            let items = findItemsByKey(buttonId[i], this.myState.buttons);
            items.forEach((item) => {
                item[attr] = value;

                if (attr === 'disabled') {
                    if (item.type === 'divider') {
                        item['disabled'] = value;
                    } else {
                        item['isenable'] = !value;
                    }
                }
                if (attr === 'visible' && item.parentCode) {
                    if (value) {
                        setButtonAttribute.call(this, item.parentCode, 'visible', true);

                    }
                }
            });
        }
    } else if (isObj(buttonId)) {
        for (let key of Object.keys(buttonId)) {
            addCustomAttribute.call(this, key, attr, buttonId[key]);
            let items = findItemsByKey(key, this.myState.buttons);
            items.forEach((item) => {
                item[attr] = buttonId[key];

                if (attr === 'disabled') {
                    if (item.type === 'divider') {
                        item['disabled'] = buttonId[key];
                    } else {
                        item['isenable'] = !buttonId[key];
                    }
                }
                if (attr === 'visible' && item.parentCode) {
                    if (buttonId[key]) {
                        setButtonAttribute.call(this, item.parentCode, 'visible', true);
                    }
                }
            });
        }
    }
}


function setButtonState(buttonId, callback) {
    // let buttons = this.myState.buttons;
    let buttonsEntrys = this.myState.buttonsEntrys;
    let buttonsComponent = this.myState.buttonsComponent;
    let stopOprationBtnsRenderAreas = this.myState.stopOprationBtnsRenderAreas;
    let refName = [];
    let stopBtnRenderAreas = [];
    for (let item of Object.keys(stopOprationBtnsRenderAreas)) {
        stopOprationBtnsRenderAreas.hasOwnProperty(item) && !stopOprationBtnsRenderAreas[item] && stopBtnRenderAreas.push(item);
    }
    if (Array.isArray(buttonId)) {
        for (let ids of buttonId) {
            if (buttonsEntrys[ids]) {
                for (let id of buttonsEntrys[ids]) {
                    if (!refName.includes(id)) {
                        refName.push(id);
                    }
                }
            }
        }
    }
    refName = refName.filter(item => {
        return !stopBtnRenderAreas.includes(item);
    });
    if (callback) {
        let len = refName.length;
        for (let item of refName) {
            len = len - 1;
            buttonsComponent[item] && buttonsComponent[item].setState({
                json: []
            }, () => {
            });
        }
        if (typeof callback === 'function' && !len) {
            callback && callback();
        }
    } else {
        for (let item of refName) {
            buttonsComponent[item] && buttonsComponent[item].setState({
                json: []
            }, () => {
            });
        }
    }
}


function buttonIdToArr(buttonId) {
    let buttonIds = [];
    if (typeof buttonId == 'string') {
        buttonIds = [buttonId];
    } else if (Array.isArray(buttonId)) {
        for (let it of buttonId) {
            !buttonIds.includes(it) && buttonIds.push(it);
        }
    } else if (isObj(buttonId)) {
        for (let it of Object.keys(buttonId)) {
            !buttonIds.includes(it) && buttonIds.push(it);
        }
    }
    return buttonIds;
}

function areaToArr(areaStr) {
    let areaArr = [];
    if (areaStr.indexOf(',') !== -1) {
        areaArr = areaStr.split(',').map(item => item.trim());
    } else {
        areaArr = [areaStr];
    }
    return areaArr;
}
```
1、createButton：
2、setDisabled：
3、getDisabled：
4、setButtonDisabled：设置按钮可用性
```
// 设置按钮可用性
export function setButtonDisabled(buttonId, flag) {
    // 参数格式 1. ({buttonid:true/false}) 2.('buttonid':true/false) 3.(['buttonid1','buttonid2'],true/false)
    setButtonAttribute.call(this, buttonId, 'disabled', flag);

    if (getButtons.call(this).length) {
        setButtonState.call(this, buttonIdToArr(buttonId));
    }
}
```
5、getButtonDisabled：获取按钮可用性
```
// 获取按钮可用性
export function getButtonDisabled(id) {
    let items = findItemsByKey(id, this.myState.buttons);
    if (items.length === 0) {
        console.error(`找不到编码为 ${id} 的按钮`);
    } else {
        // 相同的 id 的按钮的禁用状态是一致的
        return !!items[0].disabled;
    }
}
```
6、setButtonVisible：设置按钮的显隐性
```
// 设置按钮的显隐性
export function setButtonVisible(buttonId, flag) {
    setButtonAttribute.call(this, buttonId, 'visible', flag);
    if (flag) {
        let items = findItemsByKey(buttonId[0], this.myState.buttons);
        items.forEach((item) => {
            if (item.parentCode) {
                // debugger;
                setButtonAttribute.call(this, item.parentCode, 'visible', true);
            }
        });
    }
    if (getButtons.call(this).length) {
        setButtonState.call(this, buttonIdToArr(buttonId));
    }
}
```
7、setButtonsVisible：由于之前有个方法名称是setButtonsVisible, 现已把该方法的功能合并到了上一个方法setButtonVisible中, 但为了兼容业务组以前的代码, 这里保留该方法名
```
// 由于之前有个方法名称是setButtonsVisible, 现已把该方法的功能合并到了上一个方法setButtonVisible中, 但为了兼容业务组以前的代码, 这里保留该方法名
export let setButtonsVisible = setButtonVisible;
```
8、getButtonVisible：获取按钮可见性
```
// 获取按钮可见性
export function getButtonVisible(id) {
    let items = findItemsByKey(id, this.myState.buttons);
    if (items.length === 0) {
        console.error(`找不到编码为 ${id} 的按钮`);
    } else {
        // 相同的 id 的按钮的禁用状态是一致的
        return !!items[0].visible || !!items[0].enabled;
    }
}
```
9、hideButtonsByAreas：隐藏所传区域的所有按钮
```
//隐藏所传区域的所有按钮
export function hideButtonsByAreas(areas) {
    // 先合并btnInfo中已有的自定义属性，再setState
    if (typeof areas === 'string') {
        this.myState.hideButtonMenu = [areas];
    } else {
        this.myState.hideButtonMenu = areas;
    }
}
```
10、setButtonTitle：设置按钮的中文名
```
// 设置按钮的中文名
export function setButtonTitle(buttonId, flag) {
    setButtonAttribute.call(this, buttonId, 'title', flag);
    if (getButtons.call(this).length) {
        setButtonState.call(this, buttonIdToArr(buttonId));
    }
}
```
11、getButtons：
12、setButtons：设置按钮的数据
```
//设置按钮的数据
export function setButtons(buttons, callback) {
    // 先合并btnInfo中已有的自定义属性，再setState
    let { btnInfo, hideButtonMenu } = this.myState;
    let btnKeys = Object.keys(btnInfo);
    btnKeys.forEach((key) => {
        let items = findItemsByKey(key, buttons);
        items.forEach((item) => {
            if (item.type === 'divider') {
                if (btnInfo[key]) {
                    for (let it of Object.keys(btnInfo[key])) {
                        if (it === 'type') {
                            Object.assign(item, {
                                dividerType: btnInfo[key].type === 'button_main',
                                dropdownType: btnInfo[key].type === 'button_main'
                            });
                        } else {
                            Object.assign(item, { [it]: btnInfo[key][it] });
                        }
                    }

                } else {
                    Object.assign(item, btnInfo[key]);
                }
            } else {
                Object.assign(item, btnInfo[key]);
            }
        });
    });
    this.myState.buttons = buttons;
    if (hideButtonMenu) {
        for (let item of this.myState.buttons) {
            if (hideButtonMenu.includes(item.area)) {
                item.visible = false;
            }
        }
    }
    let keys = [];
    let areas = [];
    let keysOAreas = {};
    if (this.myState.buttons) {
        for (let items of this.myState.buttons) {
            if (items.key) {
                !keys.includes(items.key) && keys.push(items.key);
                if (!keysOAreas.hasOwnProperty(items.key)) {
                    keysOAreas[items.key] = items.area ? [items.area] : [];
                } else {
                    if (!keysOAreas[items.key].includes(items.area)) {
                        keysOAreas[items.key].push(items.area);
                    }
                }

            }

            let areaArr = areaToArr(items.area);
            for (let i of areaArr) {
                !areas.includes(i) && areas.push(i);
            }
            if (items.children) {
                if (items.children && items.children.length) {
                    for (let item of items.children) {
                        if (item.key) {
                            !keys.includes(item.key) && keys.push(item.key);
                            if (!keysOAreas.hasOwnProperty(item.key)) {
                                keysOAreas[item.key] = item.area ? [item.area] : [];
                            } else {
                                if (!keysOAreas[item.key].includes(item.area)) {
                                    keysOAreas[item.key].push(item.area);
                                }
                            }
                        }
                        if (item.children && item.children.length) {
                            for (let it of item.children) {
                                if (it.key) {
                                    !keys.includes(it.key) && keys.push(it.key);
                                    if (!keysOAreas.hasOwnProperty(it.key)) {
                                        keysOAreas[it.key] = items.area ? [it.area] : [];
                                    } else {
                                        if (!keysOAreas[it.key].includes(it.area)) {
                                            keysOAreas[it.key].push(it.area);
                                        }
                                    }
                                }
                                if (it.children && it.children.length) {
                                    for (let i of it.children) {
                                        if (i.key) {
                                            !keys.includes(i.key) && keys.push(i.key);
                                            if (!keysOAreas.hasOwnProperty(i.key)) {
                                                keysOAreas[i.key] = items.area ? [i.area] : [];
                                            } else {
                                                if (!keysOAreas[i.key].includes(i.area)) {
                                                    keysOAreas[i.key].push(i.area);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }
    this.myState.buttonsEntrys = keysOAreas;
    setButtonState.call(this, keys, callback, areas);

}
```
13、updateButtons：更新某些区域的按钮
```
//更新某些区域的按钮
export function updateButtons(buttonsAreas) {
    let buttonsComponent = this.myState.buttonsComponent;

    for (let item of buttonsAreas) {
        buttonsComponent[item] && buttonsComponent[item].setState({
            json: []
        }, () => {
            console.log('update成功');
        });
    }
}
```
14、setMainButton：设置主要按钮和次要按钮 用来改变按钮的颜色
```
// 设置主要按钮和次要按钮 用来改变按钮的颜色
export function setMainButton(buttonId, flag) {
    let MAIN = 'button_main',
        SECONDARY = 'button_secondary';
    if (isObj(buttonId)) {
        for (let key of Object.keys(buttonId)) {
            // 传进来的buttonId[key]值为true或false,转成主要和次要按钮对应的class名
            buttonId[key] = buttonId[key] ? MAIN : SECONDARY;
        }
    }
    let value = flag ? MAIN : SECONDARY;
    setButtonAttribute.call(this, buttonId, 'btncolor', value);
    if (getButtons.call(this).length) {
        setButtonState.call(this, buttonIdToArr(buttonId));
    }
}
```
15、setPopContent：设置操作行确认提示内容框，只对表格操作列中的按钮有效， 当content为空字符串的时候 不提示
```
// 设置操作行确认提示内容框，只对表格操作列中的按钮有效， 当content为空字符串的时候 不提示
export function setPopContent(buttonId, content) {
    setButtonAttribute.call(this, buttonId, 'popContent', content);
    if (getButtons.call(this).length) {
        setButtonState.call(this, buttonIdToArr(buttonId));
    }
}
```
16、setUploadDocAmount：设置上传按钮附件数量
```
// 设置上传按钮附件数量
export function setUploadDocAmount(buttonId, amount) {
    setButtonAttribute.call(this, buttonId, 'docAmount', amount);
    if (getButtons.call(this).length) {
        setButtonState.call(this, buttonIdToArr(buttonId));
    }
}
```
17、setUploadConfig：设置按钮为上传按钮
```
// 设置按钮为上传按钮
export function setUploadConfig(buttonId, config) {
    setButtonAttribute.call(this, buttonId, 'uploadConfig', config);
    if (getButtons.call(this).length) {
        setButtonState.call(this, buttonIdToArr(buttonId));
    }
}
```
18、createButtonApp：
19、createOprationButton：
20、setOprationBtnsRenderStatus：设置操作行区域是否刷新
```
// 设置操作行区域是否刷新
export function setOprationBtnsRenderStatus(areas, flag) {
    for (let item of areas) {
        this.myState.stopOprationBtnsRenderAreas[item] = flag;
    }
}
```
21、createErrorButton：
22、createErrorFlag：
23、toggleErrorStatus：
