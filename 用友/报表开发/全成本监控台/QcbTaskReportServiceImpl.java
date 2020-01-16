package nc.impl.qcb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import nc.bd.framework.db.CMSqlBuilder;
import nc.bs.dao.BaseDAO;
import nc.hr.utils.InSQLCreator;
import nc.impl.pubapp.pattern.data.bill.BillQuery;
import nc.itf.qcb.IQcbTaskReportService;
import nc.jdbc.framework.processor.BeanListProcessor;
import nc.jdbc.framework.processor.ResultSetProcessor;
import nc.pub.smart.data.DataSet;
import nc.pub.smart.metadata.Field;
import nc.pub.smart.metadata.MetaData;
import nc.vo.org.LiabilityBookVO;
import nc.vo.pub.BusinessException;
import nc.vo.qcb.qcbworklist.AggQcbworklist;
import nc.vo.qcb.qcbworklist.Qcbworklist;
import nc.vo.qcb.taskclassification.Taskclass;
import nc.vo.qcb_workbench.qcbworkbench.AggWorkbench;
import nc.vo.qcb_workbench.qcbworkbench.Pk_task_b;
import nc.vo.qcb_workbench.qcbworkbench.Workbench;

import org.apache.commons.lang.StringUtils;

/**
 * 全成本监控台报表接口实现类
 * @author liweiat
 * @time 2019-11-30 13:30:00
 */
public class QcbTaskReportServiceImpl implements IQcbTaskReportService {
	
	public final String CONTROLMODE_CONTROL = "1";// 控制方式：严格控制
	public final String CONTROLMODE_NOT_CONTROL = "2";// 控制方式：不控制
	
	public final String FINISH = "1";// 完成状态：1完成
	public final String NOT_FINISH = "2";// 完成状态：2未完成
	
	public final String TASKATTRIBUTE_BEFORE = "1";// 任务属性：1前置任务
	public final String TASKATTRIBUTE_AFTER = "2";// 任务属性：2后置任务
	
	public final String TASK_STATUS_FINISH = "2";// 完成状态
	public final String TASK_STATUS_NOT_EXECUTABLE = "3";// 不可执行状态
	public final String TASK_STATUS_EXECUTABLE = "4";// 可执行状态

	private final BaseDAO baseDAOManager = new BaseDAO();
	
	/**
	 * 1、设置语义模型的元数据
	 * 2、数据转换成报表需要的二维结构
	 * @param pk_group 集团
	 * @param pk_accperiod 会计期间
	 * @param pk_accountingbooks 责任核算账簿数组
	 * @return
	 * @throws BusinessException
	 */
	public DataSet makeDataSet(String pk_group, String pk_accperiod, String... pk_accountingbooks) throws BusinessException {
		AggWorkbench[] bills = getAggWorkbench(pk_accperiod, pk_accountingbooks);
		DataSet result = new DataSet();
		if (null == bills || bills.length <= 0) {
			return result;
		}
		
		// 设置语义模型的元数据
		result.setMetaData(makeMetaData());
		// 获取到该集团下所有的责任核算账簿
		Map liabilityMap = getLiabilityBookVO(pk_group);
		// 获取到处理过的数据
		HashMap<String, HashMap<String, String>> resultMap = getTwoDimensionalMap(bills);
		// 获取所有的工作任务分类
		List<HashMap<String, String>> taskclassList = getTaskclassVO();
		
		Object[][] datas = getTwoDimensionalDatas(taskclassList, resultMap,
				liabilityMap, pk_accperiod, pk_accountingbooks);

		result.setDatas(datas);
		
		return result;
	}
	
