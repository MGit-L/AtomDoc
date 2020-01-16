package nc.ui.qcb.smart.designer;

import java.awt.Container;

import nc.pub.smart.context.SmartContext;
import nc.pub.smart.provider.Provider;
import nc.ui.pub.smart.provider.IProviderDesignWizard;
import nc.ui.qcb.smart.provider.QcbMonitorProvider;

public class QcbProviderDesignWizard implements IProviderDesignWizard {

	public QcbProviderDesignWizard(){};
	
	@Override
	public Provider design(Container parent, Provider provider, SmartContext context) {
		QcbMonitorProvider monitorProvider = new QcbMonitorProvider();
		monitorProvider.setCode("table_qcb");
		monitorProvider.setTitle("全成本监控台表");
		return monitorProvider;
	}

}
