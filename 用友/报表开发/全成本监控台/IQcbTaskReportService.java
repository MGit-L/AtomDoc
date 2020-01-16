package nc.itf.qcb;

import nc.pub.smart.data.DataSet;
import nc.pub.smart.metadata.MetaData;
import nc.vo.pub.BusinessException;

import com.ufida.report.anareport.exec.FreeRemoteResult;

/**
 * 全成本监控台报表接口
 * @author liweiat
 * @time 2019-11-30 13:24:00
 */
public interface IQcbTaskReportService {

	/**
	 * 1、设置语义模型的元数据
	 * 2、数据转换成报表需要的二维结构
	 * @param pk_group 集团
	 * @param pk_accperiod 会计期间
	 * @param pk_accountingbooks 责任核算账簿数组
	 * @return
	 * @throws BusinessException
	 */
	public DataSet makeDataSet(String pk_group, String pk_accperiod, String... pk_accountingbooks) throws BusinessException;
	
	/**
	 * 获取语义模型的元数据
	 * @return
	 * @throws BusinessException
	 */
	public MetaData makeMetaData() throws BusinessException; 
}