	/**
	 * 通过责任核算账簿、会计期间查询全成本聚合对象数组
	 * @param pk_accperiod
	 * @param pk_accountingbooks
	 * @return
	 * @throws BusinessException
	 */
	private AggWorkbench[] getAggWorkbench(String pk_accperiod,
			String[] pk_accountingbooks) throws BusinessException {
		if (StringUtils.isEmpty(pk_accperiod) || null == pk_accountingbooks 
				|| pk_accountingbooks.length <= 0) {
			return null;
		}
		// 将责任核算账簿存放到临时表中
		InSQLCreator inSQLCreator = new InSQLCreator();
		String condition = inSQLCreator.getInSQL(pk_accountingbooks);
		// 责任核算账簿、会计期间作为查询条件查询所有的全成本聚合vo数据
		String sql = " select PK_TASK from qcb_workbench where pk_accountingbook in ("+condition+") and pk_accperiod ='"+pk_accperiod+"' ";
		
		String[] pks = this.queryPKS(sql, "PK_TASK");
		if (pks == null || pks.length == 0) {
			return null;
		}
		
		BillQuery<AggWorkbench> billQuery = new BillQuery<AggWorkbench>(AggWorkbench.class);
		AggWorkbench[] result = billQuery.query(pks);
		return result;
	}
	
	/**
	 * 通过sql语句查询全成本pk数组
	 * @param sql
	 * @param pk
	 * @return
	 * @throws BusinessException
	 */
	private String[] queryPKS(String sql,final String pk) throws BusinessException{
		try {
			return (String[]) baseDAOManager.executeQuery(sql,
					new ResultSetProcessor() {
						private static final long serialVersionUID = 1L;

						@Override
						public Object handleResultSet(ResultSet rs)
								throws SQLException {
							List<String> pks = new ArrayList<String>();
							while (rs.next()) {
								pks.add(rs.getString(pk));
							}
							return pks.toArray(new String[0]);
						}
					});
		} catch (Exception e) {// 封闭历史错误异常继承关系
			throw new BusinessException("查询出错", e);
		}
	}

	/**
	 * 将所有的任务组装成二维数组
	 * @param taskclassList 工作分类列表
	 * @param resultMap 数据
	 * @param liabilityMap 责任核算账簿
	 * @param pk_accperiod 会计期间
	 * @param pk_accountingbooks 责任核算账簿数组
	 * @return
	 */
	private Object[][] getTwoDimensionalDatas(List<HashMap<String, String>> taskclassList,
			HashMap<String, HashMap<String, String>> resultMap,
			Map liabilityMap, String pk_accperiod, String[] pk_accountingbooks) {
		if (null == taskclassList || taskclassList.size() <= 0) {
			return null;
		}
		int row_task = taskclassList.size();
		int row_book = pk_accountingbooks.length;
		int row = pk_accountingbooks.length*row_task;// 行数
		Object[][] datas = new Object[row][6];
		for (int i = 0; i < row_book; i++) {
			// 责任核算账簿
			String pk_accountingbook = pk_accountingbooks[i];
			// 责任核算账簿名称
			LiabilityBookVO bookVO = (LiabilityBookVO) liabilityMap.get(pk_accountingbook);
			// 根据责任核算账簿获取任务状态
			HashMap<String, String> dataMap = resultMap.get(pk_accountingbook);
			
			for (int j = 0; j < row_task; j++) {
				// 会计期间
				int colu = 0;
				datas[i*row_task+j][colu] = pk_accperiod;
				colu++;
				// 责任核算账簿
				datas[i*row_task+j][colu] = pk_accountingbook;
				colu++;
				// 责任核算账簿名称
				datas[i*row_task+j][colu] = bookVO.getName();
				colu++;
				// 工作分类
				HashMap<String, String> taskMap = taskclassList.get(j);
				datas[i*row_task+j][colu] = taskMap.get("pk_taskclass");
				colu++;
				// 工作分类名称
				datas[i*row_task+j][colu] = taskMap.get("task_name");
				colu++;
				// 任务状态
				String pk_taskclass = taskMap.get("pk_taskclass");
				if (null != dataMap && StringUtils.isNotEmpty(dataMap.get(pk_taskclass))) {
					String task_status = dataMap.get(pk_taskclass);
					datas[i*row_task+j][colu] = task_status;
				} else {
					// 未完成态
					datas[i*row_task+j][colu] = "0";
				}
			}
		}
		
		return datas;
	}
	
