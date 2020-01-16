package nc.itf.qcb;

import nc.pub.smart.data.DataSet;
import nc.pub.smart.metadata.MetaData;
import nc.vo.pub.BusinessException;

import com.ufida.report.anareport.exec.FreeRemoteResult;

/**
 * ȫ�ɱ����̨����ӿ�
 * @author liweiat
 * @time 2019-11-30 13:24:00
 */
public interface IQcbTaskReportService {

	/**
	 * 1����������ģ�͵�Ԫ����
	 * 2������ת���ɱ�����Ҫ�Ķ�ά�ṹ
	 * @param pk_group ����
	 * @param pk_accperiod ����ڼ�
	 * @param pk_accountingbooks ���κ����˲�����
	 * @return
	 * @throws BusinessException
	 */
	public DataSet makeDataSet(String pk_group, String pk_accperiod, String... pk_accountingbooks) throws BusinessException;
	
	/**
	 * ��ȡ����ģ�͵�Ԫ����
	 * @return
	 * @throws BusinessException
	 */
	public MetaData makeMetaData() throws BusinessException; 
}
