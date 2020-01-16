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
 * ȫ�ɱ����̨����ӿ�ʵ����
 * @author liweiat
 * @time 2019-11-30 13:30:00
 */
public class QcbTaskReportServiceImpl implements IQcbTaskReportService {
	
	public final String CONTROLMODE_CONTROL = "1";// ���Ʒ�ʽ���ϸ����
	public final String CONTROLMODE_NOT_CONTROL = "2";// ���Ʒ�ʽ��������
	
	public final String FINISH = "1";// ���״̬��1���
	public final String NOT_FINISH = "2";// ���״̬��2δ���
	
	public final String TASKATTRIBUTE_BEFORE = "1";// �������ԣ�1ǰ������
	public final String TASKATTRIBUTE_AFTER = "2";// �������ԣ�2��������
	
	public final String TASK_STATUS_FINISH = "2";// ���״̬
	public final String TASK_STATUS_NOT_EXECUTABLE = "3";// ����ִ��״̬
	public final String TASK_STATUS_EXECUTABLE = "4";// ��ִ��״̬

	private final BaseDAO baseDAOManager = new BaseDAO();
	
	/**
	 * 1����������ģ�͵�Ԫ����
	 * 2������ת���ɱ�����Ҫ�Ķ�ά�ṹ
	 * @param pk_group ����
	 * @param pk_accperiod ����ڼ�
	 * @param pk_accountingbooks ���κ����˲�����
	 * @return
	 * @throws BusinessException
	 */
	public DataSet makeDataSet(String pk_group, String pk_accperiod, String... pk_accountingbooks) throws BusinessException {
		AggWorkbench[] bills = getAggWorkbench(pk_accperiod, pk_accountingbooks);
		DataSet result = new DataSet();
		if (null == bills || bills.length <= 0) {
			return result;
		}
		
		// ��������ģ�͵�Ԫ����
		result.setMetaData(makeMetaData());
		// ��ȡ���ü��������е����κ����˲�
		Map liabilityMap = getLiabilityBookVO(pk_group);
		// ��ȡ�������������
		HashMap<String, HashMap<String, String>> resultMap = getTwoDimensionalMap(bills);
		// ��ȡ���еĹ����������
		List<HashMap<String, String>> taskclassList = getTaskclassVO();
		
		Object[][] datas = getTwoDimensionalDatas(taskclassList, resultMap,
				liabilityMap, pk_accperiod, pk_accountingbooks);

		result.setDatas(datas);
		
		return result;
	}
	