	/**
	 * 1、将所有的工作任务进行判断其任务状态，并组装成map
	 * 2、map按照责任核算账簿，工作任务分类区别
	 * @param bills 所有的任务数据
	 * @return 责任核算账簿[key]-(工作任务分类[key]-任务状态[value])[value]
	 */
	private HashMap<String, HashMap<String, String>> getTwoDimensionalMap(
			AggWorkbench[] bills) throws BusinessException {
		HashMap<String, HashMap<String, String>> voMap = new HashMap<String, HashMap<String, String>>();
		int billLen = bills.length;
		
		// 根据全成本信息关联查询工作清单信息（获取控制方式）
		Map<String, String> qcbMap = getQcbworklistMap(bills);
		// 根据全成本信息关联查询子表前置任务完成状态
		Map<String, String> taskChildMap = getTaskChildren(bills);
		for (int i = 0; i < billLen; i++) {
			// 责任核算账簿
			String pk_accountingbook = bills[i].getParentVO().getPk_accountingbook();
			// 根据责任核算账簿获取所有的任务分类状态map
			HashMap<String, String> dataMap = voMap.get(pk_accountingbook);
			if (null != dataMap) {
				funcTaskStatusWith_voMap(bills[i], voMap, qcbMap, taskChildMap, dataMap);
			} else {
				funcTaskStatusWithout_voMap(bills[i], voMap, qcbMap, taskChildMap);
			}
		}
		return voMap;
	}

	/**
	 * 判断其任务分类状态：需要根据map里的值进行判断，再确定是否存放新的任务状态
	 * 1、任务分类是可执行状态--直接return
	 * 2、任务分类是不可执行状态--判断是否存在可执行状态的任务
	 * 3、任务分类是完成状态--判断是否1和2的任务
	 * @param aggBill 全成本工作台聚合VO对象
	 * @param voMap 组装的map：责任核算账簿[key]-(工作任务分类[key]-任务状态[value])[value]
	 * @param qcbMap 根据全成本信息关联查询工作清单信息（获取控制方式）
	 * @param taskChildMap 根据全成本信息关联查询子表前置任务完成状态
	 * @param dataMap 根据责任核算账簿获取所有的任务分类状态map
	 */
	private void funcTaskStatusWith_voMap(AggWorkbench aggBill, 
			HashMap<String, HashMap<String, String>> voMap, Map<String, String> qcbMap, 
			Map<String, String> taskChildMap, HashMap<String, String> dataMap) {
		Workbench workbench = aggBill.getParentVO();
		// 工作任务分类
		String pk_taskclass = workbench.getPk_taskclass();
		
		// dataMap里的工作任务pk对应的任务状态
		String data_task_status = dataMap.get(pk_taskclass);
		
		// 可执行状态:只要有一个任务是可执行状态，任务分类就是可执行状态
		if (TASK_STATUS_EXECUTABLE.equals(data_task_status)) {
			return;
		}
		
		// 查询控制方式
		String pk_tasklist = workbench.getPk_tasklist();
		String controlmode = null;
		if (null != qcbMap) {
			controlmode = qcbMap.get(pk_tasklist);
		}
		
		// 不可执行状态
		if (TASK_STATUS_NOT_EXECUTABLE.equals(data_task_status)) {
			// 控制方式:1、严格控制 2、不控制
			if (CONTROLMODE_CONTROL.equals(controlmode)) {
				funcTaskStatusWith_Pk_task_b(aggBill, dataMap, voMap, taskChildMap);
			}
		} else {
			funcTaskStatusWithout_voMap(aggBill, voMap, qcbMap, taskChildMap);
		}
	}

