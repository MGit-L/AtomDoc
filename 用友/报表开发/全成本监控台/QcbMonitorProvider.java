package nc.ui.qcb.smart.provider;

import nc.bs.framework.common.NCLocator;
import nc.itf.qcb.IQcbTaskReportService;
import nc.pub.smart.context.SmartContext;
import nc.pub.smart.data.DataSet;
import nc.pub.smart.exception.SmartException;
import nc.pub.smart.metadata.MetaData;
import nc.pub.smart.provider.SemanticDataProvider;
import nc.pub.smart.script.statement.select.PlainSelect;
import nc.vo.pub.BusinessException;
import nc.vo.pub.query.ConditionVO;

import org.apache.commons.lang.StringUtils;

import com.ufida.report.anareport.FreeReportContextKey;

public class QcbMonitorProvider extends SemanticDataProvider {

	IQcbTaskReportService reportService;
	
	@Override
	public DataSet provideData(SmartContext context) throws Exception {
		DataSet dataSet = new DataSet();
		// ����
		String pk_group = (String) context.getAttribute(FreeReportContextKey.LOGIN_GROUP_ID);
		
		ConditionVO[] conditionVOs = (ConditionVO[]) context.getAttribute(FreeReportContextKey.KEY_REPORT_QUERYCONDITIONVOS);
		if (null == conditionVOs || conditionVOs.length <= 0) {
			return dataSet;
		}
		int conLen = conditionVOs.length;
		String pk_accperiod = null;
		String accountingbook = null;
		for (int i = 0; i < conLen; i++) {
			ConditionVO conVo = conditionVOs[i];
			String fieldCode = conVo.getFieldCode();
			if ("pk_accountingbook".equals(fieldCode)) {
				// ���κ����˲�
				accountingbook = conVo.getValue();
			} else if ("pk_accperiod".equals(fieldCode)) {
				// ����ڼ�
				pk_accperiod = conVo.getValue();
			}
		}
		if (StringUtils.isEmpty(pk_accperiod) || StringUtils.isEmpty(accountingbook)) {
			return dataSet;
		}
		accountingbook = accountingbook.replaceAll(" ", "");
		accountingbook = accountingbook.replace("(", "");
		accountingbook = accountingbook.replace("'", "");
		accountingbook = accountingbook.replace(")", "");
		//List<String> agbList = new ArrayList();
		
		String[] pk_accountingbooks = accountingbook.split(",");
		
		//���������Ҫ��գ��������������ڱ����Զ����ɵ���ʱ�����ٴβ�ѯһ��
		PlainSelect select = (PlainSelect)context.getAttribute("key_current_plain_select");
		select.setWhere(null);
		
		return getReportService().makeDataSet(pk_group, pk_accperiod, pk_accountingbooks);
		//return new DataSet();
	}

	@Override
	public MetaData provideMetaData(SmartContext context) throws SmartException {
		try {
			return getReportService().makeMetaData();
		} catch (BusinessException e) {
			throw new SmartException(e);
		}
	}

	public IQcbTaskReportService getReportService() {
		if (null == reportService) {
			reportService = NCLocator.getInstance().lookup(IQcbTaskReportService.class);
		}
		return reportService;
	}
}