	/**
	 * ͨ�����κ����˲�������ڼ��ѯȫ�ɱ��ۺ϶�������
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
		// �����κ����˲���ŵ���ʱ����
		InSQLCreator inSQLCreator = new InSQLCreator();
		String condition = inSQLCreator.getInSQL(pk_accountingbooks);
		// ���κ����˲�������ڼ���Ϊ��ѯ������ѯ���е�ȫ�ɱ��ۺ�vo����
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
	 * ͨ��sql����ѯȫ�ɱ�pk����
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
		} catch (Exception e) {// �����ʷ�����쳣�̳й�ϵ
			throw new BusinessException("��ѯ����", e);
		}
	}

	/**
	 * �����е�������װ�ɶ�ά����
	 * @param taskclassList ���������б�
	 * @param resultMap ����
	 * @param liabilityMap ���κ����˲�
	 * @param pk_accperiod ����ڼ�
	 * @param pk_accountingbooks ���κ����˲�����
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
		int row = pk_accountingbooks.length*row_task;// ����
		Object[][] datas = new Object[row][6];
		for (int i = 0; i < row_book; i++) {
			// ���κ����˲�
			String pk_accountingbook = pk_accountingbooks[i];
			// ���κ����˲�����
			LiabilityBookVO bookVO = (LiabilityBookVO) liabilityMap.get(pk_accountingbook);
			// �������κ����˲���ȡ����״̬
			HashMap<String, String> dataMap = resultMap.get(pk_accountingbook);
			
			for (int j = 0; j < row_task; j++) {
				// ����ڼ�
				int colu = 0;
				datas[i*row_task+j][colu] = pk_accperiod;
				colu++;
				// ���κ����˲�
				datas[i*row_task+j][colu] = pk_accountingbook;
				colu++;
				// ���κ����˲�����
				datas[i*row_task+j][colu] = bookVO.getName();
				colu++;
				// ��������
				HashMap<String, String> taskMap = taskclassList.get(j);
				datas[i*row_task+j][colu] = taskMap.get("pk_taskclass");
				colu++;
				// ������������
				datas[i*row_task+j][colu] = taskMap.get("task_name");
				colu++;
				// ����״̬
				String pk_taskclass = taskMap.get("pk_taskclass");
				if (null != dataMap && StringUtils.isNotEmpty(dataMap.get(pk_taskclass))) {
					String task_status = dataMap.get(pk_taskclass);
					datas[i*row_task+j][colu] = task_status;
				} else {
					// δ���̬
					datas[i*row_task+j][colu] = "0";
				}
			}
		}
		
		return datas;
	}
	
	/**
	 * 1�������еĹ�����������ж�������״̬������װ��map
	 * 2��map�������κ����˲������������������
	 * @param bills ���е���������
	 * @return ���κ����˲�[key]-(�����������[key]-����״̬[value])[value]
	 */
	private HashMap<String, HashMap<String, String>> getTwoDimensionalMap(
			AggWorkbench[] bills) throws BusinessException {
		HashMap<String, HashMap<String, String>> voMap = new HashMap<String, HashMap<String, String>>();
		int billLen = bills.length;
		
		// ����ȫ�ɱ���Ϣ������ѯ�����嵥��Ϣ����ȡ���Ʒ�ʽ��
		Map<String, String> qcbMap = getQcbworklistMap(bills);
		// ����ȫ�ɱ���Ϣ������ѯ�ӱ�ǰ���������״̬
		Map<String, String> taskChildMap = getTaskChildren(bills);
		for (int i = 0; i < billLen; i++) {
			// ���κ����˲�
			String pk_accountingbook = bills[i].getParentVO().getPk_accountingbook();
			// �������κ����˲���ȡ���е��������״̬map
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
	 * �ж����������״̬����Ҫ����map���ֵ�����жϣ���ȷ���Ƿ����µ�����״̬
	 * 1����������ǿ�ִ��״̬--ֱ��return
	 * 2����������ǲ���ִ��״̬--�ж��Ƿ���ڿ�ִ��״̬������
	 * 3��������������״̬--�ж��Ƿ�1��2������
	 * @param aggBill ȫ�ɱ�����̨�ۺ�VO����
	 * @param voMap ��װ��map�����κ����˲�[key]-(�����������[key]-����״̬[value])[value]
	 * @param qcbMap ����ȫ�ɱ���Ϣ������ѯ�����嵥��Ϣ����ȡ���Ʒ�ʽ��
	 * @param taskChildMap ����ȫ�ɱ���Ϣ������ѯ�ӱ�ǰ���������״̬
	 * @param dataMap �������κ����˲���ȡ���е��������״̬map
	 */
	private void funcTaskStatusWith_voMap(AggWorkbench aggBill, 
			HashMap<String, HashMap<String, String>> voMap, Map<String, String> qcbMap, 
			Map<String, String> taskChildMap, HashMap<String, String> dataMap) {
		Workbench workbench = aggBill.getParentVO();
		// �����������
		String pk_taskclass = workbench.getPk_taskclass();
		
		// dataMap��Ĺ�������pk��Ӧ������״̬
		String data_task_status = dataMap.get(pk_taskclass);
		
		// ��ִ��״̬:ֻҪ��һ�������ǿ�ִ��״̬�����������ǿ�ִ��״̬
		if (TASK_STATUS_EXECUTABLE.equals(data_task_status)) {
			return;
		}
		
		// ��ѯ���Ʒ�ʽ
		String pk_tasklist = workbench.getPk_tasklist();
		String controlmode = null;
		if (null != qcbMap) {
			controlmode = qcbMap.get(pk_tasklist);
		}
		
		// ����ִ��״̬
		if (TASK_STATUS_NOT_EXECUTABLE.equals(data_task_status)) {
			// ���Ʒ�ʽ:1���ϸ���� 2��������
			if (CONTROLMODE_CONTROL.equals(controlmode)) {
				funcTaskStatusWith_Pk_task_b(aggBill, dataMap, voMap, taskChildMap);
			}
		} else {
			funcTaskStatusWithout_voMap(aggBill, voMap, qcbMap, taskChildMap);
		}
	}

	/**
	 * �ж����������״̬����һ��ֱ�ӽ��жϵ�����״̬����map��
	 * @param aggBill ȫ�ɱ�����̨�ۺ�VO����
	 * @param voMap ��װ��map�����κ����˲�[key]-(�����������[key]-����״̬[value])[value]
	 * @param qcbMap ����ȫ�ɱ���Ϣ������ѯ�����嵥��Ϣ����ȡ���Ʒ�ʽ��
	 * @param taskChildMap ����ȫ�ɱ���Ϣ������ѯ�ӱ�ǰ���������״̬
	 */
	private void funcTaskStatusWithout_voMap(AggWorkbench aggBill, 
			HashMap<String, HashMap<String, String>> voMap, Map<String, String> qcbMap, 
			Map<String, String> taskChildMap) {
		Workbench workbench = aggBill.getParentVO();
		// ִ�����κ����˲�
		String pk_accountingbook = workbench.getPk_accountingbook();
		// �����������
		String pk_taskclass = workbench.getPk_taskclass();
		
		HashMap<String, String> dataMap = new HashMap<String, String>();
		if (FINISH.equals(workbench.getFishstatus())) {
			// ���̬
			putVoMap(pk_taskclass, TASK_STATUS_FINISH, pk_accountingbook, dataMap, voMap);
			return;
		}
		
		// ��ѯ���Ʒ�ʽ
		String pk_tasklist = workbench.getPk_tasklist();
		String controlmode = null;
		if (null != qcbMap) {
			controlmode = qcbMap.get(pk_tasklist);
		}
		if (CONTROLMODE_CONTROL.equals(controlmode)) {
			funcTaskStatusWith_Pk_task_b(aggBill, dataMap, voMap, taskChildMap);
		} else {
			// ��ִ��״̬:������
			putVoMap(pk_taskclass, TASK_STATUS_EXECUTABLE, pk_accountingbook, dataMap, voMap);
		}
	}
	
	/**
	 * �ж������Ƿ�Ϊ����ִ��״̬��
	 * 1�����Ʒ�ʽ���ϸ���ƣ�������ǰ����������״̬��δ��ɣ�������״̬Ϊ����ִ��
	 * @param aggBill ȫ�ɱ�����̨�ۺ�VO����
	 * @param dataMap �������κ����˲���ȡ���е��������״̬map
	 * @param voMap ��װ��map�����κ����˲�[key]-(�����������[key]-����״̬[value])[value]
	 * @param taskChildMap ����ȫ�ɱ���Ϣ������ѯ�ӱ�ǰ���������״̬
	 */
	private void funcTaskStatusWith_Pk_task_b(AggWorkbench aggBill, HashMap<String, String> dataMap, 
			HashMap<String, HashMap<String, String>> voMap, Map<String, String> taskChildMap){
		Workbench workbench = aggBill.getParentVO();
		// ִ�����κ����˲�
		String pk_accountingbook = workbench.getPk_accountingbook();
		// �����������
		String pk_taskclass = workbench.getPk_taskclass();
		
		Pk_task_b[] taskChildren = (Pk_task_b[]) aggBill.getChildren(Pk_task_b.class);
		if (null == taskChildren || taskChildren.length <= 0) {
			// ��ִ��״̬:û������
			putVoMap(pk_taskclass, TASK_STATUS_EXECUTABLE, pk_accountingbook, dataMap, voMap);
			return;
		}
		int childLen = taskChildren.length;
		boolean childFinishFlag = true;// ����ǰ���������̬
		boolean childTaskattrFlag = false;// �Ƿ����ǰ������
		for (int j = 0; j < childLen; j++) {
			Pk_task_b taskChild = taskChildren[j];
			// ǰ������
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
			// ����ִ��״̬:���Ʒ�ʽ���ϸ���ƣ�������ǰ����������״̬��δ��ɣ�������״̬Ϊ����ִ��
			putVoMap(pk_taskclass, TASK_STATUS_NOT_EXECUTABLE, pk_accountingbook, dataMap, voMap);
		} else {
			// ��ִ��״̬
			putVoMap(pk_taskclass, TASK_STATUS_EXECUTABLE, pk_accountingbook, dataMap, voMap);
		}
	}
	
	/**
	 * ����VoMap
	 * @param pk_taskclass �����������
	 * @param taskclass_status �����������״̬
	 * @param pk_accountingbook ���κ����˲�
	 * @param dataMap �����������[key]-�����������״̬[value]
	 * @param voMap ��װ��map�����κ����˲�[key]-(�����������[key]-����״̬[value])[value]
	 */
	private void putVoMap(String pk_taskclass, String taskclass_status, String pk_accountingbook,
			HashMap<String, String> dataMap, HashMap<String, HashMap<String, String>> voMap){
		dataMap.put(pk_taskclass, taskclass_status);
		voMap.put(pk_accountingbook, dataMap);
	}

	/**
	 * ����ȫ�ɱ���Ϣ������ѯ�ӱ�ǰ���������״̬
	 * @param bills
	 * @return ǰ������������key��-ǰ���������״̬��value��
	 * 1����� 2��δ���
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
				// ǰ������
				if (TASKATTRIBUTE_BEFORE.equals(taskChild.getTaskattribute())) {
					String pk_task_b = taskChild.getPk_task_b();
					String pk_bench = taskChild.getPk_bench();
					// Ŀǰ��ţ�ǰ������������key��-ǰ�������Ӧ����������value��
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
	 * ����ȫ�ɱ���Ϣ������ѯ�����嵥��Ϣ����ȡ���Ʒ�ʽ��
	 * @param bills ȫ�ɱ���Ϣ
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
	 * ��ȡԪ����
	 * @return
	 * @throws BusinessException
	 */
	public MetaData makeMetaData() throws BusinessException {
		MetaData metaData = new MetaData();
		Field fld = null;
		
		fld = new Field();
		fld.setCaption("����ڼ�");
		fld.setDbColumnType(java.sql.Types.VARCHAR);
		fld.setFldname("pk_accperiod");
		fld.setPrecision(250);
		metaData.addField(fld);
		
		/*fld = new Field();
		fld.setCaption("����ڼ�����");
		fld.setDbColumnType(java.sql.Types.VARCHAR);
		fld.setFldname("accperiod_year_month");
		fld.setPrecision(250);
		metaData.addField(fld);*/
		
		fld = new Field();
		fld.setCaption("���κ����˲�");
		fld.setDbColumnType(java.sql.Types.VARCHAR);
		fld.setFldname("pk_accountingbook");
		fld.setPrecision(250);
		metaData.addField(fld);
		
		fld = new Field();
		fld.setCaption("���κ����˲�����");
		fld.setDbColumnType(java.sql.Types.VARCHAR);
		fld.setFldname("accountingbook_name");
		fld.setPrecision(250);
		metaData.addField(fld);
		
		fld = new Field();
		fld.setCaption("��������");
		fld.setDbColumnType(java.sql.Types.VARCHAR);
		fld.setFldname("pk_taskclass");
		fld.setPrecision(250);
		metaData.addField(fld);
		
		fld = new Field();
		fld.setCaption("������������");
		fld.setDbColumnType(java.sql.Types.VARCHAR);
		fld.setFldname("taskclass_name");
		fld.setPrecision(250);
		metaData.addField(fld);
		
		fld = new Field();
		fld.setCaption("����״̬");
		fld.setDbColumnType(java.sql.Types.VARCHAR);
		fld.setFldname("task_status");
		fld.setPrecision(250);
		metaData.addField(fld);
		
		return metaData;
	}
	
	/**
	 * ��ȡ�����еĹ����������
	 * @return pk_taskclass-��������������task_name������������
	 * @throws BusinessException
	 */
	private List<HashMap<String, String>> getTaskclassVO() throws BusinessException{
		// ��ѯ���еĹ����������
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
	 * ���ݼ��Ų�ѯ���е����κ����˲���Ϣ
	 * @param pk_group ��������
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