	/**
	 * 判断其任务分类状态：第一次直接将判断的任务状态放置map中
	 * @param aggBill 全成本工作台聚合VO对象
	 * @param voMap 组装的map：责任核算账簿[key]-(工作任务分类[key]-任务状态[value])[value]
	 * @param qcbMap 根据全成本信息关联查询工作清单信息（获取控制方式）
	 * @param taskChildMap 根据全成本信息关联查询子表前置任务完成状态
	 */
	private void funcTaskStatusWithout_voMap(AggWorkbench aggBill, 
			HashMap<String, HashMap<String, String>> voMap, Map<String, String> qcbMap, 
			Map<String, String> taskChildMap) {
		Workbench workbench = aggBill.getParentVO();
		// 执行责任核算账簿
		String pk_accountingbook = workbench.getPk_accountingbook();
		// 工作任务分类
		String pk_taskclass = workbench.getPk_taskclass();
		
		HashMap<String, String> dataMap = new HashMap<String, String>();
		if (FINISH.equals(workbench.getFishstatus())) {
			// 完成态
			putVoMap(pk_taskclass, TASK_STATUS_FINISH, pk_accountingbook, dataMap, voMap);
			return;
		}
		
		// 查询控制方式
		String pk_tasklist = workbench.getPk_tasklist();
		String controlmode = null;
		if (null != qcbMap) {
			controlmode = qcbMap.get(pk_tasklist);
		}
		if (CONTROLMODE_CONTROL.equals(controlmode)) {
			funcTaskStatusWith_Pk_task_b(aggBill, dataMap, voMap, taskChildMap);
		} else {
			// 可执行状态:不控制
			putVoMap(pk_taskclass, TASK_STATUS_EXECUTABLE, pk_accountingbook, dataMap, voMap);
		}
	}
	
	/**
	 * 判断任务是否为不可执行状态：
	 * 1、控制方式是严格控制，其所有前置任务的完成状态＝未完成，其启动状态为不可执行
	 * @param aggBill 全成本工作台聚合VO对象
	 * @param dataMap 根据责任核算账簿获取所有的任务分类状态map
	 * @param voMap 组装的map：责任核算账簿[key]-(工作任务分类[key]-任务状态[value])[value]
	 * @param taskChildMap 根据全成本信息关联查询子表前置任务完成状态
	 */
	private void funcTaskStatusWith_Pk_task_b(AggWorkbench aggBill, HashMap<String, String> dataMap, 
			HashMap<String, HashMap<String, String>> voMap, Map<String, String> taskChildMap){
		Workbench workbench = aggBill.getParentVO();
		// 执行责任核算账簿
		String pk_accountingbook = workbench.getPk_accountingbook();
		// 工作任务分类
		String pk_taskclass = workbench.getPk_taskclass();
		
		Pk_task_b[] taskChildren = (Pk_task_b[]) aggBill.getChildren(Pk_task_b.class);
		if (null == taskChildren || taskChildren.length <= 0) {
			// 可执行状态:没有任务
			putVoMap(pk_taskclass, TASK_STATUS_EXECUTABLE, pk_accountingbook, dataMap, voMap);
			return;
		}
		int childLen = taskChildren.length;
		boolean childFinishFlag = true;// 所有前置任务完成态
		boolean childTaskattrFlag = false;// 是否存在前置任务
		for (int j = 0; j < childLen; j++) {
			Pk_task_b taskChild = taskChildren[j];
			// 前置任务
			if (TASKATTRIBUTE_BEFORE.equals(taskChild.getTaskattribute())) {
				childTaskattrFlag = true;
				String pk_task_b = taskChild.getPk_task_b();
				String taskB_Finish = taskChildMap.get(pk_task_b);
				if (NOT_FINISH.equals(taskB_Finish)) {
					continue;
				}
				childFinishFlag = false;
			}
		}
		if (childTaskattrFlag && childFinishFlag) {
			// 不可执行状态:控制方式是严格控制，其所有前置任务的完成状态＝未完成，其启动状态为不可执行
			putVoMap(pk_taskclass, TASK_STATUS_NOT_EXECUTABLE, pk_accountingbook, dataMap, voMap);
		} else {
			// 可执行状态
			putVoMap(pk_taskclass, TASK_STATUS_EXECUTABLE, pk_accountingbook, dataMap, voMap);
		}
	}
	
