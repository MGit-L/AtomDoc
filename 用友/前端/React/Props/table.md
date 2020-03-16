<center>表格</center>

一、描述
二、Simple introduction
1、createSimpleTable
2、getTablePageInfo：获取pageinfo对象的方法
3、setAllTableData：设置表格组件数据并渲染
4、openModel：打开新增model
5、closeModel：关闭新增model方法
6、setTableRender：设置表格某一列的 render
7、setTableValueByKeyAndRecord：设置表格数据
8、hideColByKey：隐藏列通过key
9、showColByKey：显示列通过key
10、getCheckedRows：获取选中的行
11、selectAllRows：选中所有行
12、reverseSelected：反转所有行的选中状态
13、deleteTableRowsByRowId：通过行id删除行
14、deleteTableRowsByIndex：通过行index删除行
15、addRow
16、setTableValueBykey：通过key设置单元格的值
17、setTableValueRequired：通过key设置列是否必输
18、setTableValueDisabled：通过key设置表格列的编辑性
19、focusRowByIndex
20、getAllTableData：获取表格全数据方法
21、setValByKeyAndRowId：根据rowid设置表格某行某个字段值（根据index值）
22、setValByKeyAndIndex：根据行序号设置表格某行某个字段值  0代表第一行  行序号可有可没有
23、updateDataByIndexs：更新多行的数据（根据index值）
24、selectTableRows：设置某些行的选中状态
25、getPks：获取某页的pks
26、getClickRowIndex：获取当前点击行
27、setClickRowIndex：设置当前点击行
28、setColScale
29、checkVisible
30、hasCacheData：判断列表是否有缓存数据
31、deleteCacheId：删除 allpks
32、addCacheId：新增 allpks
33、updateTableData：更新表格某些行数据
34、updateDiffDataByIndex：更新多行的数据（根据index值）
35、updateTableHeight：：刷新表格高度  这是特殊场景使用，一般是表格上方部分，高度变化，表格需要不断适应时使用
36、getSortParam：获取当前表格,排序信息包括当前是多列还是单列排序, 排序的字段,和排序顺序
37、updateDataByRefresh：根据wensocket推送值，更新表格数据
三、function detail introduction
引入
```
/**
 * Table组件的封装
 * 将state沉到组件内部  实验代码
 * 2018/8/22 zhanghengh
 */

import PubSub from 'pubsub-js';
import { warningOnce, isObj, isUndefined, isWrong, isFunction } from '../../public';
//import CacheTools from '../../api/cacheTools';
import { getRandom } from './util';
import { clearSortStatus, insertNewRows, delNewRows } from '../EditTable/util';
import ViewModel from '../../shell/viewmodel/viewmodel';
```
0-0、注释
```
// 页面其他区域变化表格重新计算标识
const OTHERCOMPLETE = 'otherComplete';
/**
 *     ***********      方 法 目 录    ************
 *
 *     PS： 按习惯和常用性排序，新增方法请追加
 *
 *
 *      序号       英文名称                 作用
 *      1         resetTablenumber        当前页码重置为1
 *      2         getTablePageInfo        获取pageinfo对象的方法
 *      3         setAllTableData         设置表格组件数据并渲染
 *      4         openModel               新增功能
 *      5         closeModel              关闭新增的model
 *      6         setTableRender          设置表格某一列的 render
 *      7         setTableColumn          设置表格的列配置
 *      8         setTableColumnMeta      设置表格的列配置
 *      9         getAllTableData         获取表格全数据
 *      10        setTableValueByKeyAndRecord
 *      11        hideColByKey            通过key隐藏列
 *      12        showColByKey            通过key显示列
 *      13        getCheckedRows          获取选中行
 *      14        selectAllRows           选中所有行
 *      15        reverseSelected         反转所有行的选中状态
 *      16        deleteTableRowsByIndex  删除行通过index
 *      17        deleteTableRowsByRowId  删除行通过行id
 *      18        addTableRow             通过index新增行
 *      19        setTableValueBykey      设置值通过键key
 *      20        setTableValueDisabled   通过key设置表格列的编辑性
 *      21        setTableValueRequired   通过key设置表格列是否必输
 *      22        focusTableRowByIndex    把当前index行设置为选中行
 *      23        updateDataByIndexs      更新多行的数据（根据index值）
 *      24        setValByKeyAndRowId     根据rowid设置表格某行某个字段值
 *      25        selectTableRows         设置行多选框的选中状态
 *      26        getPks                  获取某页的pks
 */
```
0-1、more function
```
/**7
 * zhanghengh 设置表格的列配置
 * @param {*} moduleId 表格id
 * @param {*} data
 * mofify
 *  1 zhanghengh @18/05/21 给方法增加注释
 */
export function setTableColumn(moduleId, data) {
  if (!moduleId) return;
  this.setState({
    meta: {
      ...this.state.meta,
      [moduleId]: data
    }
  });
}
/**8
 * zhanghengh 设置表格的列配置
 * @param {*} moduleId 表格id
 * @param {*} data
 * mofify
 *  1 zhanghengh @18/05/21 给方法增加注释
 */
export function setTableColumnMeta(moduleId, data) {
  if (!moduleId) return;
  this.setState({
    meta: {
      ...this.state.meta,
      [moduleId]: data
    }
  });
}
/**18
 * zhanghengh 通过index新增行
 * @param {*} tableId 表格id
 * @param {*} data 新增的行数据 { rows:[] }
 * @param {*} index 行号-1
 * mofify
 *  1 zhanghengh @18/05/21 给方法增加注释
 */
export function addTableRow(tableId, data, index) {
  if (typeof tableId == 'string' && this.state.meta[tableId]) {
    const myCardScope = this.myTable[tableId];
    let tempArr = this.myTable[tableId].state.table.rows;
    let len = tempArr.length;
    let numFlag = index == undefined || (!isNaN(parseInt(index, 10)) && index >= 0 && index <= len);
    if (numFlag) {
      index = index == undefined ? 0 : index;
      let newArr = JSON.parse(JSON.stringify(data.rows));
      newArr = newArr.map(e => {
        e.rowId = e.rowId || String(new Date().getTime()).slice(-5) + Math.random().toString(12);
        return e;
      });
      /**
       * 筛选状态增行, 将就总数据同步正航
        */
      myCardScope.filterAllData && insertNewRows(myCardScope.filterAllData, tempArr, newArr, 0);

      this.myTable[tableId].state.table.rows = [...newArr, ...tempArr];
      // 只有当前页条目数量位10才删除多余项
      this.myTable[tableId].setState(
        {
          table: this.myTable[tableId].state.table
        },
        () => {
          console.log(this.myTable[tableId].state.table);
        }
      );
      return false;
    }
    warningOnce(numFlag, '传入的第三个参数为行序号，不传入默认为行首，否则须为大于等于0且小于等于总行数减1的整数');
    return false;
  }

  warningOnce(false, `所操作的表格中无ID为${tableId}的数据`);
  return false;
}

/**22
 * add by zhanghengh @18/05/9
 * 设置当前行
 * @param  tableId   meta的id号
 * @param  index     index 行序号-1
 */
export function focusTableRowByIndex(tableId, index) {
  // 目前是单选
  this.myTable[tableId].state.table.currentIndex = index;
  this.myTable[tableId].setState({
    table: this.myTable[tableId].state.table
  });
}
```
1、createSimpleTable
2、getTablePageInfo：获取pageinfo对象的方法
```
/**2
 * zhanghengh 获取pageinfo对象的方法
 * @param {*} moduleId 表格id
 * mofify
 *  1 zhanghengh @18/05/21 给方法增加注释
 */
export function getTablePageInfo(moduleId) {
  if (typeof moduleId == 'string') {
    if (this.myTable[moduleId]) {
      let { pageIndex = 0, pageSize = 10 } = this.myTable[moduleId].state.table.pageInfo;
      return {
        pageIndex: pageIndex > 0 ? pageIndex - 1 : 0,
        pageSize
      };
    } else {
      let { pageIndex = 0, pageSize = 10 } = this.myTableData[moduleId].pageInfo;
      return {
        pageIndex: pageIndex > 0 ? pageIndex - 1 : 0,
        pageSize
      };
    }
  }
  return { pageIndex: 0, pageSize: 10 };
}
```
3、setAllTableData：设置表格组件数据并渲染
```
/**3
 * zhanghengh 设置表格组件数据并渲染
 * @param {*} moduleId 表格id
 * @param {*} data 数据
 * @param {*} isTop 数据是否置顶
 * mofify
 *  1 zhanghengh @18/05/21 给方法增加注释
 */
export function setAllTableData(moduleId, data, isTop = true) {
  let tmp = {
    pageInfo: {},
    rows: [],
    model: null,
    origin: null,
    operType: null,
    checkedAll: false,
    indeterminate: false
  };
  if (data.hasOwnProperty('pageInfo') && !isWrong(data.pageInfo)) {
    // 前端重置后端返回的页码，初次为0，应该改为1，有问题在看看
    data.pageInfo.pageIndex = String(Number(data.pageInfo.pageIndex) + 1);
  } else if (data.rows.length == 0) {
    data.pageInfo = {
      pageSize: '10',
      pageIndex: '1',
      total: '0',
      totalPage: '1'
    };
  } else {
    if (
      this.myTable[moduleId] &&
      Array.isArray(this.myTable[moduleId].state.table.allpks) &&
      this.myTable[moduleId].state.table.allpks.length > 0
    ) {
      data.pageInfo = this.myTable[moduleId].state.table.pageInfo;
    } else {
      data.pageInfo = {
        pageSize: '10',
        pageIndex: '1'
      };
    }
  }

  data.rows = data.rows.map(e => {
    e.rowId = e.rowId || String(new Date().getTime()).slice(-5) + Math.random().toString(12);
    return e;
  });

  if (data.hasOwnProperty('allpks')) {
    // 存储到本地缓存中，卡片翻页需要用
    //CacheTools.set('allpks', data.allpks);
  } else if (data.rows.length == 0) {
    data.allpks = [];
    //CacheTools.set('allpks', data.allpks);
  } else {
    if (
      this.myTable[moduleId] &&
      Array.isArray(this.myTable[moduleId].state.table.allpks) &&
      this.myTable[moduleId].state.table.allpks.length > 0
    ) {
      data.allpks = this.myTable[moduleId].state.table.allpks;
    } else {
      data.allpks = [];
    }
  }
  let conData = JSON.parse(JSON.stringify({ ...tmp, ...data }));

  // 将排序标记清空
  if (this.state.meta[moduleId]) {
    clearSortStatus({ colums: this.state.meta[moduleId].items });
  }
  // 重新设置数据模版滚动条定位到顶部
  if (isTop) {
    conData.focusIndex = 0;
  }
  if (this.myTable[moduleId]) {
    const myTableScope = this.myTable[moduleId];
    /**
         * 处理筛选状态下的情况
         * 清空的表格筛选标志
         * 清空筛选状态下的表格全数据
         * 退出筛选状态
         */
    if (myTableScope.filterAllData) {
      myTableScope.FilterPanelCallBack.current.handleClearFliterStatus();
    }
    // 解决组件销毁后，再次赋值，赋到了原来缓存的组件上，导致新的组件没有值的问题
    this.myTableData[moduleId] = conData;
    this.myTable[moduleId].setState(
      {
        table: conData
      },
      () => {
        this.myTable[moduleId].state.table.focusIndex = -1;
      }
    );
  } else {
    this.myTableData[moduleId] = conData;
  }
}
```
4、openModel：打开新增model
```
/**4
 * zhanghengh 打开新增model
 * @param {*} moduleId 表格id
 * @param {*} type 操作类型  编辑或者新增
 * @param {*} record
 * @param {*} index 行号-1
 * mofify
 *  1 zhanghengh @18/05/21 给方法增加注释
 */
export function openModel(moduleId, type, record, index) {
  if (!moduleId) return;
  let data = JSON.parse(JSON.stringify(this.myTable[moduleId].state.table));
  data.model = true;
  if (type == 'edit') {
    data.origin = record;
    data.rowIndex = index || null;
  } else if (type == 'add') {
    data.origin = null;
    data.rowIndex = null;
  }
  data.operType = type;
  this.myTable[moduleId].setState({
    table: data,
    tableModeldata: record || {}
  });
}
```
5、closeModel：关闭新增model方法
```
/**5
 * zhanghengh 关闭新增model方法
 * @param {*} moduleId 表格id
 * mofify
 *  1 zhanghengh @18/05/21 给方法增加注释
 */
export function closeModel(moduleId) {
  let data = JSON.parse(JSON.stringify(this.myTable[moduleId].state.table));
  data.model = false;

  this.myTable[moduleId].setState({
    table: data
  });
}
```
6、setTableRender：设置表格某一列的 render
```
/**6
 * zhanghengh 设置表格某一列的 render
 * @param {*} moduleId 表格id
 * @param {*} key
 * @param {*} render
 * mofify
 *  1 zhanghengh @18/05/21 给方法增加注释
 */
export function setTableRender(moduleId, key, render) {
  if (this.state.meta[moduleId]) {
    let obj = this.state.meta[moduleId].items.find(function (elem) {
      return elem.attrcode == key;
    });
    let index = this.state.meta[moduleId].items.indexOf(obj);
    this.state.meta[moduleId].items[index].render = render;
    this.setState({
      meta: this.state.meta
    });
  }
}
```
7、setTableValueByKeyAndRecord：设置表格数据
```
/**10
 * zhanghengh 设置表格数据
 * @param {*} moduleId 表格id
 * @param {*} record
 * @param {*} dist
 * mofify
 *  1 zhanghengh @18/05/21 给方法增加注释
 */
export function setTableValueByKeyAndRecord(moduleId, record, dist) {
  // TODO
  let newRecord = { ...record, ...dist };
  let id = record.attrcode.value;
  let rows = this.myTable[moduleId].state.table.rows;
  rows.map(item => {
    if (item.values.attrcode && id == item.values.attrcode.value) {
      item.values = { ...newRecord };
    }
  });

  let data = this.myTable[moduleId].state.table;
  // data.rows = rows;

  this.myTable[moduleId].setState({
    table: data
  });
}
```
8、hideColByKey：隐藏列通过key
```
/**11
 * zhanghengh 隐藏列通过key
 * @param {*} moduleId 表格id
 * @param {*} key 属性
 * mofify
 *  1 zhanghengh @18/05/21 给方法增加注释
 */
export function hideColByKey(moduleId, key) {
  if (this.state.meta[moduleId]) {
    this.state.meta[moduleId].items.map(function (elem) {
      if (elem.attrcode == key && elem.visible) {
        elem.visible = false;
      }
    });
    this.setState({
      meta: this.state.meta
    });
  }
}
```
9、showColByKey：显示列通过key
```
/**12
 * zhanghengh 显示列通过key
 * @param {*} moduleId 表格id
 * @param {*} key 属性
 * mofify
 *  1 zhanghengh @18/05/21 给方法增加注释
 */
export function showColByKey(moduleId, key) {
  if (this.state.meta[moduleId]) {
    this.state.meta[moduleId].items.map(function (elem) {
      if (elem.attrcode == key && !elem.visible) {
        elem.visible = true;
      }
    });
    this.setState({
      meta: this.state.meta
    });
  }
}
```
10、getCheckedRows：获取选中的行
```
/**13
 * zhanghengh 获取选中的行
 * @param {*} moduleId 表格id
 * mofify
 *  1 zhanghengh @18/05/21 给方法增加注释
 */
export function getCheckedRows(moduleId) {
  let consArr = [];
  const myTableScope = this.myTable[moduleId];
  if (myTableScope) {
    let rows = getAllTableData.call(this, moduleId, false).rows;
    // this.myTable[moduleId].state.table.rows.map((item, index) => {
    rows.map((item, index) => {
      if (item.selected) {
        consArr.push({
          data: item,
          index
        });
      }
    });
  } else {
    this.myTableData[moduleId] && this.myTableData[moduleId].rows.map((item, index) => {
      if (item.selected) {
        consArr.push({
          data: item,
          index
        });
      }
    });
  }

  return consArr;
}
```
11、selectAllRows：选中所有行
```
/**14
 * zhanghengh 选中所有行
 * @param {*} moduleId 表格id
 * @param {*} checked true或false
 * mofify
 *  1 zhanghengh @18/05/21 给方法增加注释
 */
export function selectAllRows(moduleId, checked) {
  this.myTable[moduleId].state.table.checkedAll = checked;
  if (!this.myTable[moduleId].state.table.checkedAll) {
    this.myTable[moduleId].state.table.indeterminate = false;
  }
  let rows = getAllTableData.call(this, moduleId, false).rows;
  let len = rows.length;
  for (let i = 0; i < len; i++) {
    rows[i].selected = checked;
  }
  // 勾选行颜色选中
  let myTable = this.myTable[moduleId].state.table;
  if (myTable.checkedAll) {
    for (let i = 0; i < len; i++) {
      // 全选将需要添加样式的行数组填满
      if (!myTable.currentIndex || myTable.currentIndex == -1) {
        myTable.currentIndex = [];
      } else {
        if (!Array.isArray(myTable.currentIndex)) {
          myTable.currentIndex = [myTable.currentIndex];
        }
      }
      myTable.currentIndex.push(i);
    }
  } else {
    myTable.currentIndex = [];
  }
  this.myTable[moduleId].setState({
    table: myTable
  });
}
```
12、reverseSelected：反转所有行的选中状态
```
/**15
 * zhanghengh 反转所有行的选中状态
 * @param {*} moduleId 表格id
 * mofify
 *  1 zhanghengh @18/05/21 给方法增加注释
 */
export function reverseSelected(moduleId) {
  let rows = getAllTableData.call(this, moduleId, false).rows;
  rows.forEach(element => {
    element.selected = !element.selected;
  });
  let len = rows.length;
  // 如果有一个备选 哪个开关为开，同时看是否全选，
  while (len--) {
    if (rows[len].selected) {
      this.myTable[moduleId].state.table.indeterminate = true;
      break;
    } else {
      this.myTable[moduleId].state.table.indeterminate = false;
    }
  }
  this.myTable[moduleId].state.table.checkedAll = rows.every(
    item => !!item.selected
  );
  // 勾选行颜色选中
  let myTable = this.myTable[moduleId].state.table;
  if (myTable.checkedAll) {
    for (let i = 0; i < len; i++) {
      // 全选将需要添加样式的行数组填满
      if (!myTable.currentIndex || myTable.currentIndex == -1) {
        myTable.currentIndex = [];
      } else {
        if (!Array.isArray(myTable.currentIndex)) {
          myTable.currentIndex = [myTable.currentIndex];
        }
      }
      myTable.currentIndex.push(i);
    }
  } else {
    myTable.currentIndex = [];
  }
  this.myTable[moduleId].setState({
    table: myTable
  });
}
```
13、deleteTableRowsByRowId：通过行id删除行
```
/**17
 * zhanghengh 通过行id删除行
 * @param {*} tableId 表格id
 * @param {*} rowId 行id
 * mofify
 *  1 zhanghengh @18/05/21 给方法增加注释
 */
export function deleteTableRowsByRowId(tableId, rowId) {
  if (typeof tableId == 'string' && this.myTable[tableId]) {
    if (typeof rowId == 'string') {
      let arr = this.myTable[tableId].state.table.rows;
      const myTableScope = this.myTable[tableId];
      let caheIndex = -1,
        myTable = myTableScope.state.table;

      myTable.rows = arr.filter((item, index) => {
        if (item.rowId === rowId) {
          // 更新筛选状态下的全数据
          myTableScope.filterAllData && delNewRows(myTableScope.filterAllData, myTable.rows[index]);
          caheIndex = index;
        }
        return item.rowId !== rowId;
      });

      // 删除自动选中到下一个行的逻辑 , 与快捷键的的删除逻辑冲突 by bbqin
      if (caheIndex >= 0 && caheIndex == myTable.currentIndex) {
        myTable.currentIndex = -1;
      }
      this.myTable[tableId].setState({
        table: myTable
      });
      return false;
    } else {
      warningOnce(typeof rowId == 'string', '传入的第二个参数为rowId，字符串');
      return false;
    }
  }

  warningOnce(false, `所操作的表格中无ID为${tableId}的数据`);
  return false;
}
```
14、deleteTableRowsByIndex：通过行index删除行
```
/**16
 * zhanghengh 通过行index删除行
 * @param {*} tableId 表格id
 * @param {*} index 索引  行号-1
 * mofify
 *  1 zhanghengh @18/05/21 给方法增加注释
 */
export function deleteTableRowsByIndex(tableId, index) {
  let myTableScope = this.myTable[tableId];
  if (typeof tableId == 'string' && myTableScope) {
    let myTable = myTableScope.state.table;
    let rows = myTable.rows;
    let numFlag = Array.isArray(index) || !isNaN(parseInt(index, 10));
    let deleteNum = 0;
    if (numFlag) {
      if (Array.isArray(index)) {
        index.forEach(item => {
          deleteNum++;
          myTableScope.filterAllData && delNewRows(myTableScope.filterAllData, myTable.rows[item]);
          delete rows[item];
        });
        // 删除自动选中到下一个行的逻辑 , 与快捷键的的删除逻辑冲突 by bbqin
        if (index.indexOf(myTable.currentIndex) !== -1) {
          myTable.currentIndex = -1;
        }
      } else if (!isNaN(parseInt(index, 10))) {
        deleteNum++;
        myTableScope.filterAllData && delNewRows(myTableScope.filterAllData, myTable.rows[index]);
        rows.splice(index, 1);
        // 删除自动选中到下一个行的逻辑 , 与快捷键的的删除逻辑冲突 by bbqin
        if (index >= 0 && index == myTable.currentIndex) {
          myTable.currentIndex = -1;
        }
      }
      myTable.rows = rows.filter(item => !!item);
      // 设值时回显之前的多选结果
      let len = myTable.rows.length;
      while (len--) {
        if (myTable.rows[len].selected) {
          myTable.indeterminate = true;
          break;
        } else {
          myTable.indeterminate = false;
        }
      }
      myTable.checkedAll = myTable.rows.every(item => !!item.selected);
      // 这个判断是同步删除后的总行数，确保分页正确, 可能不需要家、加
      // if (
      //   myTable.pageInfo &&
      //   myTable.pageInfo.total
      // ) {
      //   let pageInfo = myTable.pageInfo;
      //   pageInfo.total -= deleteNum;
      //   pageInfo.totalPage = Math.ceil(pageInfo.total / pageInfo.pageSize);
      // }
      myTableScope.setState({
        table: myTable
      });
      return false;
    }
    warningOnce(numFlag, '传入的第二个参数为行index值，可以是数字组成的数组或者单个数字');
    return false;
  }
  warningOnce(false, `所操作的表格中无ID为${tableId}的数据`);
  return false;
}
```
15、addRow
16、setTableValueBykey：通过key设置单元格的值
```
/**19
 * zhanghengh 通过key设置单元格的值
 * @param {*} moduleId 表格id
 * @param {*} key 属性
 * @param {*} data 替换的数据
 * mofify
 *  1 zhanghengh @18/05/21 给方法增加注释
 */
export function setTableValueBykey(tableId, key, data, type) {
  // TODO 问问亚军 data是对象还是value
  if (type == 'refer') {
    this.myTable[tableId].state.tableModeldata[key] = {
      display: data.refname,
      value: data.refpk
    };
  } else {
    this.myTable[tableId].state.tableModeldata[key] = {
      value: data,
      display: null
    };
  }

  this.myTable[tableId].setState({
    tableModeldata: this.myTable[tableId].state.tableModeldata
  });
}
```
17、setTableValueRequired：通过key设置列是否必输
```
/**21
 * zhanghengh 通过key设置列是否必输
 * @param {*} tableId 表格id
 * @param {*} key  属性
 * @param {*} flag true 或 false
 * mofify
 *  1 zhanghengh @18/05/21 给方法增加注释
 */
export function setTableValueRequired(tableId, key, flag) {
  if (this.state.meta[tableId]) {
    this.state.meta[tableId].items.map(function (elem) {
      if (elem.attrcode == key) {
        elem.required = !!flag;
      }
    });
    this.setState({
      meta: this.state.meta
    });
  }
}
```
18、setTableValueDisabled：通过key设置表格列的编辑性
```
/**20
 * zhanghengh 通过key设置表格列的编辑性
 * @param {*} moduleId 表格id
 * @param {*} key  属性
 * @param {*} flag true 或 false
 * mofify
 *  1 zhanghengh @18/05/21 给方法增加注释
 */
export function setTableValueDisabled(tableId, key, flag) {
  if (this.state.meta[tableId]) {
    this.state.meta[tableId].items.map(function (elem) {
      if (elem.attrcode == key) {
        elem.disabled = !!flag;
      }
    });
    this.setState({
      meta: this.state.meta
    });
  }
}
```
19、focusRowByIndex
20、getAllTableData：获取表格全数据方法
```
/**9
 * zhanghengh 获取表格全数据方法
 * @param {*} moduleId 表格id
 * mofify
 *  1 zhanghengh @18/05/21 给方法增加注释
 */
export function getAllTableData(moduleId, flag = true) {
  if (!moduleId) return;
  const myTableScope = this.myTable[moduleId];
  let data = null;
  if (myTableScope) {
    data = myTableScope.filterAllData ? { ...myTableScope.state.table, rows: myTableScope.filterAllData } : myTableScope.state.table;
  } else {
    data = this.myTableData[moduleId];
  }
  return flag ? JSON.parse(JSON.stringify(data)) : data;
  // return JSON.parse(
  //   JSON.stringify(this.myTable[moduleId] ? this.myTable[moduleId].state.table : this.myTableData[moduleId])
  // );
}
```
21、setValByKeyAndRowId：根据rowid设置表格某行某个字段值（根据index值）
```
/**24
 * add by zhanghengh@18/05/29
 * 根据rowid设置表格某行某个字段值（根据index值）
 * @param  tableId   meta的id号
 * @param  record    插入的数据数组
 */
export function setValByKeyAndRowId(tableId, rowid, key, { value, display, scale }) {
  if (typeof tableId == 'string' && this.state.meta[tableId]) {
    if (typeof rowid == 'string') {
      let metaArr = this.state.meta[tableId].items.map(item => item.attrcode);
      if (metaArr.includes(key)) {
        let row = this.myTable[tableId].state.table.rows;
        row.find(item => {
          let _isObj = isObj(item.values[key]);
          if (!_isObj) {
            item.values[key] = {};
          }
          if (item.rowId == rowid) {
            !isUndefined(value) && (item.values[key].value = value);
            !isUndefined(display) && (item.values[key].display = display);
            !isUndefined(scale) && (item.values[key].scale = scale);
          }
        });
        this.myTable[tableId].setState({
          table: this.myTable[tableId].state.table
        });
        return false;
      }
      warningOnce(metaArr.includes(key), '传入的第三个参数为表头键值，须是meta中字段');
      return false;
    }
    warningOnce(typeof rowid == 'string', '传入的第二个参数为rowid，须为字符串');
    return false;
  }

  warningOnce(false, `所操作的表格中无ID为${tableId}的数据`);

  return false;
}
```
22、setValByKeyAndIndex：根据行序号设置表格某行某个字段值  0代表第一行  行序号可有可没有
```
/**30
 * modify by zhanghehng @18/08/024
 * 根据行序号设置表格某行某个字段值  0代表第一行  行序号可有可没有
 * @param  tableId   meta的id号
 * @param  index     行序号，从0开始
 * @param  key       columns的键值
 * @param  value     需要设置的value值
 * @param  display   需要设置的display值
 * @param  scale     需要设置的scale值
 */
export function setValByKeyAndIndex(tableId, index, key, { value, display, scale, isEdit }) {
  if (typeof tableId == 'string' && this.state.meta[tableId]) {
    let allRows = this.myTable[tableId].state.table.rows.length - 1;
    if (+index >= 0 && +index <= allRows) {
      let num = parseInt(index, 10);
      this.myTable[tableId].state.table.rows[num].rowid += Math.random().toString(5);
      let metaArr = this.state.meta[tableId].items.map(item => item.attrcode);
      if (metaArr.includes(key)) {
        let _isObj = isObj(this.myTable[tableId].state.table.rows[num].values[key]);
        if (!_isObj) {
          this.myTable[tableId].state.table.rows[num].values[key] = {};
        }
        !isUndefined(value) && (this.myTable[tableId].state.table.rows[num].values[key].value = value);
        !isUndefined(display) && (this.myTable[tableId].state.table.rows[num].values[key].display = display);
        !isUndefined(scale) && (this.myTable[tableId].state.table.rows[num].values[key].scale = scale);
        !isUndefined(isEdit) && (this.myTable[tableId].state.table.rows[num].values[key].isEdit = isEdit);
        this.myTable[tableId].setState({
          table: this.myTable[tableId].state.table
        });
        return false;
      }
      warningOnce(metaArr.includes(key), '传入的第三个参数为表头键值，须是meta中字段');
      return false;
    }
    warningOnce(+index >= 0 && +index <= allRows + 1, '传入的第二个参数为行序号，第一行从0开始');
    return false;
  }

  warningOnce(false, `所操作的表格中无ID为${tableId}的数据`);
  return false;
}
```
23、updateDataByIndexs：更新多行的数据（根据index值）
```
/**23
 * add by zhanghengh@18/05/22  暂时注释掉，后续继续完善 暂时还有问题，之后接着改
 * 更新多行的数据（根据index值）
 * @param  tableId   meta的id号
 * @param  record    插入的数据数组
 * mofify 18/10/10 更更改了多选框的问题，update之后将半选和全选都去掉了
 */
export function updateDataByIndexs(tableId, record) {
  if (typeof tableId == 'string' && this.myTable[tableId].state.table) {
    let testDataType = Array.isArray(record);
    if (testDataType) {
      let table = this.myTable[tableId].state.table;
      let tableData = getAllTableData.call(this, tableId, false).rows;
      let checkedAll = true;
      let indeterminate = false;
      record.map(item => {
        let { index, data } = item;
        if (isObj(data)) {
          let rowId = getRandom();
          tableData[index] = { ...data, rowId };
        }
      });
      // let table = {
      //     ...this.myTable[tableId].state.table,
      //     rows: tableData
      //   };
      //判断勾选
      let rows = table.rows;
      let len = rows.length;
      while (len--) {
        if (rows[len].selected) {
          indeterminate = true;
        } else {
          checkedAll = false;
        }
      }
      table.checkedAll = checkedAll;
      table.indeterminate = indeterminate;
      this.myTable[tableId].setState({
        table
      });
      return false;
    }
    warningOnce(testDataType, '传入的第二个参数是数组，格式为：[{index: 0, data: { values: {} }, ...]');
    return false;
  }
  warningOnce(false, '第一个参数必须为字符串');
  return false;
}
```
24、selectTableRows：设置某些行的选中状态
```
/**25
 * 设置某些行的选中状态
 * add by  zhangheng 18/06/12
 * @param {*} tableId 表格id
 * @param {*} index 行索引   数组或者数字
 * @param {*} flag 是否选中 true 或false
 * @param {*} isCallBack 是否执行回掉 true 或false
 */
export function selectTableRows(tableId, index, flag, isCallBack = false) {
  const that = this;
  const cloneTable = this.myTable[tableId].state.table;
  if (typeof tableId == 'string' && cloneTable && cloneTable.rows.length > 0) {
    if (Array.isArray(index) || (cloneTable.rows.length - 1 >= index && index >= 0)) {
      if (!Array.isArray(index)) {
        index = [index];
      }
      let isFlag = true;
      index.forEach(eve => {
        if (cloneTable.rows.length - 1 >= eve && eve >= 0) {
          cloneTable.rows[eve].selected = flag;
          isFunction(cloneTable.onSelected) &&
            isCallBack &&
            cloneTable.onSelected(
              { ...that.props, ...that.output },
              tableId,
              cloneTable.rows[eve],
              eve,
              cloneTable.rows[eve].selected
            );
        } else {
          isFlag = false;
          warningOnce(false, '所传入的数组中，数值不符合要求');
        }
      });
      if (isFlag) {
        let checkedRowIndexArr = [];
        getCheckedRows.call(this, tableId).forEach(item => {
          checkedRowIndexArr.push(item.index);
        });
        if (flag === true) {
          if (
            Array.from(new Set(index)).length === cloneTable.rows.length ||
            Array.from(new Set(index.concat(checkedRowIndexArr))).length === cloneTable.rows.length
          ) {
            cloneTable.checkedAll = true;
            cloneTable.indeterminate = false;
          } else {
            cloneTable.checkedAll = false;
            cloneTable.indeterminate = true;
          }
        } else {
          if (Array.from(new Set(index)).length === cloneTable.rows.length) {
            cloneTable.checkedAll = false;
            cloneTable.indeterminate = false;
          } else {
            let arr = checkedRowIndexArr.slice();
            checkedRowIndexArr.forEach((value, indexx) => {
              if (index.includes(value)) {
                arr.splice(indexx, 1, null);
              }
            });
            // 标记
            let sign = false;
            arr.forEach(ele => {
              // 如果该数组中，有一个值不是null,说明还有勾选存在
              if (ele !== null) {
                sign = true;
              }
            });
            if (sign) {
              cloneTable.checkedAll = false;
              cloneTable.indeterminate = true;
            } else {
              cloneTable.checkedAll = false;
              cloneTable.indeterminate = false;
            }
          }
        }
        // 处理勾选行选中样式逻辑
        if (flag) {
          if (!cloneTable.currentIndex || cloneTable.currentIndex == -1) {
            cloneTable.currentIndex = [];
          } else {
            if (!Array.isArray(cloneTable.currentIndex)) {
              cloneTable.currentIndex = [cloneTable.currentIndex];
            }
          }
          if (Array.isArray(index)) {
            cloneTable.currentIndex = [...cloneTable.currentIndex, ...index];
          } else {
            cloneTable.currentIndex.push(index);
          }
        } else {
          if (Array.isArray(cloneTable.currentIndex)) {
            if (Array.isArray(index)) {
              cloneTable.currentIndex = cloneTable.currentIndex.filter((rowIndex) => {
                return !index.includes(rowIndex);
              });
            } else {
              cloneTable.currentIndex = cloneTable.currentIndex.filter(rowIndex => rowIndex !== index);
            }
          }
        }
        this.myTable[tableId].setState({
          table: cloneTable
        });
        return true;
      } else {
        return false;
      }
    }
    warningOnce(false, '所传入的第二个参数不符合要求');
    return false;
  }
  warningOnce(false, `所操作的表格中无ID为${tableId}的数据`);
  return false;
}
```
25、getPks：获取某页的pks
```
/**26
 * 获取某页的pks  这里还有问题接着改
 * add by  zhangheng 18/06/26
 * @param {*} tableId 表格id
 * @param {*} pageIndex  当前页 第一页为1开始
 * @param {*} pageSize  每页条数
 */
export function getPks(tableId, pageIndex, pageSize) {
  if (typeof tableId === 'string' && this.myTable[tableId].state.table) {
    let {
      pageInfo: { pageIndex: currentPageIndex, pageSize: currentPageSize },
      allpks = []
    } = this.myTable[tableId].state.table;
    let pks = [];
    let index = Number(isUndefined(pageIndex) ? currentPageIndex : pageIndex);
    let size = Number(isUndefined(pageSize) ? currentPageSize : pageSize);
    let start = (index - 1) * size;
    for (let i = start; i < start + size; i++) {
      if (allpks[i]) {
        pks.push(allpks[i]);
      } else {
        break;
      }
    }
    return pks;
  }
}
```
26、getClickRowIndex：获取当前点击行
```
/**29
 * 获取当前点击行
 * add by  zhangheng 18/07/04
 * @param {*} tableId 表格id
 */
export function getClickRowIndex(tableId) {
  return this.myTable[tableId].state.table.currentkInfo;
}
```
27、setClickRowIndex：设置当前点击行
```
/**28
 * 设置当前点击行
 * add by  zhangheng 18/07/04
 * @param {*} tableId 表格id
 * index  索引
 */
export function setClickRowIndex(tableId, data) {
  if (this.myTable[tableId]) {
    this.myTable[tableId].state.table.currentkInfo = data;
  }
}
```
28、setColScale
29、checkVisible
30、hasCacheData：判断列表是否有缓存数据
```
/**
 * 判断列表是否有缓存数据
 */
export function hasCacheData(dataSource) {
  if (dataSource) {
    let mcData = ViewModel.getData(dataSource);
    if (mcData && mcData.simpleTable && mcData.simpleTable.rows && mcData.simpleTable.rows.length) {
      return true;
    }
  }
  return false;
}
```
31、deleteCacheId：删除 allpks
```
/**
 * 删除 allpks
 *
 */
export function deleteCacheId(tableId, pkvalue) {
  if (tableId && this.myTable[tableId] && this.myTable[tableId].state && this.myTable[tableId].state.table) {
    let { allpks } = this.myTable[tableId].state.table;
    if (Array.isArray(allpks) && allpks.length) {
      if (typeof pkvalue === 'string') {
        allpks = allpks.filter(item => {
          return item != pkvalue;
        });
      }

      if (Array.isArray(pkvalue) && pkvalue.length) {
        allpks = allpks.filter(item => {
          return !pkvalue.includes(item);
        });
      }

      this.myTable[tableId].state.table.allpks = allpks;
      this.myTable[tableId].setState({
        table: this.myTable[tableId].state.table
      });
    }
  }
}
```
32、addCacheId：新增 allpks
```
/**
 * 新增 allpks
 */
export function addCacheId(tableId, pkvalue) {
  if (tableId && this.myTable[tableId] && this.myTable[tableId].state && this.myTable[tableId].state.table) {
    let { allpks } = this.myTable[tableId].state.table;
    if (Array.isArray(allpks)) {
      if (!allpks.includes(pkvalue)) {
        allpks.push(pkvalue);
      }
      this.myTable[tableId].state.table.allpks = allpks;
      this.myTable[tableId].setState({
        table: this.myTable[tableId].state.table
      });
    }
  }
}
```
33、updateTableData：更新表格某些行数据
```
/**
 * add by zhanghengh @18/07/8  有问题过来改
 * 27、更新表格某些行数据
 * @param  tableId   meta的id号
 * @param  data      后台返回的data 需要有rowId 删除的数据不要返回
 */
export function updateTableData(tableId, data) {
  if (typeof tableId == 'string') {
    let checkData = isObj(data) && Array.isArray(data.rows);
    if (checkData) {
      // 处理rowId
      let allRows = getAllTableData.call(this, tableId, false).rows;
      // this.myTable[tableId].state.table.rows.forEach(item => {
      allRows.forEach(item => {
        for (let row of data.rows) {
          if (item.rowId == row.rowId) {
            // 状态为 1，2用返回的0代替
            Object.keys(item).forEach(key => {
              if (!isUndefined(row[key])) {
                item[key] = row[key];
              } else {
                delete item[key];
              }
            });
            // 检测不用修改rowId是否重绘制 TODO
            item.rowId = getRandom();
          }
        }
      });
      // 更新重新设置总多选框的状态
      const rows = this.myTable[tableId].state.table.rows;
      const selectedAll = rows.every(row => {
        return row.selected;
      });
      this.myTable[tableId].state.table.indeterminate = false;
      this.myTable[tableId].state.table.checkedAll = false;
      if (selectedAll) {
        this.myTable[tableId].state.table.checkedAll = true;
      } else if (
        rows.filter(row => {
          return row.selected;
        }).length
      ) {
        this.myTable[tableId].state.table.checkedAll = false;
        this.myTable[tableId].state.table.indeterminate = true;
      }
      this.myTable[tableId].setState({
        table: this.myTable[tableId].state.table
      });
      return false;
    }
    warningOnce(checkData, '传入的第二个参数为所设置的数据，数据格式是对象，且有个rows属性，rows的内容是数组');
    return false;
  }

  warningOnce(false, '第一个参数必须为字符串');
  return false;
}
```
34、updateDiffDataByIndex：更新多行的数据（根据index值）
```
/**31
 * 王策加
 * 更新多行的数据（根据index值）
 * @param  tableId   meta的id号
 * @param  record    插入的数据数组
 * mofify 18/10/10 更更改了多选框的问题，update之后将半选和全选都去掉了
 */
export function updateDiffDataByIndex(tableId, data) {
  let myTable = this.myTable[tableId];
  if (typeof tableId == 'string' && myTable) {
    let table = myTable.state.table;
    // let rows = table.rows;
    let rows = getAllTableData.call(this, tableId, false).rows;
    let testDataType = Array.isArray(data);
    if (testDataType) {
      data.map(item => {
        let { index, values } = item;
        if (isObj(values)) {
          Object.keys(values).forEach(key => {
            rows[index].values[key] = { ...rows[index].values[key], ...values[key] };
          });
        }
      });
      myTable.setState({
        table
      });
      return false;
    }
    warningOnce(testDataType, '传入的第二个参数是数组，格式为：[{index: 0, data: { values: {} }, ...]');
    return false;
  }

  warningOnce(false, '第一个参数必须为字符串');

  return false;
}
```
35、updateTableHeight：刷新表格高度  这是特殊场景使用，一般是表格上方部分，高度变化，表格需要不断适应时使用
```
/**32
 * 共享  郭扬让加的，他们现在再用
 * 刷新表格高度  这是特殊场景使用，一般是表格上方部分，高度变化，表格需要不断适应时使用
 */
export function updateTableHeight() {
  PubSub.publish(OTHERCOMPLETE, true);
}
```
36、getSortParam：获取当前表格,排序信息包括当前是多列还是单列排序, 排序的字段,和排序顺序
```
/**33
 * 获取当前表格,排序信息包括当前是多列还是单列排序, 排序的字段,和排序顺序
 * 这是产品李聪慧让加的, 为了解决单据打印时，列表对中字段排序产生的问题
 * add by  zhangheng 19/8/13
 * @param {*} tableId 表格id
 */
export function getSortParam(tableId) {
  return this.myTable[tableId].state.table.sortParam;
}
```
37、updateDataByRefresh：根据wensocket推送值，更新表格数据
```
/**34
 *  根据wensocket推送值，更新表格数据
 * refreshData
 */

export function updateDataByRefresh(tableId, pkname, refreshData, saga_errormesg) {
  if (typeof tableId == 'string' && this.myTable[tableId] && Array.isArray(refreshData)) {
    let myTable = this.myTable[tableId];
    let table = myTable.state.table;
    let rows = table.rows;
    let refreshLen = refreshData.length;
    rows.map((item, index) => {
      let values = item.values;
      for (let i = 0; i < refreshLen; i++) {
        //找到相应行,更新对应字段的value
        if (values[pkname] && values[pkname].value === refreshData[i][pkname]) {
          for (let pop in refreshData[i]) {
            if (values[pop]) {
              values[pop].value = refreshData[i][pop];
            } else {
              values[pop] = { value: refreshData[i][pop] }
            }

            //有错误信息时，将错误信息数据放表格行
            if (pop === "saga_status" && refreshData[i][pop] === "1" && saga_errormesg) {
              values.saga_errormesg = saga_errormesg;
            }

          }

          break;

        }
      }
      return item;
    })

    myTable.setState({
      table
    });
  }
}
```