	/**
	 * 处理VoMap
	 * @param pk_taskclass 工作任务分类
	 * @param taskclass_status 工作任务分类状态
	 * @param pk_accountingbook 责任核算账簿
	 * @param dataMap 工作任务分类[key]-工作任务分类状态[value]
	 * @param voMap 组装的map：责任核算账簿[key]-(工作任务分类[key]-任务状态[value])[value]
	 */
	private void putVoMap(String pk_taskclass, String taskclass_status, String pk_accountingbook,
			HashMap<String, String> dataMap, HashMap<String, HashMap<String, String>> voMap){
		dataMap.put(pk_taskclass, taskclass_status);
		voMap.put(pk_accountingbook, dataMap);
	}

	/**
	 * 根据全成本信息关联查询子表前置任务完成状态
	 * @param bills
	 * @return 前置任务主键（key）-前置任务完成状态（value）
	 * 1、完成 2、未完成
	 */
	private Map<String, String> getTaskChildren(AggWorkbench[] bills) throws BusinessException{
		Map<String, String> result = new HashMap<String, String>();
		
		int billLen = bills.length;
		Set<String> paraSet = new TreeSet<String>();
		for (int i = 0; i < billLen; i++) {
			AggWorkbench aggWork = bills[i];
			Pk_task_b[] taskBs = (Pk_task_b[]) aggWork.getChildren(Pk_task_b.class);
			if (null == taskBs || taskBs.length <= 0) {
				continue;
			}
			int childBLen = taskBs.length;
			for (int j = 0; j < childBLen; j++) {
				Pk_task_b taskChild = taskBs[j];
				// 前置任务
				if (TASKATTRIBUTE_BEFORE.equals(taskChild.getTaskattribute())) {
					String pk_task_b = taskChild.getPk_task_b();
					String pk_bench = taskChild.getPk_bench();
					// 目前存放：前置任务主键（key）-前置任务对应任务主键（value）
					result.put(pk_task_b, pk_bench);
					paraSet.add(pk_bench);
				}
			}
		}
		
		if (paraSet.size() <= 0) {
			return result;
		}
		
		String[] pk_benchs = paraSet.toArray(new String[paraSet.size()]);
		InSQLCreator inSQLCreator = new InSQLCreator();
		String condition = inSQLCreator.getInSQL(pk_benchs);
		
		CMSqlBuilder finSql = new CMSqlBuilder();
		finSql.select();
		finSql.append(" * ");
		finSql.from(" qcb_workbench ");
		finSql.where();
		finSql.append(" pk_task in ("+condition+") ");
		ArrayList<Workbench> wbList = (ArrayList<Workbench>) baseDAOManager.executeQuery(
				finSql.toString(), new BeanListProcessor(Workbench.class));
		
		if (wbList.size() <= 0) {
			return result;
		}
		
		int wbLen = wbList.size();
		Map<String, String> wbMap = new HashMap<String, String>();
		for (int i = 0; i < wbLen; i++) {
			Workbench wb = wbList.get(i);
			wbMap.put(wb.getPk_task(), wb.getFishstatus());
		}
		
		for (String key : result.keySet()) {
			result.put(key, wbMap.get(result.get(key)));
		}
		
		return result;
	}

	/**
	 * 根据全成本信息关联查询工作清单信息（获取控制方式）
	 * @param bills 全成本信息
	 * @return
	 * @throws BusinessException
	 */
	private Map<String, String> getQcbworklistMap(AggWorkbench[] bills) throws BusinessException{
		int billLen = bills.length;
		List<String> pk_worklist = new ArrayList<String>();
		for (int i = 0; i < billLen; i++) {
			AggWorkbench aggBill = bills[i];
			Workbench workbench = aggBill.getParentVO();
			String pk_tasklist = workbench.getPk_tasklist();
			pk_worklist.add(pk_tasklist);
		}
		
		BillQuery<AggQcbworklist> billQuery = new BillQuery<AggQcbworklist>(AggQcbworklist.class);
		AggQcbworklist[] aggQcbworklists = billQuery.query(pk_worklist.toArray(new String[0]));
		if (null == aggQcbworklists || aggQcbworklists.length <= 0) {
			return null;
		}
		
		int qcbLen = aggQcbworklists.length;
		Map<String, String> resultMap = new HashMap<String, String>();
		for (int i = 0; i < qcbLen; i++) {
			AggQcbworklist aggQcb = aggQcbworklists[i];
			Qcbworklist qcbworklist = aggQcb.getParentVO();
			String pk_task = qcbworklist.getPk_worklist();
			String controlmode = qcbworklist.getControlmode();
			resultMap.put(pk_task, controlmode);
		}
		
		return resultMap;
	}
	
	/**
	 * 获取元数据
	 * @return
	 * @throws BusinessException
	 */
	public MetaData makeMetaData() throws BusinessException {
		MetaData metaData = new MetaData();
		Field fld = null;
		
		fld = new Field();
		fld.setCaption("会计期间");
		fld.setDbColumnType(java.sql.Types.VARCHAR);
		fld.setFldname("pk_accperiod");
		fld.setPrecision(250);
		metaData.addField(fld);
		
		/*fld = new Field();
		fld.setCaption("会计期间年月");
		fld.setDbColumnType(java.sql.Types.VARCHAR);
		fld.setFldname("accperiod_year_month");
		fld.setPrecision(250);
		metaData.addField(fld);*/
		
		fld = new Field();
		fld.setCaption("责任核算账簿");
		fld.setDbColumnType(java.sql.Types.VARCHAR);
		fld.setFldname("pk_accountingbook");
		fld.setPrecision(250);
		metaData.addField(fld);
		
		fld = new Field();
		fld.setCaption("责任核算账簿名称");
		fld.setDbColumnType(java.sql.Types.VARCHAR);
		fld.setFldname("accountingbook_name");
		fld.setPrecision(250);
		metaData.addField(fld);
		
		fld = new Field();
		fld.setCaption("工作分类");
		fld.setDbColumnType(java.sql.Types.VARCHAR);
		fld.setFldname("pk_taskclass");
		fld.setPrecision(250);
		metaData.addField(fld);
		
		fld = new Field();
		fld.setCaption("工作分类名称");
		fld.setDbColumnType(java.sql.Types.VARCHAR);
		fld.setFldname("taskclass_name");
		fld.setPrecision(250);
		metaData.addField(fld);
		
		fld = new Field();
		fld.setCaption("任务状态");
		fld.setDbColumnType(java.sql.Types.VARCHAR);
		fld.setFldname("task_status");
		fld.setPrecision(250);
		metaData.addField(fld);
		
		return metaData;
	}
	
	/**
	 * 获取到所有的工作任务分类
	 * @return pk_taskclass-工作分类主键，task_name工作分类名称
	 * @throws BusinessException
	 */
	private List<HashMap<String, String>> getTaskclassVO() throws BusinessException{
		// 查询所有的工作任务分类
		CMSqlBuilder finSql = new CMSqlBuilder();
		finSql.select();
		finSql.append(" * ");
		finSql.from(" QCB_TASKCLASS ");
		finSql.where();
		finSql.append(" pk_taskclass in ( ");
		finSql.append(" select DISTINCT(pk_taskclass) ");
		finSql.append(" from qcb_workbench ");
		finSql.append(" ) ");
		ArrayList<Taskclass> taskTypeList = (ArrayList<Taskclass>) baseDAOManager
				.executeQuery(finSql.toString(), new BeanListProcessor(Taskclass.class));
		if (null == taskTypeList || taskTypeList.size() <= 0) {
			return null;
		}
		List<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
		int taskLen = taskTypeList.size();
		HashMap<String, String> taskMap = null;
		for (int i = 0; i < taskLen; i++) {
			taskMap = new HashMap<String, String>();
			Taskclass tc = taskTypeList.get(i);
			String pk_taskclass = tc.getPk_taskclass();
			String task_name = tc.getName();
			taskMap.put("pk_taskclass", pk_taskclass);
			taskMap.put("task_name", task_name);
			
			result.add(taskMap);
		}
		
		return result;
	}
	
	/**
	 * 根据集团查询所有的责任核算账簿信息
	 * @param pk_group 集团主键
	 * @return 
	 * @throws BusinessException
	 */
	private Map<String, LiabilityBookVO> getLiabilityBookVO(String pk_group) throws BusinessException{
		CMSqlBuilder finSql = new CMSqlBuilder();
		finSql.select();
		finSql.append(" code,name,pk_liacenter,pk_fatherorg,pk_liabilitybook,pk_relorg,pk_group,pk_setofbook,enablestate ");
		finSql.append(" from ( ");
		finSql.append(" SELECT org_liabilitybook.creationtime,org_liabilitybook.creator,org_liabilitybook.modifiedtime, " );
		finSql.append(" org_liabilitybook.modifier, org_liabilitybook.dataoriginflag,org_liabilitybook.dr, ");
		finSql.append(" org_liabilitybook.enablestate,org_liabilitybook.liabilitytype,org_liabilitybook.ts,  ");
		finSql.append(" org_liabilitybook.code,org_liabilitybook.name,org_liabilitybook.name2,org_liabilitybook.name3, ");
		finSql.append(" org_liabilitybook.name4,org_liabilitybook.name5,org_liabilitybook.name6, org_liabilitybook.mnecode, ");
		finSql.append(" org_liabilitybook.pk_exratescheme,org_liabilitybook.pk_group,org_liabilitybook.pk_liabilitybook,  ");
		finSql.append(" org_liabilitybook.pk_liabilityperiod,org_liabilitybook.pk_org,org_liabilitybook.pk_relorg, ");
		finSql.append(" org_liabilitybook.pk_setofbook, org_liabilitybook.shortname,org_liabilitybook.shortname2, ");
		finSql.append(" org_liabilitybook.shortname3,org_liabilitybook.shortname4,org_liabilitybook.shortname5, ");
		finSql.append(" org_liabilitybook.shortname6, org_liabilitybook.pk_setofbook || org_liacenter.pk_liabilitycenter as pk_liacenter, ");
		finSql.append(" org_liabilitybook.pk_setofbook || org_liacenter.pk_fatherorg as pk_fatherorg ");
		finSql.append(" FROM org_liabilitybook  ");
		finSql.append(" LEFT JOIN org_liacenter ON org_liabilitybook.pk_relorg = org_liacenter.pk_liabilitycenter ");
		finSql.append(" ) org_liabilitybook ");
		finSql.where();
		finSql.append(" enablestate = 2 ");
		if (StringUtils.isNotEmpty(pk_group)) {
			finSql.and();
			finSql.append(" pk_group ", pk_group);
		}
		ArrayList<LiabilityBookVO> liabilityList = (ArrayList<LiabilityBookVO>) baseDAOManager
				.executeQuery(finSql.toString(), new BeanListProcessor(LiabilityBookVO.class));
		if (null == liabilityList || liabilityList.size() <= 0) {
			return null;
		}
		Map<String, LiabilityBookVO> result = new HashMap<String, LiabilityBookVO>();
		int bookLen = liabilityList.size();
		for (int i = 0; i < bookLen; i++) {
			LiabilityBookVO liabilityBookVO = liabilityList.get(i);
			result.put(liabilityBookVO.getPk_liabilitybook(), liabilityBookVO);
		}
		
		return result;
	}

}
